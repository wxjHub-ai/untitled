package com.snackshop.model;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 购物车项目实体类，对应数据库中的 cart_items 表
 */
@Entity
@Table(name = "cart_items")
public class CartItem {
    /**
     * 购物车项目唯一标识符
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
     * 关联的商品
     */
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /**
     * 商品数量
     */
    private int quantity;

    /**
     * 添加到购物车的时间
     */
    private LocalDateTime addedAt = LocalDateTime.now();

    /**
     * 默认构造函数
     */
    public CartItem() {}

    /**
     * 获取购物车项目ID
     * @return 项目ID
     */
    public Long getId() { return id; }

    /**
     * 设置购物车项目ID
     * @param id 项目ID
     */
    public void setId(Long id) { this.id = id; }

    /**
     * 获取关联的用户
     * @return 用户对象
     */
    public User getUser() { return user; }

    /**
     * 设置关联的用户
     * @param user 用户对象
     */
    public void setUser(User user) { this.user = user; }

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
     * 获取商品数量
     * @return 数量
     */
    public int getQuantity() { return quantity; }

    /**
     * 设置商品数量
     * @param quantity 数量
     */
    public void setQuantity(int quantity) { this.quantity = quantity; }

    /**
     * 获取添加时间
     * @return 添加时间
     */
    public LocalDateTime getAddedAt() { return addedAt; }

    /**
     * 设置添加时间
     * @param addedAt 添加时间
     */
    public void setAddedAt(LocalDateTime addedAt) { this.addedAt = addedAt; }

    /**
     * 计算该项目的总价（单价 * 数量）
     * @return 总价
     */
    public java.math.BigDecimal getTotalPrice() {
        return product.getPrice().multiply(new java.math.BigDecimal(quantity));
    }
}