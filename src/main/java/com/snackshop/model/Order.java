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
    /**
     * 订单唯一编号
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联的用户
     */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 下单时间
     */
    private LocalDateTime orderDate;

    /**
     * 订单状态 (待处理 PENDING, 已发货 SHIPPED, 已送达 DELIVERED)
     */
    private String status;

    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;

    /**
     * 关联的地址簿地址
     */
    @ManyToOne
    @JoinColumn(name = "address_id")
    private UserAddress address;

    /**
     * 下单时的快照地址
     */
    @Column(nullable = false)
    private String deliveryAddress;

    /**
     * 订单包含的商品项
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    /**
     * 默认构造函数
     */
    public Order() {}

    /**
     * 获取订单地址
     * @return 地址对象
     */
    public UserAddress getAddress() { return address; }

    /**
     * 设置订单地址
     * @param address 地址对象
     */
    public void setAddress(UserAddress address) { this.address = address; }

    /**
     * 获取订单ID
     * @return 订单ID
     */
    public Long getId() { return id; }

    /**
     * 设置订单ID
     * @param id 订单ID
     */
    public void setId(Long id) { this.id = id; }

    /**
     * 获取下单用户
     * @return 用户对象
     */
    public User getUser() { return user; }

    /**
     * 设置下单用户
     * @param user 用户对象
     */
    public void setUser(User user) { this.user = user; }

    /**
     * 获取下单时间
     * @return 下单时间
     */
    public LocalDateTime getOrderDate() { return orderDate; }

    /**
     * 设置下单时间
     * @param orderDate 下单时间
     */
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }

    /**
     * 获取订单状态
     * @return 订单状态
     */
    public String getStatus() { return status; }

    /**
     * 设置订单状态
     * @param status 订单状态
     */
    public void setStatus(String status) { this.status = status; }

    /**
     * 获取总金额
     * @return 总金额
     */
    public BigDecimal getTotalAmount() { return totalAmount; }

    /**
     * 设置总金额
     * @param totalAmount 总金额
     */
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    /**
     * 获取快照配送地址
     * @return 配送地址
     */
    public String getDeliveryAddress() { return deliveryAddress; }

    /**
     * 设置快照配送地址
     * @param deliveryAddress 配送地址
     */
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    /**
     * 获取订单项目列表
     * @return 订单项目列表
     */
    public List<OrderItem> getItems() { return items; }

    /**
     * 设置订单项目列表
     * @param items 订单项目列表
     */
    public void setItems(List<OrderItem> items) { this.items = items; }
    
    /**
     * 向订单中添加商品项的便捷方法
     * @param item 订单项目
     */
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }
}
