package com.acepero13.research.ruleengine.model.rules;

import com.acepero13.research.ruleengine.api.Rule;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public abstract class BasicRule implements Rule {
    public static final int DEFAULT_PRIORITY = Integer.MAX_VALUE;
    private final String name;
    private final String description;
    private final int priority;

    public BasicRule(String name, String description, int priority) {
        this.name = name;
        this.description = description;
        this.priority = priority;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public int priority() {
        return priority;
    }

    @Override
    public String description() {
        return description;
    }

    @Override
    public int compareTo(Rule o) {
        return Integer.compare(o.priority(), priority);
    }

    @Override
    public String toString() {
        return name + " Rule";
    }
}
