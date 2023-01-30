package com.acepero13.research.ruleengine.api;

@FunctionalInterface
public interface Action {
    @SuppressWarnings("RedundantThrows")
    void execute(Facts facts) throws Exception;
}
