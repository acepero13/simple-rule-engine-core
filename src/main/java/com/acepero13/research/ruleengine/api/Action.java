package com.acepero13.research.ruleengine.api;

@FunctionalInterface
public interface Action {
    void execute(Facts facts) throws Exception;
}
