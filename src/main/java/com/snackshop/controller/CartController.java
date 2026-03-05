package com.snackshop.controller;

import com.snackshop.model.*;
import com.snackshop.service.OrderService;
import com.snackshop.service.ProductService;
import com.snackshop.service.UserService;
import com.snackshop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;
    
    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public String viewCart(HttpSession session, Model model) {
        Cart cart = getCart(session);
        model.addAttribute("cart", cart);
        model.addAttribute("totalAmount", cart.getTotalAmount());
        return "cart";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam Long productId, @RequestParam(defaultValue = "1") int quantity, HttpSession session, Model model) {
        Product product = productService.getProductById(productId).orElse(null);
        if (product != null) {
            if (product.getStock() < quantity) {
                return "redirect:/product/" + productId + "?error=NotEnoughStock";
            }
            Cart cart = getCart(session);
            // Check if adding this quantity exceeds stock considering what's already in cart
            // (Simplified check for now, ideally check cart content too)
            cart.addItem(product, quantity);
        }
        return "redirect:/cart";
    }

    @GetMapping("/remove/{productId}")
    public String removeFromCart(@PathVariable Long productId, HttpSession session) {
        Cart cart = getCart(session);
        cart.removeItem(productId);
        return "redirect:/cart";
    }

    @PostMapping("/checkout")
    public String checkout(HttpSession session, Model model) {
        Cart cart = getCart(session);
        if (cart.getItems().isEmpty()) {
            return "redirect:/cart";
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "redirect:/login";
        }

        String username = auth.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        
        if (user != null) {
            // Convert Cart items to OrderItems
            List<OrderItem> orderItems = new ArrayList<>();
            for (CartItem ci : cart.getItems()) {
                OrderItem oi = new OrderItem();
                oi.setProduct(ci.getProduct());
                oi.setQuantity(ci.getQuantity());
                oi.setPrice(ci.getProduct().getPrice());
                orderItems.add(oi);
            }
            
            try {
                orderService.createOrder(user, orderItems, cart.getTotalAmount());
                cart.clear();
            } catch (RuntimeException e) {
                // Handle out of stock exception
                return "redirect:/cart?error=" + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8);
            }
        }

        return "redirect:/orders/my";
    }

    private Cart getCart(HttpSession session) {
        Cart cart = (Cart) session.getAttribute("cart");
        if (cart == null) {
            cart = new Cart();
            session.setAttribute("cart", cart);
        }
        return cart;
    }
}
