package com.nayonikaeyecare.api.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.testcontainers.containers.MongoDBContainer;
import org.springframework.data.mongodb.core.MongoTemplate;
import com.mongodb.client.MongoClients;

@TestConfiguration
public class TestConfig {

    @Bean
    public MongoDBContainer mongoDBContainer() {
        MongoDBContainer container = new MongoDBContainer("mongo:4.4.2");
        container.start();
        return container;
    }

    @Bean
    @Primary
    public MongoTemplate mongoTemplate(MongoDBContainer mongoDBContainer) {
        MongoTemplate template = new MongoTemplate(
                MongoClients.create(mongoDBContainer.getReplicaSetUrl()),
                "test");
        // Enable bulk operations
        template.setWriteConcern(com.mongodb.WriteConcern.MAJORITY);
        return template;
    }
}