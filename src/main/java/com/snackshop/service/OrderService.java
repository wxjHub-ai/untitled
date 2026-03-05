package com.snackshop.service;

import com.snackshop.model.Order;
import com.snackshop.model.OrderItem;
import com.snackshop.model.User;
import com.snackshop.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

import com.snackshop.model.Product;
import com.snackshop.repository.ProductRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Transactional
    public void createOrder(User user, List<OrderItem> items, java.math.BigDecimal totalAmount, String deliveryAddress) {
        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("已支付");
        order.setDeliveryAddress(deliveryAddress);
        
        java.math.BigDecimal serverCalculatedTotal = java.math.BigDecimal.ZERO;
        
        for (OrderItem item : items) {
            Product product = item.getProduct();
            // Refetch product to get latest stock and correct price
            Product dbProduct = productRepository.findById(product.getId()).orElseThrow();
            
            if (dbProduct.getStock() < item.getQuantity()) {
                throw new RuntimeException("商品 " + dbProduct.getName() + " 库存不足! 剩余: " + dbProduct.getStock());
            }

            if (item.getQuantity() <= 0) {
                throw new RuntimeException("商品数量必须大于 0");
            }
            
            // Deduct stock
            dbProduct.setStock(dbProduct.getStock() - item.getQuantity());
            productRepository.save(dbProduct);
            
            // Use server-side price to prevent fraud
            item.setPrice(dbProduct.getPrice());
            item.setProduct(dbProduct); 
            order.addItem(item);
            
            java.math.BigDecimal itemTotal = dbProduct.getPrice().multiply(new java.math.BigDecimal(item.getQuantity()));
            serverCalculatedTotal = serverCalculatedTotal.add(itemTotal);
        }
        
        order.setTotalAmount(serverCalculatedTotal);
        orderRepository.save(order);
    }

    public List<Order> getOrdersByMerchant(User merchant) {
        // Find all orders that contain at least one product from this merchant
        return orderRepository.findAll().stream()
                .filter(order -> order.getItems().stream()
                        .anyMatch(item -> item.getProduct().getStore() != null && 
                                          item.getProduct().getStore().getOwner().getId().equals(merchant.getId())))
                .collect(java.util.stream.Collectors.toList());
    }

    public java.math.BigDecimal getMerchantRevenue(User merchant) {
        return orderRepository.findAll().stream()
                .flatMap(order -> order.getItems().stream())
                .filter(item -> item.getProduct().getStore() != null && 
                                item.getProduct().getStore().getOwner().getId().equals(merchant.getId()))
                .map(item -> item.getPrice().multiply(new java.math.BigDecimal(item.getQuantity())))
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    }
    
    public List<Order> getOrdersByUser(User user) {
        return orderRepository.findByUserOrderByOrderDateDesc(user);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByOrderDateDesc();
    }

    public void updateOrderStatus(Long id, String status) {
        Order order = orderRepository.findById(id).orElseThrow();
        order.setStatus(status);
        orderRepository.save(order);
    }

    public java.math.BigDecimal getTotalRevenue() {
        return orderRepository.findAll().stream()
                .map(Order::getTotalAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    }

    @Transactional
    public void deleteOrder(Long id) {
        Order order = orderRepository.findById(id).orElseThrow();
        if ("已取消".equals(order.getStatus())) {
            orderRepository.delete(order);
        } else {
            throw new RuntimeException("只能删除已取消的订单！");
        }
    }

    public List<Order> searchOrders(String status, java.time.LocalDate startDate, java.time.LocalDate endDate, User merchant) {
        List<Order> orders;
        
        java.time.LocalDateTime start = (startDate != null) ? startDate.atStartOfDay() : null;
        java.time.LocalDateTime end = (endDate != null) ? endDate.atTime(23, 59, 59) : null;

        if (status != null && !status.isEmpty() && !status.equals("全部") && start != null && end != null) {
            orders = orderRepository.findByStatusAndOrderDateBetweenOrderByOrderDateDesc(status, start, end);
        } else if (status != null && !status.isEmpty() && !status.equals("全部")) {
            orders = orderRepository.findByStatusOrderByOrderDateDesc(status);
        } else if (start != null && end != null) {
            orders = orderRepository.findByOrderDateBetweenOrderByOrderDateDesc(start, end);
        } else {
            orders = orderRepository.findAllByOrderByOrderDateDesc();
        }

        // If merchant is provided, filter the results
        if (merchant != null) {
            return orders.stream()
                .filter(order -> order.getItems().stream()
                    .anyMatch(item -> item.getProduct().getStore() != null && 
                                      item.getProduct().getStore().getOwner().getId().equals(merchant.getId())))
                .collect(java.util.stream.Collectors.toList());
        }
        
        return orders;
    }
}
