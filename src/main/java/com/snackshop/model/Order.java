package com.snackshop.model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单实体类，对应数据库中的 orders 表
 * 记录用户下单的信息
 */
@Entity
@Table(name = "orders")
public class Order {
    // 订单唯一编号
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 关联的用户 (多对一关系，多个订单可以对应一个用户)
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 下单时间
    private LocalDateTime orderDate;

    // 订单状态 (待处理 PENDING, 已发货 SHIPPED, 已送达 DELIVERED)
    private String status;

    // 订单总金额
    private BigDecimal totalAmount;

    // 关联的地址簿地址
    @ManyToOne
    @JoinColumn(name = "address_id")
    private UserAddress address;

    // 下单时的快照地址
    @Column(nullable = false)
    private String deliveryAddress;

    public UserAddress getAddress() { return address; }
    public void setAddress(UserAddress address) { this.address = address; }

    // 订单包含的商品项 (一对多关系，一个订单包含多个商品项)
    // cascade = CascadeType.ALL 表示对订单的操作会级联影响到订单项
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    // 默认构造函数
    public Order() {}

    // 属性的 Getter 和 Setter 方法
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
    
    // 向订单中添加商品项的便捷方法
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }
}
