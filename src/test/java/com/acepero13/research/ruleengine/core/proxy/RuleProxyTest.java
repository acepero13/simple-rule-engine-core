package com.acepero13.research.ruleengine.core.proxy;

import com.acepero13.research.ruleengine.annotations.Action;
import com.acepero13.research.ruleengine.annotations.Condition;
import com.acepero13.research.ruleengine.annotations.Fact;
import com.acepero13.research.ruleengine.annotations.Rule;
import com.acepero13.research.ruleengine.model.Facts;
import com.acepero13.research.ruleengine.model.rules.RuleBuilder;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuleProxyTest {
    private final List<Integer> numbers = new ArrayList<>();

    @Test
    void loadRule() {
        var rule = RuleBuilder.of(new TestRule());
        assertEquals("fizz", rule.name());
        assertEquals(2, rule.priority());
        assertEquals("desc", rule.description());
    }

    @Test
    void evaluate() {
        var rule = RuleBuilder.of(new TestRule());
        Facts facts = new Facts();
        facts.put("number", 7);
        assertTrue(rule.evaluates(facts));
    }

    @Test void compareWithAnohterRule(){
        var rule = RuleBuilder.of(new TestRule()); // prio = 2
        var another = RuleBuilder.of(new AnotherRule()); // prio = 3

        assertEquals(1, rule.compareTo(another));
    }

    @Test void compareWithRealRule(){
        var rule = RuleBuilder.of(new TestRule()); // prio = 2
        var another = new RuleBuilder()
                .name("test")
                .priority(1)
                .build();

        assertEquals(-1, rule.compareTo(another));
    }


    @Test
    void executesAction() throws Exception {
        var rule = RuleBuilder.of(new TestRule());
        Facts facts = new Facts();
        facts.put("number", 7);
        rule.execute(facts);
        assertEquals(7, numbers.get(0));
    }

    @Rule(name = "fizz", priority = 2, description = "desc")
    private class TestRule {
        @Condition
        public boolean condition(@Fact("number") Integer number) {
            return number % 7 == 0;
        }

        @Action
        public void action(@Fact("number") int number) {
            System.out.println(" fizz");
            numbers.add(number);
        }
    }

    @Rule(name = "another", priority = 3, description = "desc")
    private class AnotherRule {
        @Condition
        public boolean condition(@Fact("number") Integer number) {
            return number % 5 == 0;
        }

        @Action
        public void action(@Fact("number") int number) {
            System.out.println(" fizz");
            numbers.add(number);
        }
    }

}