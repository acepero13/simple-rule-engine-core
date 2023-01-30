package com.acepero13.research.ruleengine.api;

import com.acepero13.research.ruleengine.model.Rules;

public interface RuleEngine {
    void fire(Rules rules, Facts facts);

    void fire(Facts facts);
    void register(RulesEventsListener listener);
    void unregister(RulesEventsListener listener);
    void unregisterAll();


}
