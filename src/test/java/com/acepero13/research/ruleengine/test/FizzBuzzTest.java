package com.acepero13.research.ruleengine.test;

import com.acepero13.research.ruleengine.api.Facts;
import com.acepero13.research.ruleengine.api.Rule;
import com.acepero13.research.ruleengine.api.RuleEngine;
import com.acepero13.research.ruleengine.core.engines.DefaultRuleEngine;
import com.acepero13.research.ruleengine.model.InMemoryFacts;
import com.acepero13.research.ruleengine.model.Rules;
import com.acepero13.research.ruleengine.model.rules.RuleBuilder;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.acepero13.research.ruleengine.test.Constants.EXPECTED_FIZZ_SOLUTION;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FizzBuzzTest {


    public static final String NUMBER_FACT = "number";

    public final List<String> result = new ArrayList<>();

    @Test
    void fizzbuzz() {
        Rules rules = createRules();

        RuleEngine engine = new DefaultRuleEngine(rules);
        Facts facts = new InMemoryFacts();

        for (int i = 1; i <= 100; i++) {
            facts.put(NUMBER_FACT, i);
            engine.fire(facts);
        }

        assertEquals(EXPECTED_FIZZ_SOLUTION, result);


    }

    private Rules createRules() {
        Rule fizzBuzz = new RuleBuilder()
                .name("fizz buzz")
                .priority(3)
                .when(facts -> facts.get("number")
                        .map(Integer.class::cast)
                        .filter(n -> n % 5 == 0 && n % 7 == 0)
                        .isPresent())
                .then(facts -> result.add("fizzbuzz"))
                .build();

        Rule fizz = new RuleBuilder()
                .name("fizz")
                .priority(2)
                .when(facts -> facts.get(NUMBER_FACT)
                        .map(Integer.class::cast)
                        .filter(n -> n % 7 == 0)
                        .isPresent())
                .then(facts -> result.add("buzz"))
                .build();

        Rule buzz = new RuleBuilder()
                .name("buzz")
                .priority(2)
                .when(facts -> facts.get(NUMBER_FACT)
                        .map(Integer.class::cast)
                        .filter(n -> n % 5 == 0)
                        .isPresent())
                .then(facts -> result.add("fizz"))
                .build();

        Rule nonFizzBuzz = new RuleBuilder()
                .name("neither fizz nor buzz")
                .priority(1)
                .when(facts -> facts.get(NUMBER_FACT)
                        .map(Integer.class::cast)
                        .filter(n -> n % 5 != 0 && n % 7 != 0)
                        .isPresent())
                .then(facts -> result.add(String.valueOf(facts.get(NUMBER_FACT, Integer.class, -1))))
                .build();

        return new Rules(fizzBuzz, fizz, buzz, nonFizzBuzz);
    }
}
