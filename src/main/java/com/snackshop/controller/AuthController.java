package com.snackshop.controller;

import com.snackshop.model.User;
import com.snackshop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * 身份验证控制器，处理用户登录和注册的请求。
 */
@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * 显示登录页面。
     * 
     * @return 登录页面的模板名称
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /**
     * 显示注册页面。
     * 
     * @param model 用于向页面传递数据的模型
     * @return 注册页面的模板名称
     */
    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    /**
     * 处理用户提交的注册表单。
     * 
     * @param user 提交的用户信息
     * @return 注册成功后重定向到登录页面
     */
    @PostMapping("/register")
    public String register(@ModelAttribute User user) {
        userService.registerUser(user);
        return "redirect:/login";
    }
}
