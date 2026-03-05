package com.snackshop.service;

import com.snackshop.model.Product;
import com.snackshop.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 商品服务类，处理商品的业务逻辑
 * @Service 告诉 Spring 这是一个服务层组件，由 Spring 管理
 */
@Service
public class ProductService {

    // 图片上传的目录路径
    private final Path UPLOAD_DIR = Paths.get("src/main/resources/static/product_images");

    // 自动注入 ProductRepository，用于访问数据库
    @Autowired
    private ProductRepository productRepository;

    /**
     * 获取所有未删除的商品列表
     * @return 商品列表
     */
    public List<Product> getAllProducts() {
        return productRepository.findByDeletedFalse();
    }

    /**
     * 根据搜索词和分类搜索商品
     * @param category 分类（可选）
     * @param query 搜索关键词（可选）
     * @return 匹配的商品列表
     */
    public List<Product> searchProducts(String category, String query) {
        boolean hasCategory = category != null && !category.isEmpty() && !category.equals("全部");
        boolean hasQuery = query != null && !query.isEmpty();

        if (hasCategory && hasQuery) {
            return productRepository.findByCategoryAndNameContainingIgnoreCaseAndDeletedFalse(category, query);
        } else if (hasCategory) {
            return productRepository.findByCategoryAndDeletedFalse(category);
        } else if (hasQuery) {
            return productRepository.findByNameContainingIgnoreCaseAndDeletedFalse(query);
        } else {
            return productRepository.findByDeletedFalse();
        }
    }

    /**
     * 根据 ID 获取单个商品信息
     * @param id 商品 ID
     * @return 返回 Optional，可能包含商品，也可能为空
     */
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    /**
     * 保存或更新商品信息，并处理图片上传
     * @param product 商品对象
     * @param imageFile 上传的图片文件
     */
    public void saveProduct(Product product, MultipartFile imageFile) {
        if (!imageFile.isEmpty()) {
            try {
                // 检查上传目录是否存在，不存在则创建
                if (!Files.exists(UPLOAD_DIR)) {
                    Files.createDirectories(UPLOAD_DIR);
                }

                // 获取原始文件名并生成唯一的文件名，防止同名文件冲突
                String originalFilename = imageFile.getOriginalFilename();
                String fileExtension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
                
                // 保存图片文件到本地
                Path filePath = UPLOAD_DIR.resolve(uniqueFileName);
                Files.copy(imageFile.getInputStream(), filePath);
                
                // 将图片的相对路径存入数据库
                product.setImageUrl("/product_images/" + uniqueFileName);
            } catch (IOException e) {
                // 处理 IO 异常，例如记录日志
                e.printStackTrace();
            }
        } else if (product.getId() != null) {
            // If updating and no new image, keep the existing one
            productRepository.findById(product.getId()).ifPresent(existing -> {
                product.setImageUrl(existing.getImageUrl());
            });
        }
        // 调用 Repository 保存商品到数据库
        productRepository.save(product);
    }
    
    /**
     * 删除商品（采用逻辑删除，而非物理删除）
     * @param id 商品 ID
     */
    public void deleteProduct(Long id) {
        Optional<Product> product = productRepository.findById(id);
        if (product.isPresent()) {
            Product p = product.get();
            // 设置删除标志为 true
            p.setDeleted(true);
            productRepository.save(p);
        }
    }
}
