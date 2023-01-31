package com.acepero13.research.ruleengine.core;

import com.acepero13.research.ruleengine.api.Rule;
import com.acepero13.research.ruleengine.api.RuleEngine;
import com.acepero13.research.ruleengine.api.RulesEventsListener;

import com.acepero13.research.ruleengine.api.Facts;
import com.acepero13.research.ruleengine.core.engines.DefaultRuleEngine;
import com.acepero13.research.ruleengine.core.engines.EngineParameters;
import com.acepero13.research.ruleengine.model.FactsOperation;
import com.acepero13.research.ruleengine.model.InMemoryFacts;
import com.acepero13.research.ruleengine.model.Rules;
import com.acepero13.research.ruleengine.model.rules.RuleBuilder;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("SameParameterValue")
class DefaultRuleEngineTest implements RulesEventsListener {

    private final Facts facts = new InMemoryFacts();
    private final List<Rule> before = new ArrayList<>();
    private final List<Rule> after = new ArrayList<>();
    private final List<Rule> executionFailures = new ArrayList<>();
    private final List<Rule> evaluationFailures = new ArrayList<>();

    @Test
    void fireRules() {
        // create a rules engine and fire rules on known facts
        Rules rules = Rules.of(createRule());
        AtomicReference<FactsOperation> op = new AtomicReference<>();
        facts.registerNotificationsFor("test", op::set);
        facts.put("test", 1);
        System.out.println("rules = " + rules);

        RuleEngine rulesEngine = new DefaultRuleEngine(rules);

        rulesEngine.fire(facts);
        rulesEngine.unregisterAll();
        assertEquals(FactsOperation.CREATE, op.get());
        facts.unregisterAll();
    }

    @Test
    void defaultParameters() {


        DefaultRuleEngine rulesEngine = new DefaultRuleEngine(Rules.of(createRule()));
        assertEquals(EngineParameters.defaultParameters(), rulesEngine.getParameters());
    }

    @Test
    void beforeAndAfterRulesAreFired() {
        Rules rules = Rules.of(createAlwaysFire(50));
        facts.put("test", 0);

        RuleEngine rulesEngine = new DefaultRuleEngine(rules);
        rulesEngine.register(this);
        rulesEngine.fire(facts);
        assertEquals("alwaysFire", before.get(0).name());
        assertEquals("alwaysFire", after.get(0).name());
        rulesEngine.unregister(this);
    }

    @Test
    void firstRuleCannotFire() {
        Rules rules = Rules.of(createAlwaysFire(1), createRule());
        facts.put("test", 0);

        RuleEngine rulesEngine = new DefaultRuleEngine(rules);
        rulesEngine.register(this);
        rulesEngine.fire(facts);
        assertEquals("alwaysFire", before.get(0).name());
        assertEquals("alwaysFire", after.get(0).name());
    }

    @Test
    void registrationListeners() {
        Rules rules = Rules.of(createAlwaysFire(1), createRule());
        facts.put("test", 0);

        RuleEngine rulesEngine = new DefaultRuleEngine(rules);
        rulesEngine.register(this);
        rulesEngine.unregister(this);
        rulesEngine.fire(facts);
        assertEquals(0, before.size());
        assertEquals(0, after.size());
    }

    @Test
    void skipRuleBecauseThresholdIsTooLow() {
        Rules rules = Rules.of(createAlwaysFire(1), createRule());
        EngineParameters params = EngineParameters.builder()
                .priorityThreshold(3).build();
        facts.put("test", 0);

        RuleEngine rulesEngine = new DefaultRuleEngine(rules, params);
        rulesEngine.register(this);
        rulesEngine.fire(facts);
        assertRuleNotFired();
    }

    @Test
    void executesOnlyOneRuleByDefault() {
        Rules rules = Rules.of(createAlwaysFire(1), createAlwaysFire(2));

        facts.put("test", 0);

        RuleEngine rulesEngine = new DefaultRuleEngine(rules);
        rulesEngine.register(this);
        rulesEngine.fire(facts);
        assertEquals(1, before.size());
    }


    @Test
    void stopsExecutionOnFailureByDefault() {
        Rules rules = Rules.of(createFailRule(2), createAlwaysFire(1));

        facts.put("test", 0);

        RuleEngine rulesEngine = new DefaultRuleEngine(rules);
        rulesEngine.register(this);
        rulesEngine.fire(facts);
        assertEquals(1, before.size());
        assertEquals(1, executionFailures.size());

    }


