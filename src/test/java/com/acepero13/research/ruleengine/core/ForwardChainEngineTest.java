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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ForwardChainEngineTest {
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
    private static class IfFrogGreen {
        @Condition
        boolean when(@Fact("animal") Animal animal) {
            return animal instanceof Frog;
        }

        @Action
        void then(Facts facts) {
            facts.get("animal", Animal.class)
                    .ifPresent(a -> {
                        a.setColor(Color.GREEN);
                        facts.put("animal", a);
                    });
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
            facts.get("animal", Animal.class)
                    .ifPresent(a -> {
                        a.setColor(Color.BLUE);
                        facts.put("animal", a);
                    });
        }
    }


    @Data
    private abstract static class Animal {
        protected abstract EAT eats();

        private Color color;
        private String name;


    }

    private enum EAT {
        FLIES, ELSE;
    }

    private enum Color {
        GREEN, BLUE;
    }

    private static class Frog extends Animal {

        @Override
        protected EAT eats() {
            return EAT.FLIES;
        }
    }

    private static class Canary extends Animal {
        @Override
        protected EAT eats() {
            return EAT.ELSE;
        }
    }

}