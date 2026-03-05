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
    // 商品唯一标识符
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 商品名称
    @Column(nullable = false)
    private String name;

    // 商品描述
    @Column(length = 1000)
    private String description;

    // 商品价格 (使用 BigDecimal 处理精确的金额)
    @Column(nullable = false)
    @DecimalMin(value = "0.0", inclusive = true, message = "价格不能为负数")
    private BigDecimal price;

    // 商品库存数量
    @Min(value = 0, message = "库存不能为负数")
    private int stock;

    // 商品图片的文件路径或 URL
    private String imageUrl;
    
    // 商品分类 (例如：膨化食品, 坚果, 饮品)
    private String category;

    // 软删除标志 (true 表示已删除，不在前端显示)
    private boolean deleted = false;

    // 默认构造函数
    public Product() {}

    // 创建商品对象的构造函数
    public Product(String name, String description, BigDecimal price, int stock, String imageUrl, String category) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.imageUrl = imageUrl;
        this.category = category;
    }

    // 各个属性的 Getter 和 Setter 方法
    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
