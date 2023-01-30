package com.acepero13.research.ruleengine.core.proxy;

import com.acepero13.research.ruleengine.annotations.Action;
import com.acepero13.research.ruleengine.annotations.Condition;
import com.acepero13.research.ruleengine.api.Facts;
import com.acepero13.research.ruleengine.api.Rule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Optional;

public class RuleProxy implements InvocationHandler {
    private static final Logger logger = LogManager.getLogger();
    private final Object target;
    private String name;
    private int priority = -1;
    private String description;

    public RuleProxy(Object target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        String name = method.getName();
        switch (name) {
            case "name":
                return getRuleName();
            case "priority":
                return getPriority();
            case "description":
                return getDescription();
            case "evaluates":
                return evaluate(args);
            case "execute":
                execute(args);
            case "equals":
                return objectEquals(target, args[0]);
            case "toString":
                return objectToString(target);
            case "hashCode":
                return objectHashCode(target);
            case "compareTo":
                return compareToMethod(args);
        }
        return null;
    }

    private Object compareToMethod(Object[] args) {
        Object anotherRule = args[0];
        if (Proxy.isProxyClass(anotherRule.getClass())) {
            RuleProxy ruleProxy = (RuleProxy) Proxy.getInvocationHandler(anotherRule);
            return Integer.compare((ruleProxy).getPriority(), getPriority());
        } else if (anotherRule instanceof Rule) {
            return Integer.compare(((Rule) anotherRule).priority(), getPriority());
        }
        logger.debug("Could not compare rules, with arguments: {}", args);
        return -1;
    }

    private void execute(Object[] args) {
        Optional<Method> opMethod = AnnotationHelper.getMethodForCalling(target, Action.class);
        opMethod.ifPresent(m -> tryToExecuteAction(m, args));

    }

    private void tryToExecuteAction(Method method, Object[] facts) {
        method.setAccessible(true);
        try {
            tryToExecute(method, facts);
            return;
        } catch (AnnotationHelper.ArgumentMismatchException e) {
            logger.warn(e.getMessage());
        }

        if (facts.length != 1 || !(facts[0] instanceof Facts)) {
            logger.error("Cannot execute rule. There are unknown arguments");
            return;
        }
        try {
            method.invoke(target, facts[0]);
        } catch (IllegalAccessException | InvocationTargetException | IllegalStateException e) {
            logger.error("Error invoking method: execute", e);
        } catch (Exception e) {
            logger.error("ERROR happened", e);
        }
    }

    private boolean evaluate(Object[] args) {
        Optional<Method> opMethod = AnnotationHelper.getMethodForCalling(target, Condition.class);
        return opMethod.map(m -> tryToExecute(m, args))
                .filter(Boolean.class::isInstance)
                .map(Boolean.class::cast)
                .orElse(false);
    }

    private Object tryToExecute(Method method, Object[] facts) {
        method.setAccessible(true);
        try {
            List<Object> arguments = AnnotationHelper.getConditionFrom(method, facts);
            return method.invoke(target, arguments.toArray());
        } catch (IllegalAccessException | InvocationTargetException | IllegalStateException e) {
            throw new RuntimeException(e);
        }
    }

    private String getRuleName() {
        if (this.name == null) {
            fetchMetaData();
        }
        return this.name;
    }

    private void fetchMetaData() {
        AnnotationHelper.getRuleAnnotation(target)
                .ifPresent(a -> {
                    this.name = a.name();
                    this.priority = a.priority();
                    this.description = a.description();
                });
    }

    public static Rule asRule(final Object rule) {
        if (rule instanceof Rule) {
            return (Rule) rule;
        }
        return (Rule) Proxy.newProxyInstance(
                Rule.class.getClassLoader(),
                new Class[]{Rule.class, Comparable.class},
                new RuleProxy(rule)
        );
    }

    public int getPriority() {
        if (priority == -1) {
            fetchMetaData();
        }
        return priority;
    }

    public String getDescription() {
        if (description == null) {
            fetchMetaData();
        }
        return description;
    }

    public String objectClassName(Object obj) {
        return obj.getClass().getName();
    }

    public int objectHashCode(Object obj) {
        return System.identityHashCode(obj);
    }

    public boolean objectEquals(Object obj, Object other) {
        return obj == other;
    }

    public String objectToString(Object obj) {
        return objectClassName(obj) + '@' + Integer.toHexString(objectHashCode(obj));
    }


}
