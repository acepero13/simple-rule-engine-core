package com.acepero13.research.ruleengine.api;

import com.acepero13.research.ruleengine.model.Fact;
import com.acepero13.research.ruleengine.model.FactsOperation;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public interface Facts extends Iterable<Fact<?>> {
    <T> void put(String name, T value);

    void registerNotificationsFor(String name, Consumer<FactsOperation> consumer);

    void register(FactBaseListener listener);

    void unregister(FactBaseListener listener);

    void unregisterAll();

    void remove(String name);

    <T> void add(Fact<T> fact);

    <T> Optional<T> get(String factName);

    Optional<Fact<?>> getFact(String factName);

    int total();

    <T> T get(String factName, Class<T> type, T defaultValue);

    <T> Optional<T> get(String factName, Class<T> type);

    <T> void updatesIfExists(String factName, Class<T> type, UnaryOperator<T> func);

    boolean exists(String factName);

    String toString();

    Map<String, Object> asMap();
}
