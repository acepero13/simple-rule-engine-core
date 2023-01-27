package com.acepero13.research.ruleengine.model;

@FunctionalInterface
public interface Condition {
    boolean evaluate(Facts facts);
}
