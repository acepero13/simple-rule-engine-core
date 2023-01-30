package com.acepero13.research.ruleengine.api;

public interface RulesEventsListener {
    void beforeFire(Rule rule);
    void afterFire(Rule rule);

    default boolean shouldFire(Rule rule, Facts facts) {
        return true;
    }

}
