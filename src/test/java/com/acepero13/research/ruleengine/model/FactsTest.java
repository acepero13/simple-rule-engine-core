package com.acepero13.research.ruleengine.model;

import com.acepero13.research.ruleengine.api.Facts;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FactsTest {

    private final Facts facts = new InMemoryFacts();

    @Test
    void addAndRetrieveFacts() {
        facts.put("fact1", true);
        facts.put("fact2", "str");
        facts.put("fact3", 1);

        assertEquals("str", facts.get("fact2").orElse(""));
        assertEquals(1, facts.get("fact3").orElse(1));
        boolean value = (boolean) facts.get("fact1").orElse(false);
        assertTrue(value);
    }

    @Test
    void addingTheSameFactReplacesIt() {
        facts.put("f1", "str");
        facts.put("f1", "another");

        assertEquals("another", facts.get("f1").orElse(""));
        assertEquals(1, facts.total());
    }


}