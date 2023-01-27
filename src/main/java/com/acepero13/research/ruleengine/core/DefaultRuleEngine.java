package com.acepero13.research.ruleengine.core;

import com.acepero13.research.ruleengine.api.RuleEngine;
import com.acepero13.research.ruleengine.api.RulesEventsListener;
import com.acepero13.research.ruleengine.model.Facts;
import com.acepero13.research.ruleengine.api.Rule;
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
    public void fire(Rules rules, Facts facts) {

    }

    @Override
    public void fire(Facts facts) {
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

    @Override
    public EngineParameters getParameters() {
        return params;
    }

    private void doFire(Rules rules, Facts facts) {
        for (Rule rule : rules) {
            if (shouldSkip(rule, facts)) {
                continue;
            }
            if (evaluationFailed(facts, rule)) {
                if (params.isSkipOnFirstNonTriggeredRule()) {
                    break;
                }
                continue;
            }
            boolean result = execute(facts, rule);
            if (shouldStopExecutionAfter(result)) {
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
