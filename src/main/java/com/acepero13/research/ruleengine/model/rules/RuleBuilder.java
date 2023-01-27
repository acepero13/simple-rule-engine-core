package com.acepero13.research.ruleengine.model.rules;

import com.acepero13.research.ruleengine.core.proxy.RuleProxy;
import com.acepero13.research.ruleengine.api.Action;
import com.acepero13.research.ruleengine.api.Condition;
import com.acepero13.research.ruleengine.api.Rule;

import java.util.ArrayList;
import java.util.List;

public class RuleBuilder {
    private String name;
    private String description;
    private int priority = BasicRule.DEFAULT_PRIORITY;
    private Condition condition;
    private final List<Action> actions = new ArrayList<>();

    public static Rule of(Object rule) {
        return RuleProxy.asRule(rule);
    }

    public RuleBuilder name(String name) {
        this.name = name;
        return this;
    }

    public RuleBuilder description(String description) {
        this.description = description;
        return this;
    }

    public RuleBuilder priority(int priority) {
        this.priority = priority;
        return this;
    }

    public RuleBuilder when(Condition condition) {
        this.condition = condition;
        return this;
    }

    public RuleBuilder then(Action action) {
        this.actions.add(action);
        return this;
    }

    public Rule build() {
        return new DefaultRule(name, description, priority, condition, actions);
    }
}
