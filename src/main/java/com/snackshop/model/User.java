package com.snackshop.model;

import javax.persistence.*;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

/**
 * 用户实体类，对应数据库中的 users 表
 * @Entity 表示这是一个 JPA 实体类，将映射到数据库表
 */
@Entity
@Table(name = "users")
public class User {
    /**
     * 临时字段：店铺名称（用于注册流程，不映射到数据库）
     */
    @Transient
    private String storeName;

    /**
     * 商家申请留言/理由
     */
    @Column(length = 1000)
    private String merchantApplicationReason;

    /**
     * 主键，自动递增
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户名，唯一且不能为空
     */
    @Column(unique = true, nullable = false)
    private String username;

    /**
     * 密码，不能为空
     */
    @Column(nullable = false)
    private String password;

    /**
     * 电子邮件
     */
    private String email;

    /**
     * 关联的店铺（如果是商家角色）
     */
    @OneToOne(mappedBy = "owner", cascade = CascadeType.ALL)
    private Store store;

    /**
     * 用户收货地址
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserAddress> addresses = new ArrayList<>();

    /**
     * 用户购物车项
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> cartItems = new ArrayList<>();

    /**
     * 用户订单
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders = new ArrayList<>();

    /**
     * 用户的角色（ADMIN, USER 或 MERCHANT），以字符串形式存储在数据库中
     */
    @Enumerated(EnumType.STRING)
    private Role role;

    /**
     * 用户状态（PENDING, APPROVED, REJECTED, DISABLED）
     */
    @Enumerated(EnumType.STRING)
    private UserStatus status = UserStatus.APPROVED;

    /**
     * 默认构造函数，JPA 必须
     */
    public User() {}

    /**
     * 带参数的构造函数，方便在代码中创建用户对象
     * @param username 用户名
     * @param password 密码
     * @param email 电子邮件
     * @param role 角色
     * @param storeName 店铺名称
     * @param status 状态
     */
    public User(String username, String password, String email, Role role, String storeName, UserStatus status) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
        this.storeName = storeName;
        this.status = status;
    }

    /**
     * 获取用户ID
     * @return 用户ID
     */
    public Long getId() { return id; }

    /**
     * 设置用户ID
     * @param id 用户ID
     */
    public void setId(Long id) { this.id = id; }

    /**
     * 获取用户名
     * @return 用户名
     */
    public String getUsername() { return username; }

    /**
     * 设置用户名
     * @param username 用户名
     */
    public void setUsername(String username) { this.username = username; }

    /**
     * 获取加密后的密码
     * @return 密码
     */
    public String getPassword() { return password; }

    /**
     * 设置加密后的密码
     * @param password 密码
     */
    public void setPassword(String password) { this.password = password; }

    /**
     * 获取电子邮件
     * @return 电子邮件
     */
    public String getEmail() { return email; }

    /**
     * 设置电子邮件
     * @param email 电子邮件
     */
    public void setEmail(String email) { this.email = email; }

    /**
     * 获取关联的店铺
     * @return 店铺对象
     */
    public Store getStore() { return store; }

    /**
     * 设置关联的店铺
     * @param store 店铺对象
     */
    public void setStore(Store store) { this.store = store; }

    /**
     * 获取收货地址列表
     * @return 地址列表
     */
    public List<UserAddress> getAddresses() { return addresses; }

    /**
     * 设置收货地址列表
     * @param addresses 地址列表
     */
    public void setAddresses(List<UserAddress> addresses) { this.addresses = addresses; }

    /**
     * 获取购物车项列表
     * @return 购物车列表
     */
    public List<CartItem> getCartItems() { return cartItems; }

    /**
     * 设置购物车项列表
     * @param cartItems 购物车列表
     */
    public void setCartItems(List<CartItem> cartItems) { this.cartItems = cartItems; }

    /**
     * 获取订单列表
     * @return 订单列表
     */
    public List<Order> getOrders() { return orders; }

    /**
     * 设置订单列表
     * @param orders 订单列表
     */
    public void setOrders(List<Order> orders) { this.orders = orders; }

    /**
     * 获取用户角色
     * @return 角色枚举
     */
    public Role getRole() { return role; }

    /**
     * 设置用户角色
     * @param role 角色枚举
     */
    public void setRole(Role role) { this.role = role; }

    /**
     * 获取用户状态
     * @return 状态枚举
     */
    public UserStatus getStatus() { return status; }

    /**
     * 设置用户状态
     * @param status 状态枚举
     */
    public void setStatus(UserStatus status) { this.status = status; }

    /**
     * 获取临时店铺名称
     * @return 店铺名称
     */
    public String getStoreName() { return storeName; }

    /**
     * 设置临时店铺名称
     * @param storeName 店铺名称
     */
    public void setStoreName(String storeName) { this.storeName = storeName; }

    /**
     * 获取商家申请理由
     * @return 申请理由
     */
    public String getMerchantApplicationReason() { return merchantApplicationReason; }

    /**
     * 设置商家申请理由
     * @param merchantApplicationReason 申请理由
     */
    public void setMerchantApplicationReason(String merchantApplicationReason) { this.merchantApplicationReason = merchantApplicationReason; }
}
