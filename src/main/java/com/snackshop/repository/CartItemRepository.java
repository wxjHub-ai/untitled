package com.snackshop.repository;

import com.snackshop.model.CartItem;
import com.snackshop.model.Product;
import com.snackshop.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/**
 * 购物车项目仓库接口
 */
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    /**
     * 根据用户查找其所有的购物车项目
     * @param user 用户对象
     * @return 购物车项目列表
     */
    List<CartItem> findByUser(User user);

    /**
     * 根据用户和商品查找对应的购物车项目
     * @param user 用户对象
     * @param product 商品对象
     * @return 购物车项目的可选包装对象
     */
    Optional<CartItem> findByUserAndProduct(User user, Product product);

    /**
     * 删除指定用户的所有购物车项目
     * @param user 用户对象
     */
    void deleteByUser(User user);
}