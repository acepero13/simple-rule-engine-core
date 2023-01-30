package com.acepero13.research.ruleengine.core;

import com.acepero13.research.ruleengine.annotations.Action;
import com.acepero13.research.ruleengine.annotations.Condition;
import com.acepero13.research.ruleengine.annotations.Fact;
import com.acepero13.research.ruleengine.api.Rule;
import com.acepero13.research.ruleengine.api.RuleEngine;
import com.acepero13.research.ruleengine.model.Facts;
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
        Rule IfAThenB = new RuleBuilder()
                .name("A -> B")
                .when(facts -> facts.get("A").isPresent())
                .then(facts -> facts.put("B", "B"))
                .build();

        Rule IfBThenC = new RuleBuilder()
                .name("B -> C")
                .when(facts -> facts.get("B").isPresent())
                .then(facts -> facts.put("C", "C"))
                .build();

        Rules rules = new Rules(IfBThenC, IfAThenB);
        Facts facts = new Facts();
        facts.put("A", "A");

        RuleEngine engine = new ForwardChainEngine(rules);
        engine.fire(facts);

        String actual = facts.get("C", String.class, "");
        assertEquals("C", actual);

    }

    @Test
    void inferFrogColor() {

        Rules rules = new Rules(
                RuleBuilder.of(new IsAFrog()),
                RuleBuilder.of(new IsACanary()),
                RuleBuilder.of(new IfFrogGreen()),
                RuleBuilder.of(new IfCanaryBlue())
        );
        Facts facts = new Facts();
        facts.put("croaks", true);
        facts.put("eats", EAT.FLIES);


        RuleEngine engine = new ForwardChainEngine(rules);
        engine.fire(facts);

        Animal inferred = facts.get("animal", Animal.class, null);
        assertEquals(Color.GREEN, inferred.getColor());


    }

    @com.acepero13.research.ruleengine.annotations.Rule(name = "It is a Frog")
    @ToString
    @EqualsAndHashCode
    private static class IsAFrog {
        @Condition
        boolean when(@Fact("croaks") boolean croaks, @Fact("eats") EAT eats) {
            return croaks && eats == EAT.FLIES;
        }

        @Action
        void then(Facts facts) {
            facts.put("animal", new Frog());
        }
    }

    @com.acepero13.research.ruleengine.annotations.Rule(name = "It is a canary")
    private static class IsACanary {
        @Condition
        boolean when(@Fact("chirps") boolean chirps, @Fact("sings") boolean signs) {
            return chirps && signs;
        }

        @Action
        void then(Facts facts) {
            facts.put("animal", new Canary());
        }
    }

    @com.acepero13.research.ruleengine.annotations.Rule(name = "If frog then is green")
    @EqualsAndHashCode
    private static class IfFrogGreen {
        @Condition
        public boolean when(@Fact("animal") Animal animal) {
            return animal instanceof Frog;
        }

        @Action
        void then(Facts facts) {
            facts.updatesIfExists("animal", Animal.class, (Animal a) -> a.setColor(Color.GREEN));
        }
    }

    @com.acepero13.research.ruleengine.annotations.Rule(name = "If canary then is blue")
    private static class IfCanaryBlue {
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
        FLIES, ELSE;
    }

    private enum Color {
        GREEN, BLUE;
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