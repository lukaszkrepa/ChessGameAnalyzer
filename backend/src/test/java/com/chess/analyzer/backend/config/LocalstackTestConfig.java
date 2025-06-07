package com.chess.analyzer.backend.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class LocalstackTestConfig {

    @Bean
    public LocalStackContainer localStackContainer() {
        LocalStackContainer container = new LocalStackContainer(DockerImageName.parse("localstack/localstack:latest"))
                .withServices(LocalStackContainer.Service.DYNAMODB);
        container.start(); // make sure it's started BEFORE Spring loads the context
        return container;
    }
}
