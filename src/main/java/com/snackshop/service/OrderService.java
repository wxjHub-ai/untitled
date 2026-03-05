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
    public void createOrder(User user, List<OrderItem> items, java.math.BigDecimal totalAmount) {
        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("已支付");
        
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
    
    public List<Order> getOrdersByUser(User user) {
        return orderRepository.findByUser(user);
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
}
