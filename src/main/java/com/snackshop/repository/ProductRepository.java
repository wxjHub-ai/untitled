package com.snackshop.repository;

import com.snackshop.model.Product;
import com.snackshop.model.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * 商品仓库接口
 * 继承 JpaRepository 即可获得基本的 CRUD（增删改查）功能
 * Spring Data JPA 会在运行时自动生成该接口的实现类
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * 查找所属店铺的未删除商品
     */
    List<Product> findByStoreAndDeletedFalse(Store store);

    /**
     * 根据商品分类查找商品列表
     * @param category 商品分类名称
     * @return 该分类下的商品列表
     */
    List<Product> findByCategory(String category);

    /**
     * 查找所有未被删除的商品
     * 对应逻辑删除的功能
     * @return 未删除的商品列表
     */
    List<Product> findByDeletedFalse();

    /**
     * 根据分类查找未删除的商品
     */
    List<Product> findByCategoryAndDeletedFalse(String category);

    /**
     * 根据名称模糊查询未删除的商品 (忽略大小写)
     */
    List<Product> findByNameContainingIgnoreCaseAndDeletedFalse(String name);

    /**
     * 根据分类和名称模糊查询未删除的商品 (忽略大小写)
     */
    List<Product> findByCategoryAndNameContainingIgnoreCaseAndDeletedFalse(String category, String name);
}
