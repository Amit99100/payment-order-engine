package com.example.engine.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Creates the event topic when the application starts if it does not exist.
 * One partition keeps local ordering simple. Increase partitions in a real
 * deployment after choosing a message key and considering ordering guarantees.
 */
@Configuration
public class KafkaTopicConfig {
    @Bean
    public NewTopic paymentOrderTopic(@Value("${app.kafka.topic}") String topicName) {
        return TopicBuilder.name(topicName).partitions(1).replicas(1).build();
    }
}
