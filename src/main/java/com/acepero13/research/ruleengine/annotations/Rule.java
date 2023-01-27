package com.acepero13.research.ruleengine.annotations;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Rule {
    String name() default com.acepero13.research.ruleengine.api.Rule.DEFAULT_NAME;

    int priority() default com.acepero13.research.ruleengine.api.Rule.DEFAULT_PRIORITY;

    String description() default com.acepero13.research.ruleengine.api.Rule.DEFAULT_DESCRIPTION;
}
