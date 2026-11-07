package com.example.engine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Application entry point.
 *
 * Scheduling is enabled because the outbox publisher is a background worker.
 * It periodically finds committed events and hands them to the configured
 * publisher. In production this adapter would publish to Kafka, RabbitMQ, etc.
 */
@SpringBootApplication
@EnableScheduling
public class PaymentOrderEngineApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaymentOrderEngineApplication.class, args);
    }
}
