package com.acepero13.research.ruleengine.core.engines;

import com.acepero13.research.ruleengine.api.RuleEngine;
import com.acepero13.research.ruleengine.api.RulesEventsListener;
import com.acepero13.research.ruleengine.api.Facts;
import com.acepero13.research.ruleengine.api.Rule;
import com.acepero13.research.ruleengine.core.utils.LoggingUtils;
import com.acepero13.research.ruleengine.model.Rules;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DefaultRuleEngine implements RuleEngine {
    private static final Logger logger = LogManager.getLogger();
    private final Rules rules;
    private final List<RulesEventsListener> listeners = new ArrayList<>();
    private final EngineParameters params;

    public DefaultRuleEngine(Rules rules) {
        this(Objects.requireNonNull(rules, "Rules cannot be null"), EngineParameters.defaultParameters());
    }

    public DefaultRuleEngine(Rules rules, EngineParameters parameters) {
        this.rules = rules;
        this.params = parameters;
    }

    @Override
    public void fire(Rules rules, Facts facts) {
        doFire(rules, facts);
    }

    @Override
    public void fire(Facts facts) {
        fire(rules, facts);
    }

    @Override
    public void register(RulesEventsListener listener) {
        listeners.add(listener);
    }

    @Override
    public void unregister(RulesEventsListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void unregisterAll() {
        listeners.clear();
    }

    public EngineParameters getParameters() {
        return params;
    }

    private void doFire(Rules rules, Facts facts) {
        logger.info("Engine parameters: {}", params);
        logger.info("Registered rules:");
        LoggingUtils.logIterable(rules, logger);
        logger.info("Known facts:");
        LoggingUtils.logIterable(facts, logger);

        executeAgenda(rules, facts);

        logger.info("Finished evaluating rules. Facts based after evaluation: {}", facts);
    }

    private void executeAgenda(Rules rules, Facts facts) {
        for (Rule rule : rules) {
            if (shouldSkip(rule, facts)) {
                logger.debug("Skipping rule: {}. With facts: {}", rule, facts);
                continue;
            }
            if (evaluationFailed(facts, rule)) {
                listeners.forEach(l -> l.evaluationFailed(rule, facts));
                if (params.isSkipOnFirstNonTriggeredRule()) {
                    logger.debug("Stop executing rules because skipOnFirstNonTriggeredRule flag is activated");
                    break;
                }
                logger.debug("Evaluation of rule: {}. With facts: {}. Continuing evaluation", rule, facts);
                continue;
            }
            logger.info("Rule: '{}' is activated", rule);
            boolean result = execute(facts, rule);
            logger.info("Firing rule: '{}'", rule);
            if (shouldStopExecutionAfter(result)) {
                logger.debug("Stop executing rules because isSkipOnFirstAppliedRule flag is activated and rule was executed successfully.");
                break;
            }

        }
    }

    private boolean evaluationFailed(Facts facts, Rule rule) {
        try {
            return !rule.evaluates(facts);
        } catch (Exception e) {
            logFailure(e, rule);
            return true;
        }


    }

    private boolean execute(Facts facts, Rule rule) {
        listeners.forEach(l -> l.beforeFire(rule));
        boolean result = tryToFire(facts, rule);
        listeners.forEach(l -> l.afterFire(rule));
        return result;
    }

    private boolean shouldStopExecutionAfter(boolean result) {
        if (params.isSkipOnFirstAppliedRule() && result) {
            return true;
        }
        return params.isSkipOnFirstFailedRule() && !result;
    }

    private boolean tryToFire(Facts facts, Rule rule) {
        try {
            rule.execute(facts);
            return true;
        } catch (Exception e) {
            logFailure(e, rule);
            listeners.forEach(l -> l.executionFailed(rule, facts));
            return false;
        }
    }

    private void logFailure(Exception e, Rule rule) {
        var message = "Evaluation failed with error. " + e.getMessage() + " ."
                + (params.isSkipOnFirstFailedRule() ? "Skipping subsequent rules" : "Continuing with other rules.")
                + " Failing rule:" + rule;
        logger.error(message);
    }

    private boolean shouldSkip(Rule rule, Facts facts) {
        return listeners.stream().anyMatch(l -> !l.shouldFire(rule, facts)) || isThresholdTooLow(rule);
    }

    private boolean isThresholdTooLow(Rule rule) {
        if (params.getPriorityThreshold() <= 0) {
            return false;
        }
        return params.getPriorityThreshold() > rule.priority();
    }
}
