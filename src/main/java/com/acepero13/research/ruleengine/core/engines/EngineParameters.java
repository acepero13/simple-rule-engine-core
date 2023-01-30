package com.acepero13.research.ruleengine.core.engines;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Builder
@Getter
@EqualsAndHashCode
public class EngineParameters {

    private final int priorityThreshold;
    private final boolean skipOnFirstAppliedRule;
    private final boolean skipOnFirstFailedRule;
    private final boolean skipOnFirstNonTriggeredRule;

    public static EngineParameters defaultParameters() {
        return EngineParameters.builder()
                .skipOnFirstAppliedRule(true)
                .skipOnFirstNonTriggeredRule(false)
                .skipOnFirstFailedRule(true)
                .build();
    }
}
