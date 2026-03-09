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

@Service
public class CartService {

    @Autowired
    private CartItemRepository cartItemRepository;

    public List<CartItem> getCartItems(User user) {
        return cartItemRepository.findByUser(user);
    }

    @Transactional
    public void addItem(User user, Product product, int quantity) {
        Optional<CartItem> existingItem = cartItemRepository.findByUserAndProduct(user, product);
        int totalQuantity = quantity;
        if (existingItem.isPresent()) {
            totalQuantity += existingItem.get().getQuantity();
        }

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

    @Transactional
    public void updateQuantity(User user, Long productId, int delta) {
        Optional<CartItem> itemOpt = cartItemRepository.findByUser(user).stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (itemOpt.isPresent()) {
            CartItem item = itemOpt.get();
            int newQuantity = item.getQuantity() + delta;

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

    @Transactional
    public void removeItem(User user, Long productId) {
        // Find specifically by user and product
        cartItemRepository.findByUser(user).stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .ifPresent(item -> cartItemRepository.delete(item));
    }

    @Transactional
    public void clearCart(User user) {
        cartItemRepository.deleteByUser(user);
    }

    public BigDecimal getTotalAmount(User user) {
        return getCartItems(user).stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}