package com.acepero13.research.ruleengine.api;

@FunctionalInterface
public interface Condition {
    boolean evaluate(Facts facts);
}