    @Test
    void continuesExecutionAfterFailure() {
        Rules rules = Rules.of(createFailRule(3), createAlwaysFire(1));
        EngineParameters params = EngineParameters.builder()
                .skipOnFirstFailedRule(false).build();
        facts.put("test", 0);

        RuleEngine rulesEngine = new DefaultRuleEngine(rules, params);
        rulesEngine.register(this);
        rulesEngine.fire(facts);
        assertEquals(2, before.size());
    }

    @Test
    void continuesExecutionAfterFailureOnCondition() {
        Rules rules = Rules.of(createFailRuleOnCondition(2), createAlwaysFire(1));
        EngineParameters params = EngineParameters.builder()
                .skipOnFirstFailedRule(false).build();
        facts.put("test", 0);

        RuleEngine rulesEngine = new DefaultRuleEngine(rules, params);
        rulesEngine.register(this);
        rulesEngine.fire(facts);
        assertEquals(1, before.size());
        assertEquals(1, evaluationFailures.size());
    }


    @Test
    void stopsAfterFirstNonFiredRule() {
        Rules rules = Rules.of(createRule(), createAlwaysFire(1));
        EngineParameters params = EngineParameters.builder()
                .skipOnFirstNonTriggeredRule(true).build();
        facts.put("test", 0);

        RuleEngine rulesEngine = new DefaultRuleEngine(rules, params);
        rulesEngine.register(this);
        rulesEngine.fire(facts);
        assertRuleNotFired();
    }


    @Test
    void executesTwoRulesBecauseNoSkip() {
        Rules rules = Rules.of(createAlwaysFire(1), createAlwaysFire(2));

        EngineParameters params = EngineParameters.builder()
                .skipOnFirstAppliedRule(false).build();
        facts.put("test", 0);

        RuleEngine rulesEngine = new DefaultRuleEngine(rules, params);
        rulesEngine.register(this);
        rulesEngine.fire(facts);
        assertEquals(2, before.size());
    }

    @Test
    void skipRuleBecauseListenerSaidToSkip() {
        Rules rules = Rules.of(createAlwaysFire(1));
        facts.put("test", 0);

        RuleEngine rulesEngine = new DefaultRuleEngine(rules);
        rulesEngine.register(new RulesEventsListener() {
            @Override
            public void beforeFire(Rule rule) {
                before.add(rule);
            }

            @Override
            public void afterFire(Rule rule) {
                after.add(rule);
            }

            @Override
            public boolean shouldFire(Rule rule, Facts facts) {
                return false;
            }

            @Override
            public void evaluationFailed(Rule rule, Facts facts) {

            }

            @Override
            public void executionFailed(Rule rule, Facts facts) {

            }
        });
        rulesEngine.fire(facts);
        assertRuleNotFired();
    }

    private void assertRuleNotFired() {
        assertTrue(before.isEmpty());
        assertTrue(after.isEmpty());
    }

    private static Rule createRule() {
        return new RuleBuilder()
                .name("SimpleRule")
                .description("A description")
                .priority(50)
                .when(facts -> facts.get("test", Integer.class, -1).equals(1))
                .then(facts -> System.out.println("facts = " + facts))
                .build();
    }

    private Rule createFailRule(int prio) {
        return new RuleBuilder()
                .name("alwaysFire")
                .description("A description")
                .priority(prio)
                .when(facts -> true)
                .then(facts -> {
                    throw new RuntimeException("Failure");
                })
                .build();
    }

    private Rule createFailRuleOnCondition(int prio) {
        return new RuleBuilder()
                .name("failureOnCondition")
                .description("A description")
                .priority(prio)
                .when(facts -> {
                    throw new RuntimeException("Failure");
                })
                .then(facts -> System.out.println("facts = " + facts))
                .build();
    }

    private static Rule createAlwaysFire(int prio) {
        return new RuleBuilder()
                .name("alwaysFire")
                .description("A description")
                .priority(prio)
                .when(facts -> true)
                .then(facts -> System.out.println("facts = " + facts))
                .build();
    }

    @Override
    public void beforeFire(Rule rule) {
        before.add(rule);
    }

    @Override
    public void afterFire(Rule rule) {
        after.add(rule);
    }

    @Override
    public void evaluationFailed(Rule rule, Facts facts) {
        this.evaluationFailures.add(rule);
    }

    @Override
    public void executionFailed(Rule rule, Facts facts) {
        this.executionFailures.add(rule);
    }




}
