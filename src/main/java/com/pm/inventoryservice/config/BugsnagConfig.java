package com.pm.inventoryservice.config;

import com.bugsnag.Bugsnag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BugsnagConfig {

    @Bean
    public Bugsnag bugsnag(
            @Value("${bugsnag.api-key}") String apiKey) {

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "BUGSNAG_API_KEY is not configured"
            );
        }

        Bugsnag bugsnag = new Bugsnag(apiKey);

        bugsnag.setAppVersion("0.0.1");
        bugsnag.setReleaseStage("development");

        return bugsnag;
    }
}
