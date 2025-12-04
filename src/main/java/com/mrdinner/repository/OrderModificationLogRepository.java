package com.mrdinner.repository;

import com.mrdinner.entity.Order;
import com.mrdinner.entity.OrderModificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderModificationLogRepository extends JpaRepository<OrderModificationLog, Long> {
    List<OrderModificationLog> findByOrderOrderByModifiedAtDesc(Order order);
}

