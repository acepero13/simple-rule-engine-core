package com.acepero13.research.ruleengine.model;

import lombok.ToString;

import java.util.*;

@ToString
public class Facts implements Iterable<Fact<?>> {
    private final Set<Fact<?>> facts = new HashSet<>();

    public <T> void put(String name, T value) {
        Objects.requireNonNull(name, "fact name must not be null");
        Objects.requireNonNull(value, "fact value must not be null");
        Optional<Fact<?>> opFact = getFact(name);
        if (opFact.isPresent()) {
            remove(name);
        }

        add(new Fact<T>(name, value));
    }

    private void remove(String name) {
        facts.removeIf(f -> f.matchesName(name));
    }

    public <T> void add(Fact<T> fact) {
        Objects.requireNonNull(fact, "Fact cannot be null");
        facts.add(fact);
    }


    public <T> Optional<T> get(String factName) {
        return getFact(factName).map(f -> (T) f.getValue());
    }

    private Optional<Fact<?>> getFact(String factName) {
        return facts.stream()
                .filter(f -> f.matchesName(factName))
                .findFirst();

    }

    @Override
    public Iterator<Fact<?>> iterator() {
        return facts.iterator();
    }

    public int total() {
        return facts.size();
    }

    public <T> T get(String factName, Class<T> type, T defaultValue) {
        return getFact(factName)
                .map(Fact::getValue)
                .filter(type::isInstance)
                .map(type::cast)
                .orElse(defaultValue);
    }
}
