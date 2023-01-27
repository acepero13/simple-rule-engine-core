package com.acepero13.research.ruleengine.model;

import lombok.ToString;

import java.util.Objects;

@ToString
public class Fact<T> {

    private final String name;
    private final T value;

    public Fact(String name, T value) {
        this.name = name;
        this.value = value;
    }

    public String name() {
        return name;
    }

    public T getValue() {
        return value;
    }

    public boolean matchesName(String name) {
        Objects.requireNonNull(name, "Fact name should not be null");
        return name.equals(this.name);
    }
}
