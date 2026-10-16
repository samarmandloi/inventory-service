package com.pm.inventoryservice.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class BugsnagApiKeyCondition implements Condition {

    @Override
    public boolean matches(
            ConditionContext context,
            AnnotatedTypeMetadata metadata) {

        String apiKey = context.getEnvironment()
                .getProperty("bugsnag.api-key");

        return apiKey != null && !apiKey.isBlank();
    }
}
