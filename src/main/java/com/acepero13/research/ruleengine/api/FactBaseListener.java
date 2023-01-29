package com.acepero13.research.ruleengine.api;

public interface FactBaseListener {
    <T> void newFactAdded(String name, T value);

    <T> void newFactReplaced(String name, T value);


}
