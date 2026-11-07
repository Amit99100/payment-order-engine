package com.example.engine.repository;

import com.example.engine.domain.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySku(String sku);

    /**
     * PESSIMISTIC_WRITE generates a database row lock (SELECT ... FOR UPDATE).
     * Concurrent transactions trying to reserve the same SKU wait their turn.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);
}
