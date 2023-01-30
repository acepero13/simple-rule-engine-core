package com.acepero13.research.ruleengine.core;

import com.acepero13.research.ruleengine.api.FactBaseListener;
import com.acepero13.research.ruleengine.api.Rule;
import com.acepero13.research.ruleengine.api.RuleEngine;
import com.acepero13.research.ruleengine.api.RulesEventsListener;
import com.acepero13.research.ruleengine.api.Facts;
import com.acepero13.research.ruleengine.model.Rules;
import lombok.Builder;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

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
    public void fire(final Rules rules, final Facts facts) {
        Objects.requireNonNull(rules, "Rules cannot be empty");
        Objects.requireNonNull(facts, "Facts cannot be null");

        logger.debug("#### Starting forward chaining rule evaluation with the following facts: {}", facts);

        facts.register(this);

        List<Rule> agenda;
        do {
            agenda = doFire(rules, facts);
        } while (thereAreStillRulesToConsider(agenda));

        facts.unregister(this);
        logger.debug("#### Finished evaluating rules. Facts based after evaluation: {}", facts);
    }

    private List<Rule> doFire(Rules rules, Facts facts) {
        List<Rule> agenda;
        newFacts.clear();
        agenda = selectActiveRules(rules, facts);
        executeAgenda(facts, agenda);
        return agenda;
    }

    private boolean thereAreStillRulesToConsider(List<Rule> agenda) {
        logger.debug("thereAreStillRulesToConsider. Agenda is empty: {}; there are new Facts : {}", agenda.isEmpty(), !newFacts.isEmpty());
        return !agenda.isEmpty() && (!newFacts.isEmpty() || !updatedFacts.isEmpty());
    }

    private void executeAgenda(Facts facts, List<Rule> agenda) {
        logger.debug("#### Executing the following agenda: {}, with facts: {}", agenda, facts);
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

    private List<Rule> selectActiveRules(Rules rules, Facts facts) {
        List<Rule> agenda = new ArrayList<>();
        for (Rule rule : rules) {
            if (evaluationSucceeded(facts, rule)) {
                logger.debug("#### Activated rule: {}", rule);
                agenda.add(rule);
            }else {
                logger.debug("#### Rule {} failed", rule);
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


    private boolean tryToFire(Facts facts, Rule rule) {
        try {
            listeners.forEach(l -> l.beforeFire(rule));
            rule.execute(facts);
            listeners.forEach(l -> l.afterFire(rule));
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


    @Override
    public <T> void newFactAdded(String name, T value) {
        logger.debug("New fact {} added to ForwardEngine with value: {} ", name, value);
        this.newFacts.add(name);
    }

    @Override
    public <T> void newFactReplaced(String name, T value) {
        if (params.isConsiderUpdatesFacts()) {
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
