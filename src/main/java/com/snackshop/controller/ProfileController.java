package com.snackshop.controller;

import com.snackshop.model.User;
import com.snackshop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

/**
 * 个人信息控制器
 * 处理用户个人资料的查看与修改
 */
@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserService userService;

    /**
     * 获取当前登录用户
     * @return 当前登录的用户对象，若未登录则返回 null
     */
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            Optional<User> user = userService.findByUsername(auth.getName());
            return user.orElse(null);
        }
        return null;
    }

    /**
     * 查看个人资料页面
     */
    @GetMapping
    public String viewProfile(Model model) {
        User user = getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        return "profile";
    }

    /**
     * 更新用户个人资料
     * @param updatedUser 包含更新后基本信息的 User 对象
     * @param newPassword 若需要修改密码，则提供新密码
     */
    @PostMapping("/update")
    public String updateProfile(@ModelAttribute User updatedUser,
                                @RequestParam(required = false) String newPassword,
                                RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        // 确保只能更新当前登录用户的 ID 对应的数据
        updatedUser.setId(currentUser.getId());
        try {
            userService.updateUserProfile(updatedUser, newPassword);
            redirectAttributes.addFlashAttribute("successMessage", "个人信息更新成功！");
        } catch (Exception e) {
            // 捕获更新过程中的异常并显示错误信息
            redirectAttributes.addFlashAttribute("errorMessage", "更新失败：" + e.getMessage());
        }
        return "redirect:/profile";
    }
}
