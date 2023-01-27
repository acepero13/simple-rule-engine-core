package com.acepero13.research.ruleengine.model;

import api.Rule;

import java.util.*;

public class Rules implements Iterable<Rule> {
    private final Set<Rule> rules = new HashSet<>();

    public Rules(Rule... rules) {
        this.rules.addAll(List.of(rules));
    }

    public static Rules of(Rule... rule) {
        return new Rules(rule);
    }

    @Override
    public Iterator<Rule> iterator() {
        List<Rule> sortedList = new ArrayList<>(rules);
        Collections.sort(sortedList);
        return sortedList.iterator();
    }
}
