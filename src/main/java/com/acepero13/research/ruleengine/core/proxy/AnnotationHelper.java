package com.acepero13.research.ruleengine.core.proxy;

import com.acepero13.research.ruleengine.annotations.Fact;
import com.acepero13.research.ruleengine.annotations.Rule;
import com.acepero13.research.ruleengine.api.Facts;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AnnotationHelper {

    public static Optional<Rule> getRuleAnnotation(Object target) {
        return Arrays.stream(target.getClass().getAnnotations())
                .filter(a -> a.annotationType().equals(Rule.class))
                .map(a -> (Rule) a)
                .findFirst();
    }

    public static Optional<Method> getMethodForCalling(Object target, Class<? extends Annotation> conditionAnnotation) {
        return Arrays.stream(target.getClass().getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(conditionAnnotation))
                .findFirst();

    }

    public static List<Object> getConditionFrom(Method method, Object[] facts) throws RuntimeException {
        if (facts.length != 1) {
            throw new IllegalStateException("Method cannot receive more than one facts base");
        }
        if (!(facts[0] instanceof Facts)) {
            throw new IllegalStateException("Argument must be a fact base");
        }
        Facts myFacts = (Facts) facts[0];

        Parameter[] methodParameters = method.getParameters();
        List<Object> parameters = Arrays.stream(methodParameters)
                .filter(p -> p.isAnnotationPresent(Fact.class))
                .map(p -> (Fact) p.getAnnotations()[0])
                .map(Fact::value)
                .map(myFacts::get)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        if (parameters.size() != methodParameters.length) {
            throw new ArgumentMismatchException(buildErrorMessage(myFacts, methodParameters));
        }
        return parameters;


    }

    private static String buildErrorMessage(Facts myFacts, Parameter[] methodParameters) {
        var paramFacts = Arrays.stream(methodParameters)
                .filter(p -> p.isAnnotationPresent(Fact.class))
                .map(p -> (Fact) p.getAnnotations()[0])
                .map(Fact::value)
                .collect(Collectors.toList());
        String missingFacts = paramFacts.stream()
                .filter(f -> !myFacts.exists(f))
                .collect(Collectors.joining(", "));
        return "Could not find some facts in the fact base. Missing facts: " + missingFacts + ". Fact base: " + myFacts;
    }

    public static class ArgumentMismatchException extends RuntimeException {

        public ArgumentMismatchException(String msg) {
            super(msg);
        }
    }
}
