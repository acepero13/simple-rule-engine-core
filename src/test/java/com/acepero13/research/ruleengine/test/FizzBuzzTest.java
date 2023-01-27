package com.acepero13.research.ruleengine.test;

import api.Rule;
import api.RuleEngine;
import com.acepero13.research.ruleengine.core.DefaultRuleEngine;
import com.acepero13.research.ruleengine.model.Facts;
import com.acepero13.research.ruleengine.model.Rules;
import com.acepero13.research.ruleengine.model.rules.RuleBuilder;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FizzBuzzTest {
    private static final List<String> expected = List.of("1", "2", "3", "4", "fizz", "6", "buzz", "8", "9", "fizz", "11", "12", "13", "buzz", "fizz", "16", "17", "18", "19", "fizz", "buzz", "22", "23", "24", "fizz", "26", "27", "buzz", "29", "fizz", "31", "32", "33", "34", "fizzbuzz", "36", "37", "38", "39", "fizz", "41", "buzz", "43", "44", "fizz", "46", "47", "48", "buzz", "fizz", "51", "52", "53", "54", "fizz", "buzz", "57", "58", "59", "fizz", "61", "62", "buzz", "64", "fizz", "66", "67", "68", "69", "fizzbuzz", "71", "72", "73", "74", "fizz", "76", "buzz", "78", "79", "fizz", "81", "82", "83", "buzz", "fizz", "86", "87", "88", "89", "fizz", "buzz", "92", "93", "94", "fizz", "96", "97", "buzz", "99", "fizz");

    public static final String NUMBER_FACT = "number";

    public final List<String> result = new ArrayList<>();

    @Test
    void fizzbuzz() {
        Rules rules = createRules();

        RuleEngine engine = new DefaultRuleEngine(rules);
        Facts facts = new Facts();

        for (int i = 1; i <= 100; i++) {
            facts.put(NUMBER_FACT, i);
            engine.fire(facts);
        }

        assertEquals(expected, result);


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
                .name("buzz")
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
