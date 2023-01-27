package com.acepero13.research.ruleengine.test;

import com.acepero13.research.ruleengine.annotations.Action;
import com.acepero13.research.ruleengine.annotations.Condition;
import com.acepero13.research.ruleengine.annotations.Fact;
import com.acepero13.research.ruleengine.annotations.Rule;
import com.acepero13.research.ruleengine.api.RuleEngine;
import com.acepero13.research.ruleengine.core.DefaultRuleEngine;
import com.acepero13.research.ruleengine.model.Facts;
import com.acepero13.research.ruleengine.model.Rules;
import com.acepero13.research.ruleengine.model.rules.RuleBuilder;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.acepero13.research.ruleengine.test.Constants.EXPECTED_FIZZ_SOLUTION;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FizzBuzzWithAnnotationsTests {
    public static final String NUMBER_FACT = "number";
    private final List<String> result = new ArrayList<>();

    @Test
    public void fizzBuzz() {

        Rules rules = new Rules(
                RuleBuilder.of(new Buzz(result)),
                RuleBuilder.of(new Fizz(result)),
                RuleBuilder.of(new FizzBuzz(result))

        ).register(RuleBuilder.of(new NonFizzBuzz(result)));
        RuleEngine engine = new DefaultRuleEngine(rules);
        Facts facts = new Facts();

        for (int i = 1; i <= 100; i++) {
            facts.put(NUMBER_FACT, i);
            engine.fire(facts);
        }

        assertEquals(EXPECTED_FIZZ_SOLUTION, result);

    }

    @Rule(name = "buzz", priority = 2)
    private static class Buzz {
        private final List<String> results;

        private Buzz(List<String> results) {
            this.results = results;
        }

        @Condition
        public boolean condition(@Fact("number") int number) {
            return number % 7 == 0;
        }

        @Action
        public void action(@Fact("number") int number) {
            results.add("buzz");
        }
    }


    @Rule(name = "fizz", priority = 2)
    private static class Fizz {
        private final List<String> results;

        private Fizz(List<String> results) {
            this.results = results;
        }

        @Condition
        public boolean condition(@Fact("number") int number) {
            return number % 5 == 0;
        }

        @Action
        public void action() {
            results.add("fizz");
        }
    }

    @Rule(name = "fizzbuzz", priority = 3)
    private static class FizzBuzz {
        private final List<String> results;

        private FizzBuzz(List<String> results) {
            this.results = results;
        }

        @Condition
        public boolean condition(@Fact("number") int number) {
            return number % 5 == 0 && number % 7 == 0;
        }

        @Action
        public void action() {
            results.add("fizzbuzz");
        }
    }

    @Rule(name = "Non FizzBuzz", priority = 1)
    private static class NonFizzBuzz {
        private final List<String> results;

        private NonFizzBuzz(List<String> results) {
            this.results = results;
        }

        @Condition
        public boolean condition(@Fact("number") int number) {
            return number % 5 != 0 && number % 7 != 0;
        }

        @Action
        public void action(@Fact("number") int number) {
            results.add(String.valueOf(number));
        }
    }
}
