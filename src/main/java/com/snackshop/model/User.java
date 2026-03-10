package com.snackshop.model;

import javax.persistence.*;
import java.util.Set;

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
     * 用户的角色（ADMIN, USER 或 MERCHANT），以字符串形式存储在数据库中
     */
    @Enumerated(EnumType.STRING)
    private Role role;

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
     */
    public User(String username, String password, String email, Role role, String storeName) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
        this.storeName = storeName;
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
     * 获取临时店铺名称
     * @return 店铺名称
     */
    public String getStoreName() { return storeName; }

    /**
     * 设置临时店铺名称
     * @param storeName 店铺名称
     */
    public void setStoreName(String storeName) { this.storeName = storeName; }
}
