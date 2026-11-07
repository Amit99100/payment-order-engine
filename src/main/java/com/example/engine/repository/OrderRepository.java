package com.example.engine.repository;
import com.example.engine.domain.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import java.util.Optional;
public interface OrderRepository extends JpaRepository<CustomerOrder, Long> {
    // Load items before the service transaction closes; open-in-view is disabled.
    @EntityGraph(attributePaths = "items")
    Optional<CustomerOrder> findWithItemsById(Long id);
}
