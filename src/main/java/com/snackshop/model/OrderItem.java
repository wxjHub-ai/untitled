package com.snackshop.model;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * 订单项实体类，对应数据库中的 order_items 表
 * 记录订单中具体包含的商品信息
 */
@Entity
@Table(name = "order_items")
public class OrderItem {
    /**
     * 订单项唯一标识符
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联的订单
     */
    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /**
     * 关联的商品
     */
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /**
     * 购买数量
     */
    private int quantity;

    /**
     * 下单时的快照价格
     */
    private BigDecimal price; // Snapshot price at time of order

    /**
     * 默认构造函数
     */
    public OrderItem() {}
    
    /**
     * 带参数的构造函数
     * @param order 订单对象
     * @param product 商品对象
     * @param quantity 数量
     * @param price 快照价格
     */
    public OrderItem(Order order, Product product, int quantity, BigDecimal price) {
        this.order = order;
        this.product = product;
        this.quantity = quantity;
        this.price = price;
    }

    /**
     * 获取订单项ID
     * @return ID
     */
    public Long getId() { return id; }

    /**
     * 设置订单项ID
     * @param id ID
     */
    public void setId(Long id) { this.id = id; }

    /**
     * 获取关联的订单
     * @return 订单对象
     */
    public Order getOrder() { return order; }

    /**
     * 设置关联的订单
     * @param order 订单对象
     */
    public void setOrder(Order order) { this.order = order; }

    /**
     * 获取关联的商品
     * @return 商品对象
     */
    public Product getProduct() { return product; }

    /**
     * 设置关联的商品
     * @param product 商品对象
     */
    public void setProduct(Product product) { this.product = product; }

    /**
     * 获取购买数量
     * @return 数量
     */
    public int getQuantity() { return quantity; }

    /**
     * 设置购买数量
     * @param quantity 数量
     */
    public void setQuantity(int quantity) { this.quantity = quantity; }

    /**
     * 获取快照价格
     * @return 价格
     */
    public BigDecimal getPrice() { return price; }

    /**
     * 设置快照价格
     * @param price 价格
     */
    public void setPrice(BigDecimal price) { this.price = price; }
}
