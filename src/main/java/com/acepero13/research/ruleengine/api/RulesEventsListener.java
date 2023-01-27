package com.acepero13.research.ruleengine.api;

import com.acepero13.research.ruleengine.model.Facts;

public interface RulesEventsListener {
    void beforeFire(Rule rule);
    void afterFire(Rule rule);

    default boolean shouldFire(Rule rule, Facts facts) {
        return true;
    }

}
