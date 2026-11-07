package com.example.engine.repository;
import com.example.engine.domain.IdempotencyRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface IdempotencyRepository extends JpaRepository<IdempotencyRecord, Long> {
    Optional<IdempotencyRecord> findByKey(String key);
}
