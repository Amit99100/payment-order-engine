package com.example.engine.service;

import com.example.engine.domain.OutboxEvent;
import com.example.engine.repository.OutboxRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OutboxService {
    private final OutboxRepository repository;
    public OutboxService(OutboxRepository repository) { this.repository = repository; }

    @Transactional
    public void add(String aggregateType, Long aggregateId, String eventType, String payload) {
        repository.save(new OutboxEvent(aggregateType, aggregateId, eventType, payload));
    }
}
