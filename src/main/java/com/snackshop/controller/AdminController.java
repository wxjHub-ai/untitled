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

/**
 * 管理员与商家后台控制器
 * 处理商品管理、订单管理、用户管理及数据统计仪表盘
 */
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

    /**
     * 获取当前登录用户
     * @return 当前登录的用户对象，若未登录则返回 null
     */
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            return userRepository.findByUsername(auth.getName()).orElse(null);
        }
        return null;
    }

    /**
     * 处理文件上传大小超过限制的异常
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxSizeException(MaxUploadSizeExceededException exc, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", "文件大小超过限制 (最大 10MB)！");
        return "redirect:/admin/products/add";
    }

    /**
     * 后台管理首页（仪表盘）
     * 根据用户角色（管理员或商家）展示相应的统计数据
     * @param year 指定年份的统计数据，默认为当前年份
     * @param model 视图模型
     * @return 仪表盘页面模板
     */
    @GetMapping
    public String dashboard(@RequestParam(required = false) Integer year, Model model) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/login";

        int currentYear = java.time.LocalDate.now().getYear();
        int selectedYear = (year != null) ? year : currentYear;
        model.addAttribute("selectedYear", selectedYear);
        
        // 生成年份选择器数据：最近 5 年
        List<Integer> availableYears = java.util.stream.IntStream.rangeClosed(currentYear - 4, currentYear)
                .boxed().sorted(java.util.Collections.reverseOrder()).collect(Collectors.toList());
        model.addAttribute("availableYears", availableYears);

        // 如果是商家，只显示其所属店铺的数据
        if (user.getRole() == Role.MERCHANT) {
            Store store = storeService.getStoreByOwner(user);
            if (store == null) {
                store = storeService.createDefaultStore(user);
            }
            final Store finalStore = store;
            List<Product> products = productService.getProductsByStore(store);
            List<com.snackshop.model.Order> allOrders = orderService.getOrdersByMerchant(user);
            // 过滤出选中年份的订单
            List<com.snackshop.model.Order> orders = allOrders.stream()
                .filter(o -> o.getOrderDate().getYear() == selectedYear)
                .collect(Collectors.toList());
            
            model.addAttribute("productCount", products.size());
            model.addAttribute("userCount", null); // 商家不可见总用户数
            model.addAttribute("orderCount", orders.size());
            
            // 计算商家总营收
            model.addAttribute("totalRevenue", orders.stream()
                .flatMap(o -> o.getItems().stream())
                .filter(item -> item.getProduct().getStore() != null && item.getProduct().getStore().getId().equals(finalStore.getId()))
                .map(item -> item.getPrice().multiply(new java.math.BigDecimal(item.getQuantity())))
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add));
            
            // 图表数据 (商家特定)
            // 每日营收统计
            Map<String, java.math.BigDecimal> dailyRevenue = orders.stream()
                .collect(Collectors.groupingBy(
                    o -> o.getOrderDate().format(DateTimeFormatter.ofPattern("MM-dd")),
                    TreeMap::new,
                    Collectors.reducing(java.math.BigDecimal.ZERO, 
                        o -> o.getItems().stream()
                            .filter(item -> item.getProduct().getStore() != null && item.getProduct().getStore().getId().equals(finalStore.getId()))
                            .map(item -> item.getPrice().multiply(new java.math.BigDecimal(item.getQuantity())))
                            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add),
                        java.math.BigDecimal::add)
                ));
            model.addAttribute("chartLabels", dailyRevenue.keySet());
            model.addAttribute("chartData", dailyRevenue.values());

            // 每日订单量统计
            Map<String, Long> dailyOrderCount = orders.stream()
                .collect(Collectors.groupingBy(
                    o -> o.getOrderDate().format(DateTimeFormatter.ofPattern("MM-dd")),
                    TreeMap::new, Collectors.counting()
                ));
            model.addAttribute("orderTrendLabels", dailyOrderCount.keySet());
            model.addAttribute("orderTrendData", dailyOrderCount.values());

            // 每日商品销售件数统计
            Map<String, Integer> dailyProductCount = orders.stream()
                .collect(Collectors.groupingBy(
                    o -> o.getOrderDate().format(DateTimeFormatter.ofPattern("MM-dd")),
                    TreeMap::new,
                    Collectors.summingInt(o -> o.getItems().stream()
                        .filter(item -> item.getProduct().getStore() != null && item.getProduct().getStore().getId().equals(finalStore.getId()))
                        .mapToInt(com.snackshop.model.OrderItem::getQuantity).sum())
                ));
            model.addAttribute("productCountLabels", dailyProductCount.keySet());
            model.addAttribute("productCountData", dailyProductCount.values());
            
        } else {
            // 管理员全局统计逻辑
            List<com.snackshop.model.Order> allOrders = orderRepository.findAll();
            List<com.snackshop.model.Order> orders = allOrders.stream()
                .filter(o -> o.getOrderDate().getYear() == selectedYear)
                .collect(Collectors.toList());

            model.addAttribute("productCount", productService.getAllProducts().size());
            model.addAttribute("userCount", userRepository.count());
            model.addAttribute("merchantCount", userRepository.countByRole(Role.MERCHANT));
            model.addAttribute("orderCount", orders.size());
            model.addAttribute("totalRevenue", orders.stream()
                .map(com.snackshop.model.Order::getTotalAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add));

            // 全局图表数据
            Map<String, java.math.BigDecimal> dailyRevenue = orders.stream()
                .collect(Collectors.groupingBy(
                    o -> o.getOrderDate().format(DateTimeFormatter.ofPattern("MM-dd")),
                    TreeMap::new,
                    Collectors.reducing(java.math.BigDecimal.ZERO, com.snackshop.model.Order::getTotalAmount, java.math.BigDecimal::add)
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
                    Collectors.summingInt(o -> o.getItems().stream().mapToInt(com.snackshop.model.OrderItem::getQuantity).sum())
                ));
            model.addAttribute("productCountLabels", dailyProductCount.keySet());
            model.addAttribute("productCountData", dailyProductCount.values());
        }
        return "admin_dashboard";
    }

    /**
     * 用户管理列表（仅管理员可见）
     */
    @GetMapping("/users")
    public String listUsers(Model model) {
        User user = getCurrentUser();
        if (user != null && user.getRole() == Role.MERCHANT) return "redirect:/admin";
        model.addAttribute("users", userService.getAllUsers());
        return "admin_users";
    }

    /**
     * 更新用户角色（仅管理员权限）
     */
    @PostMapping("/users/updateRole")
    public String updateUserRole(@RequestParam Long userId, @RequestParam Role role) {
        if (getCurrentUser().getRole() != Role.ADMIN) return "redirect:/admin";
        userService.updateUserRole(userId, role);
        return "redirect:/admin/users";
    }

    /**
     * 删除用户（仅管理员权限）
     */
    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        if (getCurrentUser().getRole() != Role.ADMIN) return "redirect:/admin";
        userService.deleteUser(id);
        return "redirect:/admin/users";
    }

    /**
     * 订单管理列表
     * 管理员可查看所有订单，商家仅能查看与其商品相关的订单
     */
    @GetMapping("/orders")
    public String listOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate startDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate endDate,
            Model model) {
        User user = getCurrentUser();
        List<com.snackshop.model.Order> orders = user.getRole() == Role.MERCHANT ? 
            orderService.searchOrders(status, username, startDate, endDate, user) :
            orderService.searchOrders(status, username, startDate, endDate, null);
        
        model.addAttribute("orders", orders);
        model.addAttribute("status", status);
        model.addAttribute("username", username);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        return "admin_orders";
    }

    /**
     * 更新订单状态
     */
    @PostMapping("/orders/updateStatus")
    public String updateOrderStatus(@RequestParam Long orderId, @RequestParam String status) {
        orderService.updateOrderStatus(orderId, status);
        return "redirect:/admin/orders";
    }

    /**
     * 删除订单
     */
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

    /**
     * 商品管理列表
     * 商家仅看到自己店铺的商品，管理员可筛选或查看所有商品
     */
    @GetMapping("/products")
    public String listProducts(@RequestParam(required = false) Long merchantId, Model model) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/login";

        List<Product> products;
        if (user.getRole() == Role.MERCHANT) {
            // 商家只能看自己的商品
            Store store = storeService.getStoreByOwner(user);
            if (store == null) {
                store = storeService.createDefaultStore(user);
            }
            products = productService.getProductsByStore(store);
        } else if (merchantId != null) {
            // 管理员筛选特定商家的商品
            User merchant = userRepository.findById(merchantId).orElse(null);
            Store store = merchant != null ? storeService.getStoreByOwner(merchant) : null;
            products = store != null ? productService.getProductsByStore(store) : productService.getAllProducts();
        } else {
            // 管理员查看所有商品
            products = productService.getAllProducts();
        }
        model.addAttribute("products", products);
        model.addAttribute("selectedMerchantId", merchantId);
        if (user.getRole() == Role.ADMIN) {
            model.addAttribute("merchants", userRepository.findAll().stream().filter(u -> u.getRole() == Role.MERCHANT).collect(Collectors.toList()));
        }
        return "admin_products";
    }

    /**
     * 显示新增商品表单
     */
    @GetMapping("/products/add")
    public String addProductForm(Model model) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/login";
        model.addAttribute("product", new Product());
        return "admin_product_form";
    }

    /**
     * 保存或更新商品信息（包含图片上传）
     */
    @PostMapping(value = "/products/save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String saveProduct(@Valid @ModelAttribute Product product, BindingResult bindingResult, @RequestParam("imageFile") MultipartFile imageFile, Model model) {
        if (bindingResult.hasErrors()) return "admin_product_form";
        User user = getCurrentUser();
        if (user == null) return "redirect:/login";

        Store merchantStore = null;
        if (user.getRole() == Role.MERCHANT) {
            merchantStore = storeService.getStoreByOwner(user);
            if (merchantStore == null) {
                // 如果商家没有店铺，则即时创建一个
                merchantStore = storeService.createDefaultStore(user);
            }
        }

        if (product.getId() != null) {
            // 编辑更新
            Product existing = productService.getProductById(product.getId()).orElse(null);
            if (existing == null) return "redirect:/admin/products";

            if (user.getRole() == Role.MERCHANT) {
                // 权限检查：确保商家只能编辑自己的商品
                if (existing.getStore() == null || !existing.getStore().getId().equals(merchantStore.getId())) {
                    return "redirect:/admin/products";
                }
                product.setStore(merchantStore);
            } else {
                // 管理员可以更新任何商品，保留原有店铺信息
                product.setStore(existing.getStore());
            }
        } else {
            // 新增商品
            if (user.getRole() == Role.MERCHANT) {
                product.setStore(merchantStore);
            }
        }
        
        productService.saveProduct(product, imageFile);
        return "redirect:/admin/products";
    }

    /**
     * 显示编辑商品表单
     */
    @GetMapping("/products/edit/{id}")
    public String editProductForm(@PathVariable Long id, Model model) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/login";

        Product product = productService.getProductById(id).orElse(null);
        if (product == null) return "redirect:/admin/products";

        // 商家编辑权限校验
        if (user.getRole() == Role.MERCHANT) {
            Store store = storeService.getStoreByOwner(user);
            if (store == null || product.getStore() == null || !product.getStore().getId().equals(store.getId())) {
                return "redirect:/admin/products";
            }
        }

        model.addAttribute("product", product);
        return "admin_product_form";
    }

    /**
     * 删除商品
     */
    @GetMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/login";

        Product product = productService.getProductById(id).orElse(null);
        // 商家删除权限校验
        if (product != null && user.getRole() == Role.MERCHANT) {
            Store store = storeService.getStoreByOwner(user);
            if (store == null || product.getStore() == null || !product.getStore().getId().equals(store.getId())) {
                return "redirect:/admin/products";
            }
        }
        
        productService.deleteProduct(id);
        return "redirect:/admin/products";
    }
}
