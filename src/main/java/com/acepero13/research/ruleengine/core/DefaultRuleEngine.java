package com.acepero13.research.ruleengine.core;

import com.acepero13.research.ruleengine.api.RuleEngine;
import com.acepero13.research.ruleengine.api.RulesEventsListener;
import com.acepero13.research.ruleengine.api.Facts;
import com.acepero13.research.ruleengine.api.Rule;
import com.acepero13.research.ruleengine.model.InMemoryFacts;
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
        this(Objects.requireNonNull(rules), EngineParameters.defaultParameters());
    }

    public DefaultRuleEngine(Rules rules, EngineParameters parameters) {
        this.rules = rules;
        this.params = parameters;
    }

    @Override
    public void fire(Rules rules, InMemoryFacts facts) {
        doFire(rules, facts);
    }

    @Override
    public void fire(InMemoryFacts facts) {
        doFire(rules, facts);
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

    public EngineParameters getParams() {
        return params;
    }

    private void doFire(Rules rules, Facts facts) {
        logger.debug("starting forward chaining rule evaluation with the following facts: {}", facts);
        for (Rule rule : rules) {
            if (shouldSkip(rule, facts)) {
                logger.debug("Skipping rule: {}. With facts: {}", rule, facts);
                continue;
            }
            if (evaluationFailed(facts, rule)) {
                if (params.isSkipOnFirstNonTriggeredRule()) {
                    logger.debug("Stop executing rules because skipOnFirstNonTriggeredRule flag is activated");
                    break;
                }
                logger.debug("Evaluation of rule: {}. With facts: {}. Continuing evaluation", rule, facts);
                continue;
            }
            boolean result = execute(facts, rule);
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
