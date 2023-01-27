package com.acepero13.research.ruleengine.model;

import com.acepero13.research.ruleengine.api.Rule;
import com.acepero13.research.ruleengine.model.rules.RuleBuilder;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuleBuilderTest {

    @Test
    void createSimpleRule() {
        Facts realFacts = new Facts();
        realFacts.put("test", 1);
        AtomicBoolean value = new AtomicBoolean(false);
        Rule rule = new RuleBuilder()
                .name("SimpleRule")
                .description("A description")
                .priority(50)
                .when(facts -> facts.get("test", Integer.class, -1).equals(1))
                .then(facts -> System.out.println("facts = " + facts))
                .build();

        assertEquals("SimpleRule", rule.name());
        assertEquals("A description", rule.description());
        assertEquals(50, rule.priority());

        assertTrue(rule.evaluates(realFacts));

    }

}