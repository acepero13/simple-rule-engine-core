package com.acepero13.research.ruleengine.model;

import com.acepero13.research.ruleengine.api.Rule;
import com.acepero13.research.ruleengine.model.rules.RuleBuilder;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RulesTest {

    @Test
    void sortByPriority() {
        Rules rules = Rules.of(createRule(1, "r1"), createRule(2, "r2"), createRule(3, "r3"));

        List<String> actual = new ArrayList<>();
        for (Rule rule : rules) {
            actual.add(rule.name());
        }
        assertEquals(List.of("r3", "r2", "r1"), actual);
    }


    private static Rule createRule(int prio, String name) {
        return new RuleBuilder()
                .name(name)
                .description("A description")
                .priority(prio)
                .when(facts -> facts.get("test", Integer.class, -1).equals(1))
                .then(facts -> System.out.println("facts = " + facts))
                .build();
    }
}