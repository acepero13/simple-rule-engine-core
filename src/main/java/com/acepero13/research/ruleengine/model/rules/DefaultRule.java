package com.acepero13.research.ruleengine.model.rules;

import com.acepero13.research.ruleengine.api.Action;
import com.acepero13.research.ruleengine.api.Condition;
import com.acepero13.research.ruleengine.api.Facts;

import java.util.List;

final class DefaultRule extends BasicRule {
    private final Condition condition;
    private final List<Action> actions;

    public DefaultRule(String name, String description, int priority, Condition condition, List<Action> actions) {
        super(name, description, priority);
        this.condition = condition;
        this.actions = actions;
    }

    @Override
    public boolean evaluates(Facts facts) {
        return condition.evaluate(facts);
    }

    @Override
    public void execute(Facts facts) throws Exception {
        for (Action action : actions) {
            action.execute(facts);
        }
    }


}
