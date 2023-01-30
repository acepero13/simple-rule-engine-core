package com.acepero13.research.ruleengine.api;

import com.acepero13.research.ruleengine.model.InMemoryFacts;
import com.acepero13.research.ruleengine.model.Rules;

public interface RuleEngine {
    void fire(Rules rules, InMemoryFacts facts);

    void fire(InMemoryFacts facts);
    void register(RulesEventsListener listener);
    void unregister(RulesEventsListener listener);
    void unregisterAll();


}
