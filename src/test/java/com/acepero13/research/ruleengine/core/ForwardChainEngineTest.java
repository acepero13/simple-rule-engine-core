package com.acepero13.research.ruleengine.core;

import com.acepero13.research.ruleengine.annotations.Action;
import com.acepero13.research.ruleengine.annotations.Condition;
import com.acepero13.research.ruleengine.annotations.Fact;
import com.acepero13.research.ruleengine.annotations.Rule;
import com.acepero13.research.ruleengine.api.Facts;
import com.acepero13.research.ruleengine.api.RuleEngine;
import com.acepero13.research.ruleengine.api.RulesEventsListener;
import com.acepero13.research.ruleengine.core.engines.ForwardChainEngine;
import com.acepero13.research.ruleengine.model.InMemoryFacts;
import com.acepero13.research.ruleengine.model.Rules;
import com.acepero13.research.ruleengine.model.rules.RuleBuilder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ForwardChainEngineTest implements RulesEventsListener {

    private final List<com.acepero13.research.ruleengine.api.Rule> executionFailures = new ArrayList<>();

    @Test
    void testFailingExecution() {
        var IfAThenB = new RuleBuilder()
                .name("A -> B")
                .when(facts -> facts.get("A").isPresent())
                .then(facts -> {
                    throw new RuntimeException("Error");
                })
                .build();

        Rules rules = new Rules(IfAThenB);
        Facts facts = new InMemoryFacts();
        facts.put("A", "A");
        RuleEngine engine = new ForwardChainEngine(rules);
        engine.register(this);
        engine.fire(facts);
        assertEquals(1, executionFailures.size());
        engine.unregister(this);

    }

    @Test
    void testListeners() {
        var IfAThenB = new RuleBuilder()
                .name("A -> B")
                .when(facts -> facts.get("A").isPresent())
                .then(facts -> facts.put("B", "B"))
                .build();

        Rules rules = new Rules(IfAThenB);
        Facts facts = new InMemoryFacts();
        facts.put("A", "A");

        RuleEngine engine = new ForwardChainEngine(rules);
        var before = new ArrayList<String>();
        var after = new ArrayList<String>();
        RulesEventsListener listener = new RulesEventsListener() {
            @Override
            public void beforeFire(com.acepero13.research.ruleengine.api.Rule rule) {
                before.add(rule.name());
            }

            @Override
            public void afterFire(com.acepero13.research.ruleengine.api.Rule rule) {
                after.add(rule.name());
            }

            @Override
            public void evaluationFailed(com.acepero13.research.ruleengine.api.Rule rule, Facts facts) {

            }

            @Override
            public void executionFailed(com.acepero13.research.ruleengine.api.Rule rule, Facts facts) {

            }
        };
        engine.register(listener);
        engine.fire(facts);

        engine.unregister(listener);
        assertEquals(2, before.size());
        assertEquals(2, after.size());
    }

    @Test
    void testParameters() {
        ForwardChainEngine engine = new ForwardChainEngine(new Rules(RuleBuilder.of(new IsAFrogRule())));
        assertEquals(ForwardChainEngine.ForwardChainEngineParameters.defaultParameters(), engine.getParameters());
    }


    @Test
    void stopOnCondition() {
        var IfAThenB = new RuleBuilder()
                .name("A -> B")
                .when(facts -> facts.get("A").isPresent())
                .then(facts -> facts.put("B", "B"))
                .build();

        var IfBThenC = new RuleBuilder()
                .name("B -> C")
                .when(facts -> facts.get("B").isPresent())
                .then(facts -> {
                    facts.put(UUID.randomUUID().toString(), "C");
                    facts.put("C", "C");
                }) // Endless loop
                .build();

        Rules rules = new Rules(IfBThenC, IfAThenB);
        Facts facts = new InMemoryFacts();
        facts.put("A", "A");

        var params = ForwardChainEngine.ForwardChainEngineParameters
                .builder()
                .stopOnCondition(f -> f.exists("C"))
                .considerUpdatesFacts(true)
                .build();

        RuleEngine engine = new ForwardChainEngine(rules, params);
        engine.fire(facts);

        String actual = facts.get("C", String.class, "");
        assertEquals("C", actual);

    }

    @Test
    void inference() {
        var IfAThenB = new RuleBuilder()
                .name("A -> B")
                .when(facts -> facts.get("A").isPresent())
                .then(facts -> facts.put("B", "B"))
                .build();

        var IfBThenC = new RuleBuilder()
                .name("B -> C")
                .when(facts -> facts.get("B").isPresent())
                .then(facts -> facts.put("C", "C"))
                .build();

        Rules rules = new Rules(IfBThenC, IfAThenB);
        Facts facts = new InMemoryFacts();
        facts.put("A", "A");

        RuleEngine engine = new ForwardChainEngine(rules);
        engine.fire(facts);
        engine.unregisterAll();

        String actual = facts.get("C", String.class, "");
        assertEquals("C", actual);

    }

    @Test
    void inferFrogColor() {

        Rules rules = new Rules(
                RuleBuilder.of(new IsAFrogRule()),
                RuleBuilder.of(new IsACanaryRule()),
                RuleBuilder.of(new FrogIsGreenRule()),
                RuleBuilder.of(new CanaryIsBlueRule())
        );
        Facts facts = new InMemoryFacts();
        facts.put("croaks", true);
        facts.put("eats", EAT.FLIES);


        RuleEngine engine = new ForwardChainEngine(rules);
        engine.fire(facts);

        Color actual = facts.get("animal", Animal.class)
             .map(Animal::getColor)
             .orElse(null);

        assertEquals(Color.GREEN, actual);


    }

    @Override
    public void beforeFire(com.acepero13.research.ruleengine.api.Rule rule) {

    }

    @Override
    public void afterFire(com.acepero13.research.ruleengine.api.Rule rule) {

    }

    @Override
    public void evaluationFailed(com.acepero13.research.ruleengine.api.Rule rule, Facts facts) {
    }

    @Override
    public void executionFailed(com.acepero13.research.ruleengine.api.Rule rule, Facts facts) {
        this.executionFailures.add(rule);
    }

    @Rule(name = "croaks and eat flies -> It is a frog")
    @EqualsAndHashCode
    private static class IsAFrogRule {
        @Condition
        boolean when(@Fact("croaks") boolean croaks, @Fact("eats") EAT eats) {
            return croaks && eats == EAT.FLIES;
        }

        @Action
        void then(Facts facts) {
            facts.put("animal", new Frog());
        }

        @Override
        public String toString() {
            return "It is a Frog Rule";
        }
    }

    @Rule(name = "sings -> Canary", description = "If it sings, it is a canary")
    private static class IsACanaryRule {
        @Condition
        boolean when(@Fact("chirps") boolean chirps, @Fact("sings") boolean sings) {
            return chirps && sings;
        }

        @Action
        void then(Facts facts) {
            facts.put("animal", new Canary());
        }

        @Override
        public String toString() {
            return "It is a Canary Rule";
        }
    }

    @Rule(name = "Frog -> color: Green", description = "If it is a frog, the color is green")
    @EqualsAndHashCode
    private static class FrogIsGreenRule {
        @Condition
        public boolean when(@Fact("animal") Animal animal) {
            return animal instanceof Frog;
        }

        @Action
        void then(Facts facts) {
            facts.updatesIfExists("animal", Animal.class, (Animal a) -> a.setColor(Color.GREEN));
        }

        @Override
        public String toString() {
            return "Frog is green Rule";
        }
    }

    @Rule(name = "Canary -> color: Blue", description = "If it is a canary, the color is blue")
    private static class CanaryIsBlueRule {
        @Condition
        boolean when(@Fact("animal") Animal animal) {
            return animal instanceof Canary;
        }

        @Action
        void then(Facts facts) {
            facts.updatesIfExists("animal", Animal.class, a -> a.setColor(Color.BLUE));
        }

        @Override
        public String toString() {
            return "Canary is blue Rule";
        }
    }


    @Data
    private abstract static class Animal {
        protected abstract EAT eats();

        private Color color;
        private String name;

        public Animal setColor(Color color) {
            this.color = color;
            return this;
        }


    }

    private enum EAT {
        FLIES, ELSE
    }

    private enum Color {
        GREEN, BLUE
    }

    @ToString
    private static class Frog extends Animal {

        @Override
        protected EAT eats() {
            return EAT.FLIES;
        }
    }

    @ToString
    private static class Canary extends Animal {
        @Override
        protected EAT eats() {
            return EAT.ELSE;
        }
    }

}
