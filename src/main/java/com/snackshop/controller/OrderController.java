package com.snackshop.controller;

import com.snackshop.model.User;
import com.snackshop.repository.UserRepository;
import com.snackshop.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 订单控制器
 * 处理普通用户查看个人订单的相关操作
 */
@Controller
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;

    /**
     * 查看当前登录用户的订单列表
     */
    @GetMapping("/my")
    public String myOrders(Model model) {
        // 获取当前认证信息
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByUsername(auth.getName()).orElse(null);
        if (user != null) {
            // 查询并添加该用户的订单到模型中
            model.addAttribute("orders", orderService.getOrdersByUser(user));
        }
        return "my_orders";
    }
}
