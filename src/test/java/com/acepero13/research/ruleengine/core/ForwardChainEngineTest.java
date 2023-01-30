package com.acepero13.research.ruleengine.core;

import com.acepero13.research.ruleengine.annotations.Action;
import com.acepero13.research.ruleengine.annotations.Condition;
import com.acepero13.research.ruleengine.annotations.Fact;
import com.acepero13.research.ruleengine.annotations.Rule;
import com.acepero13.research.ruleengine.api.Facts;
import com.acepero13.research.ruleengine.api.RuleEngine;
import com.acepero13.research.ruleengine.model.InMemoryFacts;
import com.acepero13.research.ruleengine.model.Rules;
import com.acepero13.research.ruleengine.model.rules.RuleBuilder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ForwardChainEngineTest {
    private static final Logger logger = LogManager.getLogger();

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

        Animal inferred = facts.get("animal", Animal.class, null);
        assertEquals(Color.GREEN, inferred.getColor());


    }

    @Rule(name = "croaks and eat flies -> It is a frog")
    @ToString
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