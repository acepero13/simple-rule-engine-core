package com.acepero13.research.ruleengine.model;

import com.acepero13.research.ruleengine.api.FactBaseListener;
import com.acepero13.research.ruleengine.api.Facts;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

@ToString
public class InMemoryFacts implements Facts {
    private static final Logger logger = LogManager.getLogger();
    private final Set<Fact<?>> facts = new HashSet<>();
    private final List<FactBaseListener> listeners = new ArrayList<>();
    private final Map<String, List<Consumer<FactsOperation>>> consumers = new HashMap<>();

    @Override
    public <T> void put(String name, T value) {
        Objects.requireNonNull(name, "fact name must not be null");
        Objects.requireNonNull(value, "fact value must not be null");
        Optional<Fact<?>> opFact = getFact(name);
        boolean valueExists = opFact.isPresent();
        Object oldValue = opFact.map(Fact::getValue).orElse(null);
        removeAnNotify(name, value, valueExists, oldValue);
        add(new Fact<>(name, value));
    }


    @Override
    public void registerNotificationsFor(String name, Consumer<FactsOperation> consumer) {
        List<Consumer<FactsOperation>> listOfConsumersForName = consumers.getOrDefault(name, new ArrayList<>());
        listOfConsumersForName.add(consumer);
        consumers.put(name, listOfConsumersForName);
    }

    private <T> void removeAnNotify(String name, T value, boolean valueExists, Object oldValue) {
        if (valueExists) {
            if (oldValue != null && !oldValue.equals(value)) {
                listeners.forEach(l -> l.newFactReplaced(name, value));
                logger.debug("Replacing fact {} with value: {}", name, value);
                notifyConsumers(name, FactsOperation.UPDATE);
            }

            remove(name);
        } else {
            notifyConsumers(name, FactsOperation.CREATE);
            logger.debug("Creating new fact: {} with value {}", name, value);
            listeners.forEach(l -> l.newFactAdded(name, value));
        }
    }

    private void notifyConsumers(String name, FactsOperation operation) {
        consumers.entrySet().stream()
                 .filter(e -> e.getKey().equals(name))
                 .flatMap(e -> e.getValue().stream())
                 .forEach(l -> l.accept(operation));
    }

    @Override
    public void register(FactBaseListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void unregister(FactBaseListener listener) {
        this.listeners.remove(listener);
    }

    @Override
    public void unregisterAll() {
        this.listeners.clear();
        this.consumers.clear();
    }

    @Override
    public void remove(String name) {
        facts.removeIf(f -> f.matchesName(name));
    }

    @Override
    public <T> void add(Fact<T> fact) {
        Objects.requireNonNull(fact, "Fact cannot be null");
        facts.add(fact);
    }


    @Override

    public <T> Optional<T> get(String factName) {
        Optional<Fact<?>> opFact = getFact(factName);
        if (opFact.isPresent()) {
            Fact<?> fact = opFact.get();
            return tryToCastFactValue(fact);
        }
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    private static <T> Optional<T> tryToCastFactValue(Fact<?> fact) {
        try {
            return Optional.ofNullable((T) fact.getValue());
        } catch (Exception e) {
            logger.error("Could not cast value. Check the types match", e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<Fact<?>> getFact(String factName) {
        return facts.stream().filter(f -> f.matchesName(factName)).findFirst();

    }

    @Override
    public Iterator<Fact<?>> iterator() {
        return facts.iterator();
    }

    @Override
    public int total() {
        return facts.size();
    }

    @Override
    public <T> T get(String factName, Class<T> type, T defaultValue) {
        return getFact(factName).map(Fact::getValue).filter(type::isInstance).map(type::cast).orElse(defaultValue);
    }

    @Override
    public <T> Optional<T> get(String factName, Class<T> type) {
        return getFact(factName).map(Fact::getValue).filter(type::isInstance).map(type::cast);
    }

    @Override
    public <T> void updatesIfExists(String factName, Class<T> type, UnaryOperator<T> func) {
        getFact(factName)
                .map(Fact::getValue)
                .filter(type::isInstance).map(type::cast)
                .ifPresent(v -> put(factName, func.apply(v)));
    }

    @Override
    public boolean exists(String factName) {
        return facts.stream().anyMatch(f -> f.matchesName(factName));
    }

    @Override
    public Map<String, Object> asMap() {
        Map<String, Object> map = new HashMap<>();
        facts.forEach(f -> map.put(f.name(), f.getValue()));
        return map;
    }

}
