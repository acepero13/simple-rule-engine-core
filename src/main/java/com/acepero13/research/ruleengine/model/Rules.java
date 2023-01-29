package com.acepero13.research.ruleengine.model;

import com.acepero13.research.ruleengine.api.Rule;
import lombok.ToString;

import java.util.*;
@ToString
public class Rules implements Iterable<Rule> {
    private final Set<Rule> rules = new HashSet<>();

    public Rules(Rule... rules) {
        Objects.requireNonNull(rules, "Rules cannot be null");
        Objects.requireNonNull(this.rules, "Rule set cannot be null");
        this.rules.addAll(List.of(rules));
    }

    public static Rules of(Rule... rule) {
        return new Rules(rule);
    }

    public Rules register(Rule rule) {
        this.rules.add(rule);
        return this;
    }

    @Override
    public Iterator<Rule> iterator() {
        List<Rule> sortedList = new ArrayList<>(rules);
        Collections.sort(sortedList);
        return sortedList.iterator();
    }
}
