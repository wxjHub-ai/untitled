package com.snackshop.controller;

import com.snackshop.model.*;
import com.snackshop.service.*;
import com.snackshop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * 购物车控制器
 * 处理用户购物车的查看、添加、修改、删除及结算功能
 */
@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;
    
    @Autowired
    private CartService cartService;

    @Autowired
    private UserAddressService userAddressService;

    @Autowired
    private UserRepository userRepository;

    /**
     * 获取当前登录用户
     * @return 当前登录的用户对象，若未登录则返回 null
     */
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return userRepository.findByUsername(auth.getName()).orElse(null);
    }

    /**
     * 查看购物车页面
     */
    @GetMapping
    public String viewCart(Model model) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/login";
        
        List<CartItem> items = cartService.getCartItems(user);
        model.addAttribute("cartItems", items);
        model.addAttribute("totalAmount", cartService.getTotalAmount(user));
        // 提供用户的收货地址簿供结算选择
        model.addAttribute("addresses", userAddressService.getAddressesByUser(user));
        return "cart";
    }

    /**
     * 向购物车添加商品
     * @param productId 商品ID
     * @param quantity 添加数量，默认为 1
     */
    @PostMapping("/add")
    public String addToCart(@RequestParam Long productId, @RequestParam(defaultValue = "1") int quantity) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/login";

        Product product = productService.getProductById(productId).orElse(null);
        if (product != null) {
            try {
                cartService.addItem(user, product, quantity);
            } catch (RuntimeException e) {
                // 如果添加失败（如库存不足），重定向回详情页并携带错误信息
                return "redirect:/product/" + productId + "?error=" + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8);
            }
        }
        return "redirect:/cart";
    }

    /**
     * 增加购物车内某项商品的数量
     */
    @GetMapping("/increment/{productId}")
    public String incrementQuantity(@PathVariable Long productId) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/login";

        try {
            cartService.updateQuantity(user, productId, 1);
        } catch (RuntimeException e) {
            return "redirect:/cart?error=" + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8);
        }
        return "redirect:/cart";
    }

    /**
     * 减少购物车内某项商品的数量
     */
    @GetMapping("/decrement/{productId}")
    public String decrementQuantity(@PathVariable Long productId) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/login";

        cartService.updateQuantity(user, productId, -1);
        return "redirect:/cart";
    }

    /**
     * 从购物车中移除特定商品
     */
    @GetMapping("/remove/{productId}")
    public String removeFromCart(@PathVariable Long productId) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/login";

        cartService.removeItem(user, productId);
        return "redirect:/cart";
    }

    /**
     * 购物车结算下单
     * @param addressId 选择的地址簿ID（可选）
     * @param deliveryAddress 直接填写的收货地址（可选）
     */
    @PostMapping("/checkout")
    public String checkout(@RequestParam(required = false) Long addressId, @RequestParam(required = false) String deliveryAddress) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/login";

        // 确定最终使用的收货地址
        String finalAddress = deliveryAddress;
        if (addressId != null) {
            UserAddress savedAddr = userAddressService.getAddressesByUser(user).stream()
                .filter(a -> a.getId().equals(addressId))
                .findFirst().orElse(null);
            if (savedAddr != null) {
                finalAddress = savedAddr.getFullAddress();
            }
        }

        if (finalAddress == null || finalAddress.trim().isEmpty()) {
            return "redirect:/cart?error=" + java.net.URLEncoder.encode("请选择或填写收货地址！", java.nio.charset.StandardCharsets.UTF_8);
        }

        List<CartItem> cartItems = cartService.getCartItems(user);
        if (cartItems.isEmpty()) {
            return "redirect:/cart";
        }

        // 将购物车项转换为订单项
        List<OrderItem> orderItems = new java.util.ArrayList<>();
        for (CartItem ci : cartItems) {
            OrderItem oi = new OrderItem();
            oi.setProduct(ci.getProduct());
            oi.setQuantity(ci.getQuantity());
            oi.setPrice(ci.getProduct().getPrice());
            orderItems.add(oi);
        }

        try {
            // 创建订单并清空购物车
            orderService.createOrder(user, orderItems, cartService.getTotalAmount(user), finalAddress);
            cartService.clearCart(user);
        } catch (RuntimeException e) {
            return "redirect:/cart?error=" + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8);
        }

        return "redirect:/orders/my";
    }
}
