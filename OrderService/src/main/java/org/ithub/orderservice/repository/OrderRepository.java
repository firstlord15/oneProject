package org.ithub.orderservice.repository;

import org.ithub.orderservice.model.Order;
import org.ithub.orderservice.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    List<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);
}
