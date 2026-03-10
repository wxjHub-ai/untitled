package com.snackshop.model;

import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import java.math.BigDecimal;

/**
 * 商品实体类，对应数据库中的 products 表
 * 存储零食商品的相关信息
 */
@Entity
@Table(name = "products")
public class Product {
    /**
     * 商品唯一标识符
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 商品名称
     */
    @Column(nullable = false)
    private String name;

    /**
     * 商品描述
     */
    @Column(length = 1000)
    private String description;

    /**
     * 商品价格 (使用 BigDecimal 处理精确的金额)
     */
    @Column(nullable = false)
    @DecimalMin(value = "0.0", inclusive = true, message = "价格不能为负数")
    private BigDecimal price;

    /**
     * 商品库存数量
     */
    @Min(value = 0, message = "库存不能为负数")
    private int stock;

    /**
     * 商品图片的文件路径或 URL
     */
    private String imageUrl;
    
    /**
     * 商品分类 (例如：膨化食品, 坚果, 饮品)
     */
    private String category;

    /**
     * 所属店铺
     */
    @ManyToOne
    @JoinColumn(name = "store_id")
    private Store store;

    /**
     * 软删除标志 (true 表示已删除，不在前端显示)
     */
    private boolean deleted = false;

    /**
     * 默认构造函数
     */
    public Product() {}

    /**
     * 创建商品对象的构造函数
     * @param name 名称
     * @param description 描述
     * @param price 价格
     * @param stock 库存
     * @param imageUrl 图片URL
     * @param category 分类
     */
    public Product(String name, String description, BigDecimal price, int stock, String imageUrl, String category) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.imageUrl = imageUrl;
        this.category = category;
    }

    /**
     * 获取是否已删除
     * @return 删除标志
     */
    public boolean isDeleted() { return deleted; }

    /**
     * 设置是否已删除
     * @param deleted 删除标志
     */
    public void setDeleted(boolean deleted) { this.deleted = deleted; }

    /**
     * 获取商品ID
     * @return 商品ID
     */
    public Long getId() { return id; }

    /**
     * 设置商品ID
     * @param id 商品ID
     */
    public void setId(Long id) { this.id = id; }

    /**
     * 获取商品名称
     * @return 名称
     */
    public String getName() { return name; }

    /**
     * 设置商品名称
     * @param name 名称
     */
    public void setName(String name) { this.name = name; }

    /**
     * 获取商品描述
     * @return 描述
     */
    public String getDescription() { return description; }

    /**
     * 设置商品描述
     * @param description 描述
     */
    public void setDescription(String description) { this.description = description; }

    /**
     * 获取商品价格
     * @return 价格
     */
    public BigDecimal getPrice() { return price; }

    /**
     * 设置商品价格
     * @param price 价格
     */
    public void setPrice(BigDecimal price) { this.price = price; }

    /**
     * 获取库存数量
     * @return 库存
     */
    public int getStock() { return stock; }

    /**
     * 设置库存数量
     * @param stock 库存
     */
    public void setStock(int stock) { this.stock = stock; }

    /**
     * 获取图片URL
     * @return 图片URL
     */
    public String getImageUrl() { return imageUrl; }

    /**
     * 设置图片URL
     * @param imageUrl 图片URL
     */
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    /**
     * 获取分类名称
     * @return 分类名称
     */
    public String getCategory() { return category; }

    /**
     * 设置分类名称
     * @param category 分类名称
     */
    public void setCategory(String category) { this.category = category; }

    /**
     * 获取所属店铺
     * @return 店铺对象
     */
    public Store getStore() { return store; }

    /**
     * 设置所属店铺
     * @param store 店铺对象
     */
    public void setStore(Store store) { this.store = store; }
}
