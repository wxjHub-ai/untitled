package com.snackshop.controller;

import com.snackshop.model.Product;
import com.snackshop.repository.OrderRepository;
import com.snackshop.repository.ProductRepository;
import com.snackshop.repository.UserRepository;
import com.snackshop.service.ProductService;
import com.snackshop.service.UserService;
import com.snackshop.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;



import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import javax.validation.Valid;
import org.springframework.validation.BindingResult;

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
    private UserRepository userRepository;
    
    @Autowired
    private OrderRepository orderRepository;

    /**
     * 处理上传文件超过限制的异常
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxSizeException(MaxUploadSizeExceededException exc, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", "文件大小超过限制 (最大 10MB)！");
        return "redirect:/admin/products/add";
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("productCount", productService.getAllProducts().size());
        model.addAttribute("userCount", userRepository.count());
        model.addAttribute("orderCount", orderRepository.count());
        model.addAttribute("totalRevenue", orderService.getTotalRevenue());
        return "admin_dashboard";
    }

    // --- User Management ---
    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin_users";
    }

    @PostMapping("/users/updateRole")
    public String updateUserRole(@RequestParam Long userId, @RequestParam com.snackshop.model.Role role) {
        userService.updateUserRole(userId, role);
        return "redirect:/admin/users";
    }

    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/admin/users";
    }

    // --- Order Management ---
    @GetMapping("/orders")
    public String listOrders(Model model) {
        model.addAttribute("orders", orderService.getAllOrders());
        return "admin_orders";
    }

    @PostMapping("/orders/updateStatus")
    public String updateOrderStatus(@RequestParam Long orderId, @RequestParam String status) {
        orderService.updateOrderStatus(orderId, status);
        return "redirect:/admin/orders";
    }

    @GetMapping("/products")
    public String listProducts(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "admin_products";
    }

    @GetMapping("/products/add")
    public String addProductForm(Model model) {
        model.addAttribute("product", new Product());
        return "admin_product_form";
    }

    @PostMapping(value = "/products/save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String saveProduct(@Valid @ModelAttribute Product product, BindingResult bindingResult, @RequestParam("imageFile") MultipartFile imageFile, Model model) {
        if (bindingResult.hasErrors()) {
            return "admin_product_form";
        }
        
        // Manual check as a second layer of defense
        if (product.getPrice().compareTo(java.math.BigDecimal.ZERO) < 0) {
            model.addAttribute("errorMessage", "价格不能为负数！");
            return "admin_product_form";
        }
        if (product.getStock() < 0) {
            model.addAttribute("errorMessage", "库存不能为负数！");
            return "admin_product_form";
        }
        
        productService.saveProduct(product, imageFile);
        return "redirect:/admin/products";
    }


    @GetMapping("/products/edit/{id}")
    public String editProductForm(@PathVariable Long id, Model model) {
        model.addAttribute("product", productService.getProductById(id).orElse(null));
        return "admin_product_form";
    }

    @GetMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return "redirect:/admin/products";
    }
}

