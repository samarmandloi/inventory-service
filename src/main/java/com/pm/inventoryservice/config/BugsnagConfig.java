package com.pm.inventoryservice.config;

import com.bugsnag.Bugsnag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BugsnagConfig {

    @Bean
    @Conditional(BugsnagApiKeyCondition.class)
    public Bugsnag bugsnag(
            @Value("${bugsnag.api-key}") String apiKey,
            @Value("${bugsnag.app-version:0.0.1}") String appVersion,
            @Value("${bugsnag.release-stage:development}") String releaseStage) {

        Bugsnag bugsnag = new Bugsnag(apiKey);
        bugsnag.setAppVersion(appVersion);
        bugsnag.setReleaseStage(releaseStage);

        return bugsnag;
    }
}
