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

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return userRepository.findByUsername(auth.getName()).orElse(null);
    }

    @GetMapping
    public String viewCart(Model model) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/login";
        
        List<CartItem> items = cartService.getCartItems(user);
        model.addAttribute("cartItems", items);
        model.addAttribute("totalAmount", cartService.getTotalAmount(user));
        model.addAttribute("addresses", userAddressService.getAddressesByUser(user));
        return "cart";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam Long productId, @RequestParam(defaultValue = "1") int quantity) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/login";

        Product product = productService.getProductById(productId).orElse(null);
        if (product != null) {
            try {
                cartService.addItem(user, product, quantity);
            } catch (RuntimeException e) {
                return "redirect:/product/" + productId + "?error=" + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8);
            }
        }
        return "redirect:/cart";
    }

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

    @GetMapping("/decrement/{productId}")
    public String decrementQuantity(@PathVariable Long productId) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/login";

        cartService.updateQuantity(user, productId, -1);
        return "redirect:/cart";
    }

    @GetMapping("/remove/{productId}")
    public String removeFromCart(@PathVariable Long productId) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/login";

        cartService.removeItem(user, productId);
        return "redirect:/cart";
    }

    @PostMapping("/checkout")
    public String checkout(@RequestParam(required = false) Long addressId, @RequestParam(required = false) String deliveryAddress) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/login";

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

        List<OrderItem> orderItems = new java.util.ArrayList<>();
        for (CartItem ci : cartItems) {
            OrderItem oi = new OrderItem();
            oi.setProduct(ci.getProduct());
            oi.setQuantity(ci.getQuantity());
            oi.setPrice(ci.getProduct().getPrice());
            orderItems.add(oi);
        }

        try {
            orderService.createOrder(user, orderItems, cartService.getTotalAmount(user), finalAddress);
            cartService.clearCart(user);
        } catch (RuntimeException e) {
            return "redirect:/cart?error=" + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8);
        }

        return "redirect:/orders/my";
    }
}