package com.acepero13.research.ruleengine.api;

public interface Rule extends Comparable<Rule> {
    String DEFAULT_NAME = "rule";
    int DEFAULT_PRIORITY = Integer.MAX_VALUE - 1;
    String DEFAULT_DESCRIPTION = "description";

    String name();

    String description();

    int priority();

    boolean evaluates(Facts facts);

    void execute(Facts facts) throws Exception;
}
