package com.acepero13.research.ruleengine.core.engines;

import com.acepero13.research.ruleengine.api.FactBaseListener;
import com.acepero13.research.ruleengine.api.Rule;
import com.acepero13.research.ruleengine.api.RuleEngine;
import com.acepero13.research.ruleengine.api.RulesEventsListener;
import com.acepero13.research.ruleengine.api.Facts;
import com.acepero13.research.ruleengine.core.utils.LoggingUtils;
import com.acepero13.research.ruleengine.model.Rules;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Predicate;


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


        logger.info("Engine parameters: {}", params);
        logger.info("Registered rules:");
        LoggingUtils.logIterable(rules, logger);
        logger.info("Known facts:");
        LoggingUtils.logIterable(facts, logger);

        facts.register(this);

        List<Rule> agenda;
        do {
            agenda = doFire(rules, facts);

        } while (thereAreStillRulesToConsider(agenda, facts));

        facts.unregister(this);
        logger.info("Finished evaluating rules. Facts based after evaluation: {}", facts);
    }

    private List<Rule> doFire(Rules rules, Facts facts) {
        List<Rule> agenda;
        newFacts.clear();
        agenda = selectActiveRules(rules, facts);
        executeAgenda(facts, agenda);
        return agenda;
    }

    private boolean thereAreStillRulesToConsider(List<Rule> agenda, Facts facts) {
        logger.debug("thereAreStillRulesToConsider. Agenda is empty: {}; there are new Facts : {}", agenda.isEmpty(), !newFacts.isEmpty());
        if (params.stopOnCondition.test(facts)) {
            logger.debug("Stopping because stopCondition from parameters was activated");
            return false;
        }
        return !agenda.isEmpty() && (!newFacts.isEmpty() || !updatedFacts.isEmpty());
    }

    private void executeAgenda(Facts facts, List<Rule> agenda) {
        logger.info("#### Executing the following agenda: {}, with facts: {}", agenda, facts);
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
        logger.info("Evaluation started: ");
        List<Rule> agenda = new ArrayList<>();

        for (Rule rule : rules) {
            if (evaluationSucceeded(facts, rule)) {
                logger.info("Rule: '{}' is activated", rule);
                agenda.add(rule);
            } else {
                logger.info("Rule: '{}' is NOT activated ", rule);
            }
        }
        return agenda;
    }

    private boolean evaluationSucceeded(Facts facts, Rule rule) {
        try {
            return rule.evaluates(facts);
        } catch (Exception e) {
            listeners.forEach(l -> l.evaluationFailed(rule, facts));
            logFailure(e, rule);
            return false;
        }


    }


    private void tryToFire(Facts facts, Rule rule) {
        try {
            listeners.forEach(l -> l.beforeFire(rule));
            rule.execute(facts);
            listeners.forEach(l -> l.afterFire(rule));
        } catch (Exception e) {
            listeners.forEach(l -> l.executionFailed(rule, facts));
            logFailure(e, rule);
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
    @ToString
    public static class ForwardChainEngineParameters {
        private static final Predicate<Facts> DO_NOT_STOP = f -> false;
        private final int priorityThreshold;
        private final boolean considerNewFacts;
        private final boolean considerUpdatesFacts;
        private final Predicate<Facts> stopOnCondition;

        public static ForwardChainEngineParameters defaultParameters() {
            return new ForwardChainEngineParameters(Integer.MAX_VALUE - 1,
                    true,
                    true,
                    DO_NOT_STOP);
        }

    }
}
