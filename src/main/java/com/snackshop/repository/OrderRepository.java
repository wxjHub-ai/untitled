package com.snackshop.repository;

import com.snackshop.model.Order;
import com.snackshop.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserOrderByOrderDateDesc(User user);
    List<Order> findAllByOrderByOrderDateDesc();
    
    // Filtering methods
    List<Order> findByStatusOrderByOrderDateDesc(String status);
    List<Order> findByOrderDateBetweenOrderByOrderDateDesc(java.time.LocalDateTime start, java.time.LocalDateTime end);
    List<Order> findByStatusAndOrderDateBetweenOrderByOrderDateDesc(String status, java.time.LocalDateTime start, java.time.LocalDateTime end);
}
