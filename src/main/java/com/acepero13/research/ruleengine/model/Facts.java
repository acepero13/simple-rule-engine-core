package com.acepero13.research.ruleengine.model;

import com.acepero13.research.ruleengine.api.FactBaseListener;
import lombok.ToString;

import java.util.*;
import java.util.function.Consumer;

@ToString
public class Facts implements Iterable<Fact<?>> {
    private final Set<Fact<?>> facts = new HashSet<>();
    private final List<FactBaseListener> listeners = new ArrayList<>();
    private final Map<String, List<Consumer<FactsOperation>>> consumers = new HashMap<>();

    public <T> void put(String name, T value) {
        Objects.requireNonNull(name, "fact name must not be null");
        Objects.requireNonNull(value, "fact value must not be null");
        Optional<Fact<?>> opFact = getFact(name);
        boolean valueExists = opFact.isPresent();
        Object oldValue = opFact.map(Fact::getValue).orElse(null);
        removeAnNotify(name, value, valueExists, oldValue);
        add(new Fact<T>(name, value));
    }


    public void registerNotificationsFor(String name, Consumer<FactsOperation> consumer) {
        List<Consumer<FactsOperation>> listOfConsumersForName = consumers.getOrDefault(name, new ArrayList<>());
        listOfConsumersForName.add(consumer);
        consumers.put(name, listOfConsumersForName);
    }

    private <T> void removeAnNotify(String name, T value, boolean valueExists, Object oldValue) {
        if (valueExists) {
            if (oldValue != null && !oldValue.equals(value)) {
                listeners.forEach(l -> l.newFactReplaced(name, value));
                notifyConsumers(name, FactsOperation.UPDATE);
            }

            remove(name);
        } else {
            notifyConsumers(name, FactsOperation.CREATE);
            listeners.forEach(l -> l.newFactAdded(name, value));
        }
    }

    private void notifyConsumers(String name, FactsOperation operation) {
        consumers.entrySet().stream()
                .filter(e -> e.getKey().equals(name))
                .flatMap(e -> e.getValue().stream())
                .forEach(l -> l.accept(operation));
    }

    public void register(FactBaseListener listener) {
        this.listeners.add(listener);
    }

    public void unregister(FactBaseListener listener) {
        this.listeners.remove(listener);
    }

    public void unregisterAll() {
        this.listeners.clear();
        this.consumers.clear();
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
        return facts.stream().filter(f -> f.matchesName(factName)).findFirst();

    }

    @Override
    public Iterator<Fact<?>> iterator() {
        return facts.iterator();
    }

    public int total() {
        return facts.size();
    }

    public <T> T get(String factName, Class<T> type, T defaultValue) {
        return getFact(factName).map(Fact::getValue).filter(type::isInstance).map(type::cast).orElse(defaultValue);
    }

    public <T> Optional<T> get(String factName, Class<T> type) {
        return getFact(factName).map(Fact::getValue).filter(type::isInstance).map(type::cast);
    }
}
