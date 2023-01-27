package com.acepero13.research.ruleengine.model;

@FunctionalInterface
public interface Action {
    void execute(Facts facts) throws Exception;
}
