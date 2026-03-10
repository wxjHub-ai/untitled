package com.snackshop.model;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 店铺实体类，对应数据库中的 stores 表
 */
@Entity
@Table(name = "stores")
public class Store {
    /**
     * 店铺唯一标识符
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 店铺名称
     */
    @Column(nullable = false)
    private String name;

    /**
     * 店铺描述
     */
    private String description;

    /**
     * 店铺 Logo 的 URL
     */
    private String logoUrl;

    /**
     * 店铺所有者
     */
    @OneToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    /**
     * 店铺状态 (例如：ACTIVE, CLOSED)
     */
    private String status = "ACTIVE";

    /**
     * 店铺创建时间
     */
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * 默认构造函数
     */
    public Store() {}

    /**
     * 获取店铺ID
     * @return 店铺ID
     */
    public Long getId() { return id; }

    /**
     * 设置店铺ID
     * @param id 店铺ID
     */
    public void setId(Long id) { this.id = id; }

    /**
     * 获取店铺名称
     * @return 名称
     */
    public String getName() { return name; }

    /**
     * 设置店铺名称
     * @param name 名称
     */
    public void setName(String name) { this.name = name; }

    /**
     * 获取店铺描述
     * @return 描述
     */
    public String getDescription() { return description; }

    /**
     * 设置店铺描述
     * @param description 描述
     */
    public void setDescription(String description) { this.description = description; }

    /**
     * 获取店铺Logo URL
     * @return Logo URL
     */
    public String getLogoUrl() { return logoUrl; }

    /**
     * 设置店铺Logo URL
     * @param logoUrl Logo URL
     */
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    /**
     * 获取店铺所有者
     * @return 用户对象
     */
    public User getOwner() { return owner; }

    /**
     * 设置店铺所有者
     * @param owner 用户对象
     */
    public void setOwner(User owner) { this.owner = owner; }

    /**
     * 获取店铺状态
     * @return 状态
     */
    public String getStatus() { return status; }

    /**
     * 设置店铺状态
     * @param status 状态
     */
    public void setStatus(String status) { this.status = status; }

    /**
     * 获取创建时间
     * @return 创建时间
     */
    public LocalDateTime getCreatedAt() { return createdAt; }

    /**
     * 设置创建时间
     * @param createdAt 创建时间
     */
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}