package com.snackshop.controller;

import com.snackshop.model.*;
import com.snackshop.repository.*;
import com.snackshop.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import javax.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.stream.Collectors;
import java.util.List;
import java.util.Map;
import java.time.format.DateTimeFormatter;
import java.util.TreeMap;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ProductService productService;
    @Autowired
    private UserService userService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private StoreService storeService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OrderRepository orderRepository;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            return userRepository.findByUsername(auth.getName()).orElse(null);
        }
        return null;
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxSizeException(MaxUploadSizeExceededException exc, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", "文件大小超过限制 (最大 10MB)！");
        return "redirect:/admin/products/add";
    }

    @GetMapping
    public String dashboard(Model model) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/login";

        if (user.getRole() == Role.MERCHANT) {
            Store store = storeService.getStoreByOwner(user);
            List<Product> products = productService.getProductsByStore(store);
            List<com.snackshop.model.Order> orders = orderService.getOrdersByMerchant(user);
            
            model.addAttribute("productCount", products.size());
            model.addAttribute("userCount", null);
            model.addAttribute("orderCount", orders.size());
            model.addAttribute("totalRevenue", orderService.getMerchantRevenue(user));
            
            // Charts Data (Merchant Specific)
            Map<String, java.math.BigDecimal> dailyRevenue = orders.stream()
                .collect(Collectors.groupingBy(
                    o -> o.getOrderDate().format(DateTimeFormatter.ofPattern("MM-dd")),
                    TreeMap::new,
                    Collectors.reducing(java.math.BigDecimal.ZERO, 
                        o -> o.getItems().stream()
                            .filter(item -> item.getProduct().getStore() != null && item.getProduct().getStore().getId().equals(store.getId()))
                            .map(item -> item.getPrice().multiply(new java.math.BigDecimal(item.getQuantity())))
                            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add),
                        java.math.BigDecimal::add)
                ));
            model.addAttribute("chartLabels", dailyRevenue.keySet());
            model.addAttribute("chartData", dailyRevenue.values());

            Map<String, Long> dailyOrderCount = orders.stream()
                .collect(Collectors.groupingBy(
                    o -> o.getOrderDate().format(DateTimeFormatter.ofPattern("MM-dd")),
                    TreeMap::new, Collectors.counting()
                ));
            model.addAttribute("orderTrendLabels", dailyOrderCount.keySet());
            model.addAttribute("orderTrendData", dailyOrderCount.values());

            Map<String, Integer> dailyProductCount = orders.stream()
                .collect(Collectors.groupingBy(
                    o -> o.getOrderDate().format(DateTimeFormatter.ofPattern("MM-dd")),
                    TreeMap::new,
                    Collectors.summingInt(o -> o.getItems().stream()
                        .filter(item -> item.getProduct().getStore() != null && item.getProduct().getStore().getId().equals(store.getId()))
                        .mapToInt(com.snackshop.model.OrderItem::getQuantity).sum())
                ));
            model.addAttribute("productCountLabels", dailyProductCount.keySet());
            model.addAttribute("productCountData", dailyProductCount.values());
            
        } else {
            // Admin Global Logic
            model.addAttribute("productCount", productService.getAllProducts().size());
            model.addAttribute("userCount", userRepository.count());
            model.addAttribute("orderCount", orderRepository.count());
            model.addAttribute("totalRevenue", orderService.getTotalRevenue());

            Map<String, java.math.BigDecimal> dailyRevenue = orderRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                    o -> o.getOrderDate().format(DateTimeFormatter.ofPattern("MM-dd")),
                    TreeMap::new,
                    Collectors.reducing(java.math.BigDecimal.ZERO, com.snackshop.model.Order::getTotalAmount, java.math.BigDecimal::add)
                ));
            model.addAttribute("chartLabels", dailyRevenue.keySet());
            model.addAttribute("chartData", dailyRevenue.values());

            Map<String, Long> dailyOrderCount = orderRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                    o -> o.getOrderDate().format(DateTimeFormatter.ofPattern("MM-dd")),
                    TreeMap::new, Collectors.counting()
                ));
            model.addAttribute("orderTrendLabels", dailyOrderCount.keySet());
            model.addAttribute("orderTrendData", dailyOrderCount.values());

            Map<String, Integer> dailyProductCount = orderRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                    o -> o.getOrderDate().format(DateTimeFormatter.ofPattern("MM-dd")),
                    TreeMap::new,
                    Collectors.summingInt(o -> o.getItems().stream().mapToInt(com.snackshop.model.OrderItem::getQuantity).sum())
                ));
            model.addAttribute("productCountLabels", dailyProductCount.keySet());
            model.addAttribute("productCountData", dailyProductCount.values());
        }
        return "admin_dashboard";
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        User user = getCurrentUser();
        if (user != null && user.getRole() == Role.MERCHANT) return "redirect:/admin";
        model.addAttribute("users", userService.getAllUsers());
        return "admin_users";
    }

    @PostMapping("/users/updateRole")
    public String updateUserRole(@RequestParam Long userId, @RequestParam Role role) {
        if (getCurrentUser().getRole() != Role.ADMIN) return "redirect:/admin";
        userService.updateUserRole(userId, role);
        return "redirect:/admin/users";
    }

    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        if (getCurrentUser().getRole() != Role.ADMIN) return "redirect:/admin";
        userService.deleteUser(id);
        return "redirect:/admin/users";
    }

    @GetMapping("/orders")
    public String listOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate startDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate endDate,
            Model model) {
        User user = getCurrentUser();
        List<com.snackshop.model.Order> orders = user.getRole() == Role.MERCHANT ? 
            orderService.searchOrders(status, startDate, endDate, user) :
            orderService.searchOrders(status, startDate, endDate, null);
        
        model.addAttribute("orders", orders);
        model.addAttribute("status", status);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        return "admin_orders";
    }

    @PostMapping("/orders/updateStatus")
    public String updateOrderStatus(@RequestParam Long orderId, @RequestParam String status) {
        orderService.updateOrderStatus(orderId, status);
        return "redirect:/admin/orders";
    }

    @GetMapping("/orders/delete/{id}")
    public String deleteOrder(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            orderService.deleteOrder(id);
            redirectAttributes.addFlashAttribute("successMessage", "订单删除成功！");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/orders";
    }

    @GetMapping("/products")
    public String listProducts(@RequestParam(required = false) Long merchantId, Model model) {
        User user = getCurrentUser();
        List<Product> products;
        if (user.getRole() == Role.MERCHANT) {
            products = productService.getProductsByStore(storeService.getStoreByOwner(user));
        } else if (merchantId != null) {
            User merchant = userRepository.findById(merchantId).orElse(null);
            products = merchant != null ? productService.getProductsByStore(storeService.getStoreByOwner(merchant)) : productService.getAllProducts();
        } else {
            products = productService.getAllProducts();
        }
        model.addAttribute("products", products);
        model.addAttribute("selectedMerchantId", merchantId);
        if (user.getRole() == Role.ADMIN) {
            model.addAttribute("merchants", userRepository.findAll().stream().filter(u -> u.getRole() == Role.MERCHANT).collect(Collectors.toList()));
        }
        return "admin_products";
    }

    @GetMapping("/products/add")
    public String addProductForm(Model model) {
        model.addAttribute("product", new Product());
        return "admin_product_form";
    }

    @PostMapping(value = "/products/save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String saveProduct(@Valid @ModelAttribute Product product, BindingResult bindingResult, @RequestParam("imageFile") MultipartFile imageFile, Model model) {
        if (bindingResult.hasErrors()) return "admin_product_form";
        User user = getCurrentUser();
        if (product.getId() != null) {
            productService.getProductById(product.getId()).ifPresent(existing -> product.setStore(existing.getStore()));
        }
        if (user != null && user.getRole() == Role.MERCHANT) {
            product.setStore(storeService.getStoreByOwner(user));
        }
        productService.saveProduct(product, imageFile);
        return "redirect:/admin/products";
    }

    @GetMapping("/products/edit/{id}")
    public String editProductForm(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id).orElse(null);
        model.addAttribute("product", product);
        return "admin_product_form";
    }

    @GetMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return "redirect:/admin/products";
    }
}