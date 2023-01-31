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
        String methodName = method.getName();
        switch (methodName) {
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
                break;
            case "equals":
                return objectEquals(args[0]);
            case "toString":
                return objectToString();
            case "hashCode":
                return objectHashCode();
            case "compareTo":
                return compareToMethod(args);
            default:
                break;
        }
        return null;
    }

    private Object compareToMethod(Object[] args) {
        Object anotherRule = args[0];
        if (isProxyClass(anotherRule)) {
            RuleProxy ruleProxy = toRuleProxy(anotherRule);
            return Integer.compare((ruleProxy).getPriority(), getPriority());
        } else if (anotherRule instanceof Rule) {
            return Integer.compare(((Rule) anotherRule).priority(), getPriority());
        }
        logger.debug("Could not compare rules, with arguments: {}", args);
        return -1;
    }

    private static RuleProxy toRuleProxy(Object anotherRule) {
        return (RuleProxy) Proxy.getInvocationHandler(anotherRule);
    }

    private static boolean isProxyClass(Object anotherRule) {
        return Proxy.isProxyClass(anotherRule.getClass());
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

    public boolean objectEquals(Object o) {
        if (this.target == o) return true;
        if (o == null ) return false;

        if(isProxyClass(o)) {
            RuleProxy ruleProxy = toRuleProxy(o);
            return isEqualToTarget(ruleProxy);
        } else if (o instanceof Rule) {
            return equalToRealRule((Rule) o);
        }
        return false;



    }

    private boolean equalToRealRule(Rule another) {
       if(getPriority() != another.priority()) return false;
       if(!name.equals(another.name())) return false;
       return description.equals(another.description());
    }

    private boolean isEqualToTarget(RuleProxy another) {
        if(this.getClass() != another.getClass()) return false;
        if (getPriority() != another.getPriority()) return false;
        if (!name.equals(another.name)) return false;
        return getDescription().equals(another.getDescription());
    }


    public int objectHashCode() {
        int result = getRuleName().hashCode();
        result = 31 * result + getPriority();
        result = 31 * result + getDescription().hashCode();
        return result;
    }


    public String objectToString() {
        return "Rule { name = " + getRuleName() + "; description = " + getDescription() + "}";
    }


}
