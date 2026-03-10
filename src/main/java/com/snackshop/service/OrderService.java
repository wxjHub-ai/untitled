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

/**
 * 订单服务类，处理订单创建、库存扣减、收益计算及订单管理等业务。
 */
@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    /**
     * 创建新订单。
     * 包括验证库存、扣减库存、计算总价并保存订单。
     * 
     * @param user 下单用户
     * @param items 订单项列表
     * @param totalAmount 订单总金额
     * @param deliveryAddress 收货地址
     */
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
            // 重新查询商品信息以确保获取最新的库存和价格
            Product dbProduct = productRepository.findById(product.getId()).orElseThrow();
            
            // 检查库存是否充足
            if (dbProduct.getStock() < item.getQuantity()) {
                throw new RuntimeException("商品 " + dbProduct.getName() + " 库存不足! 剩余: " + dbProduct.getStock());
            }

            // 检查下单数量是否合法
            if (item.getQuantity() <= 0) {
                throw new RuntimeException("商品数量必须大于 0");
            }
            
            // 扣减库存并保存
            dbProduct.setStock(dbProduct.getStock() - item.getQuantity());
            productRepository.save(dbProduct);
            
            // 设置订单项价格和商品信息，计算总额
            item.setPrice(dbProduct.getPrice());
            item.setProduct(dbProduct); 
            order.addItem(item);
            
            java.math.BigDecimal itemTotal = dbProduct.getPrice().multiply(new java.math.BigDecimal(item.getQuantity()));
            serverCalculatedTotal = serverCalculatedTotal.add(itemTotal);
        }
        
        // 设置服务器端计算的总额并保存订单
        order.setTotalAmount(serverCalculatedTotal);
        orderRepository.save(order);
    }

    /**
     * 获取属于特定商家的所有订单。
     * 
     * @param merchant 商家对象
     * @return 订单列表
     */
    public List<Order> getOrdersByMerchant(User merchant) {
        // 筛选出包含该商家商品的订单
        return orderRepository.findAll().stream()
                .filter(order -> order.getItems().stream()
                        .anyMatch(item -> item.getProduct().getStore() != null && 
                                          item.getProduct().getStore().getOwner().getId().equals(merchant.getId())))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 计算特定商家的累计总收益。
     * 
     * @param merchant 商家对象
     * @return 总收益金额
     */
    public java.math.BigDecimal getMerchantRevenue(User merchant) {
        return orderRepository.findAll().stream()
                .flatMap(order -> order.getItems().stream())
                .filter(item -> item.getProduct().getStore() != null && 
                                item.getProduct().getStore().getOwner().getId().equals(merchant.getId()))
                .map(item -> item.getPrice().multiply(new java.math.BigDecimal(item.getQuantity())))
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    }
    
    /**
     * 获取指定用户的所有订单。
     * 
     * @param user 用户对象
     * @return 订单列表，按订单日期倒序排列
     */
    public List<Order> getOrdersByUser(User user) {
        return orderRepository.findByUserOrderByOrderDateDesc(user);
    }

    /**
     * 获取系统中所有订单。
     * 
     * @return 订单列表，按日期倒序排列
     */
    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByOrderDateDesc();
    }

    /**
     * 更新订单状态。
     * 
     * @param id 订单ID
     * @param status 新状态
     */
    public void updateOrderStatus(Long id, String status) {
        Order order = orderRepository.findById(id).orElseThrow();
        order.setStatus(status);
        orderRepository.save(order);
    }

    /**
     * 获取全站总收益。
     * 
     * @return 全站总收益金额
     */
    public java.math.BigDecimal getTotalRevenue() {
        return orderRepository.findAll().stream()
                .map(Order::getTotalAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    }

    /**
     * 删除订单。
     * 仅允许删除“已取消”状态的订单。
     * 
     * @param id 订单ID
     */
    @Transactional
    public void deleteOrder(Long id) {
        Order order = orderRepository.findById(id).orElseThrow();
        if ("已取消".equals(order.getStatus())) {
            orderRepository.delete(order);
        } else {
            throw new RuntimeException("只能删除已取消的订单！");
        }
    }

    /**
     * 多条件搜索订单。
     * 
     * @param status 状态（可选）
     * @param username 用户名关键字（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param merchant 商家（可选，用于过滤该商家的订单）
     * @return 匹配的订单列表
     */
    public List<Order> searchOrders(String status, String username, java.time.LocalDate startDate, java.time.LocalDate endDate, User merchant) {
        List<Order> orders = orderRepository.findAllByOrderByOrderDateDesc();
        
        java.time.LocalDateTime start = (startDate != null) ? startDate.atStartOfDay() : null;
        java.time.LocalDateTime end = (endDate != null) ? endDate.atTime(23, 59, 59) : null;

        return orders.stream()
            .filter(order -> (status == null || status.isEmpty() || status.equals("全部") || order.getStatus().equals(status)))
            .filter(order -> (username == null || username.isEmpty() || order.getUser().getUsername().toLowerCase().contains(username.toLowerCase())))
            .filter(order -> (start == null || !order.getOrderDate().isBefore(start)))
            .filter(order -> (end == null || !order.getOrderDate().isAfter(end)))
            .filter(order -> (merchant == null || order.getItems().stream()
                .anyMatch(item -> item.getProduct().getStore() != null && 
                                  item.getProduct().getStore().getOwner().getId().equals(merchant.getId()))))
            .collect(java.util.stream.Collectors.toList());
    }
}
