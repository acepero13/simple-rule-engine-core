package com.acepero13.research.ruleengine.core.proxy;

import com.acepero13.research.ruleengine.annotations.Action;
import com.acepero13.research.ruleengine.annotations.Condition;
import com.acepero13.research.ruleengine.annotations.Fact;
import com.acepero13.research.ruleengine.annotations.Rule;
import com.acepero13.research.ruleengine.api.Facts;
import com.acepero13.research.ruleengine.model.InMemoryFacts;
import com.acepero13.research.ruleengine.model.rules.RuleBuilder;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RuleProxyTest {
    private final List<Integer> numbers = new ArrayList<>();
    @Test
    void toProxiesAreEqual(){
        var rule = RuleBuilder.of(new TestRule());
        var another = RuleBuilder.of(new TestRule());
        assertEquals(rule, another);
    }

    @Test
    void cannotBeEqualToNull(){
        var rule = RuleBuilder.of(new TestRule());
        assertNotEquals(null, rule);
    }

    @Test
    void proxyIsEqualToRule(){
        //@Rule(name = "fizz", priority = 2, description = "desc")
        var rule = RuleBuilder.of(new TestRule());
        var another = new RuleBuilder()
                .name("fizz")
                .priority(2)
                .description("desc")
                .build();
        assertEquals(rule, another);
    }

    @Test void asRule(){
        var another = new RuleBuilder()
                .name("fizz")
                .priority(2)
                .description("desc")
                .build();
        var rule = RuleProxy.asRule(another);
        assertEquals("fizz", rule.name());
    }

    @Test void description(){
        var rule = RuleBuilder.of(new TestRule());
        assertEquals("desc", rule.description());
    }

    @Test void priority(){
        var rule = RuleBuilder.of(new TestRule());
        assertEquals(2, rule.priority());
    }



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
        Facts facts = new InMemoryFacts();
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
        Facts facts = new InMemoryFacts();
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

    @Rule(name = "fizz", priority = 2, description = "desc")
    private class RuleWithParams {
        @Condition
        public boolean condition(@Fact("number") Integer number) {
            return number % 7 == 0;
        }

        @Action
        public void action(@Fact("number") int number, Facts facts) {
            System.out.println(" fizz");
            numbers.add(number);
        }
    }


    @com.acepero13.research.ruleengine.annotations.Rule(name = "another", priority = 3, description = "desc")
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