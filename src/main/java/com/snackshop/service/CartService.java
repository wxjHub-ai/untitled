package com.snackshop.service;

import com.snackshop.model.CartItem;
import com.snackshop.model.Product;
import com.snackshop.model.User;
import com.snackshop.repository.CartItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 购物车服务类，管理用户的购物车项，如添加商品、更新数量、清空购物车等。
 */
@Service
public class CartService {

    @Autowired
    private CartItemRepository cartItemRepository;

    /**
     * 获取指定用户购物车中的所有商品项。
     * 
     * @param user 用户对象
     * @return 购物车项列表
     */
    public List<CartItem> getCartItems(User user) {
        return cartItemRepository.findByUser(user);
    }

    /**
     * 向购物车添加商品。
     * 如果商品已存在，则增加其数量；否则新建购物车项。
     * 
     * @param user 用户对象
     * @param product 商品对象
     * @param quantity 添加的数量
     */
    @Transactional
    public void addItem(User user, Product product, int quantity) {
        Optional<CartItem> existingItem = cartItemRepository.findByUserAndProduct(user, product);
        int totalQuantity = quantity;
        if (existingItem.isPresent()) {
            totalQuantity += existingItem.get().getQuantity();
        }

        // 检查添加后的总数量是否超过商品库存
        if (totalQuantity > product.getStock()) {
            throw new RuntimeException("库存不足，无法添加！");
        }

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(totalQuantity);
            cartItemRepository.save(item);
        } else {
            CartItem newItem = new CartItem();
            newItem.setUser(user);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            cartItemRepository.save(newItem);
        }
    }

    /**
     * 更新购物车中商品的数量（增量更新）。
     * 
     * @param user 用户对象
     * @param productId 商品ID
     * @param delta 数量变化量（正数为增加，负数为减少）
     */
    @Transactional
    public void updateQuantity(User user, Long productId, int delta) {
        Optional<CartItem> itemOpt = cartItemRepository.findByUser(user).stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (itemOpt.isPresent()) {
            CartItem item = itemOpt.get();
            int newQuantity = item.getQuantity() + delta;

            // 如果数量减至0或以下，则移除该项；否则更新数量并检查库存
            if (newQuantity <= 0) {
                cartItemRepository.delete(item);
            } else if (newQuantity > item.getProduct().getStock()) {
                throw new RuntimeException("库存不足，无法添加！");
            } else {
                item.setQuantity(newQuantity);
                cartItemRepository.save(item);
            }
        }
    }

    /**
     * 从购物车中移除特定商品。
     * 
     * @param user 用户对象
     * @param productId 商品ID
     */
    @Transactional
    public void removeItem(User user, Long productId) {
        // 查找并删除指定商品
        cartItemRepository.findByUser(user).stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .ifPresent(item -> cartItemRepository.delete(item));
    }

    /**
     * 清空指定用户的所有购物车项。
     * 
     * @param user 用户对象
     */
    @Transactional
    public void clearCart(User user) {
        cartItemRepository.deleteByUser(user);
    }

    /**
     * 计算购物车中所有商品的总金额。
     * 
     * @param user 用户对象
     * @return 总金额
     */
    public BigDecimal getTotalAmount(User user) {
        return getCartItems(user).stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}