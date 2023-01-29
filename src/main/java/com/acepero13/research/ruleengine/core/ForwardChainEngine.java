package com.acepero13.research.ruleengine.core;

import com.acepero13.research.ruleengine.api.FactBaseListener;
import com.acepero13.research.ruleengine.api.Rule;
import com.acepero13.research.ruleengine.api.RuleEngine;
import com.acepero13.research.ruleengine.api.RulesEventsListener;
import com.acepero13.research.ruleengine.model.Facts;
import com.acepero13.research.ruleengine.model.Rules;
import lombok.Builder;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

// TODO: Consider changes (updates)
// TODO: ForwardChainEngineParams

public class ForwardChainEngine implements RuleEngine, FactBaseListener {
    private static final Logger logger = LogManager.getLogger();

    private final ForwardChainEngineParameters params;
    private final List<RulesEventsListener> listeners = new ArrayList<>();
    private final Rules rules;
    private final List<String> newFacts = new ArrayList<>();
    private final List<String> updatedFacts = new ArrayList<>();

    public ForwardChainEngine(Rules rules) {
        this(Objects.requireNonNull(rules), ForwardChainEngineParameters.defaultParameters());
    }

    public ForwardChainEngine(Rules rules, ForwardChainEngineParameters parameters) {
        this.rules = rules;
        this.params = parameters;
    }

    @Override
    public void fire(Rules rules, Facts facts) {
        facts.register(this);

        Set<Rule> agenda;
        do {
            agenda = doFire(rules, facts);
        } while (thereAreStillRulesToConsider(agenda));

        facts.unregister(this);
    }

    private Set<Rule> doFire(Rules rules, Facts facts) {
        Set<Rule> agenda;
        newFacts.clear();
        agenda = selectActiveRules(rules, facts);
        executeAgenda(facts, agenda);
        return agenda;
    }

    private boolean thereAreStillRulesToConsider(Set<Rule> agenda) {
        return !agenda.isEmpty() && (!newFacts.isEmpty() || !updatedFacts.isEmpty());
    }

    private void executeAgenda(Facts facts, Set<Rule> agenda) {
        for (Rule rule : agenda) {
            tryToFire(facts, rule);
        }
    }

    @Override
    public void fire(Facts facts) {
        fire(this.rules, facts);
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

    public ForwardChainEngineParameters getParameters() {
        return params;
    }

    private Set<Rule> selectActiveRules(Rules rules, Facts facts) {
        Set<Rule> agenda = new TreeSet<>();
        for (Rule rule : rules) {
            if (evaluationSucceeded(facts, rule)) {
                agenda.add(rule);
            }
        }
        return agenda;
    }

    private boolean evaluationSucceeded(Facts facts, Rule rule) {
        try {
            return rule.evaluates(facts);
        } catch (Exception e) {
            logFailure(e, rule);
            return false;
        }


    }

    private boolean execute(Facts facts, Rule rule) {
        listeners.forEach(l -> l.beforeFire(rule));
        boolean result = tryToFire(facts, rule);
        listeners.forEach(l -> l.afterFire(rule));
        return result;
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
                + " Failing rule:" + rule;
        logger.error(message);
    }


    private boolean isThresholdTooLow(Rule rule) {
        if (params.getPriorityThreshold() <= 0) {
            return false;
        }
        return params.getPriorityThreshold() > rule.priority();
    }

    @Override
    public <T> void newFactAdded(String name, T value) {
        this.newFacts.add(name);
    }

    @Override
    public <T> void newFactReplaced(String name, T value) {
        if(params.isConsiderUpdatesFacts()) {
            updatedFacts.add(name);
        }
    }

    @Data
    @Builder
    public static class ForwardChainEngineParameters {
        private final int priorityThreshold;
        private final boolean considerNewFacts;
        private final boolean considerUpdatesFacts;

        public static ForwardChainEngineParameters defaultParameters() {
            return new ForwardChainEngineParameters(Integer.MAX_VALUE - 1, true, false);
        }

    }
}
