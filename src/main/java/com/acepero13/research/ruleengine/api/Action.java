package com.acepero13.research.ruleengine.api;

import com.acepero13.research.ruleengine.model.Facts;

@FunctionalInterface
public interface Action {
    void execute(Facts facts) throws Exception;
}
