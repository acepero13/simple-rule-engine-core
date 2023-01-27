package com.acepero13.research.ruleengine.test;

import com.acepero13.research.ruleengine.api.Action;
import com.acepero13.research.ruleengine.api.Condition;
import com.acepero13.research.ruleengine.api.Rule;
import com.acepero13.research.ruleengine.api.RuleEngine;
import com.acepero13.research.ruleengine.core.DefaultRuleEngine;
import com.acepero13.research.ruleengine.model.Facts;
import com.acepero13.research.ruleengine.model.Rules;
import com.acepero13.research.ruleengine.model.rules.RuleBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TemperatureTest {

    public static final String TEMP_FACT = "temperature";

    @Test
    void testTemperature() {
        Facts facts = new Facts();
        facts.put(TEMP_FACT, 30);
        Rule airConditionRule = new RuleBuilder()
                .name("air conditioning")
                .when(itIsHot())
                .then(coolDown())
                .build();

        RuleEngine engine = new DefaultRuleEngine(Rules.of(airConditionRule));
        engine.fire(facts);

        var newTemperature = facts.get(TEMP_FACT, Integer.class, -1);
        assertEquals(29, newTemperature);

    }

    private Action coolDown() {
        return facts -> facts.get(TEMP_FACT, Integer.class)
                .map(t -> t - 1)
                .ifPresent(t -> facts.put(TEMP_FACT, t));
    }

    private Condition itIsHot() {
        return facts -> facts.get("temperature", Integer.class, -1000) > 25;
    }
}
