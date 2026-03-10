package com.snackshop.controller;

import com.snackshop.model.User;
import com.snackshop.model.UserAddress;
import com.snackshop.repository.UserAddressRepository;
import com.snackshop.repository.UserRepository;
import com.snackshop.service.UserAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * 用户地址控制器
 * 处理用户收货地址的管理（查看、保存、获取、删除）
 */
@Controller
@RequestMapping("/addresses")
public class UserAddressController {

    @Autowired
    private UserAddressService userAddressService;

    @Autowired
    private UserAddressRepository userAddressRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 获取当前登录用户
     */
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByUsername(auth.getName()).orElse(null);
    }

    /**
     * 显示用户的收货地址列表页面
     */
    @GetMapping
    public String listAddresses(Model model) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/login";
        // 加载用户的所有地址
        model.addAttribute("addresses", userAddressService.getAddressesByUser(user));
        // 提供一个空对象用于新增地址表单
        model.addAttribute("newAddress", new UserAddress());
        return "my_addresses";
    }

    /**
     * 保存或更新收货地址
     */
    @PostMapping("/save")
    public String saveAddress(@ModelAttribute UserAddress address) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/login";
        
        // 如果是更新现有地址，需要验证该地址是否属于当前用户
        if (address.getId() != null) {
            UserAddress existing = userAddressRepository.findById(address.getId()).orElse(null);
            if (existing == null || !existing.getUser().getId().equals(user.getId())) {
                return "redirect:/addresses";
            }
        }
        
        // 绑定所属用户并保存
        address.setUser(user);
        userAddressService.saveAddress(address);
        return "redirect:/addresses";
    }

    /**
     * 获取特定地址的 JSON 数据（用于前端编辑回显）
     */
    @GetMapping("/{id}")
    @ResponseBody
    public UserAddress getAddress(@PathVariable Long id) {
        User user = getCurrentUser();
        if (user == null) return null;
        
        UserAddress address = userAddressRepository.findById(id).orElse(null);
        // 权限检查：确保只能获取属于当前用户的地址
        if (address != null && address.getUser().getId().equals(user.getId())) {
            return address;
        }
        return null;
    }

    /**
     * 删除指定的收货地址
     */
    @GetMapping("/delete/{id}")
    public String deleteAddress(@PathVariable Long id) {
        userAddressService.deleteAddress(id);
        return "redirect:/addresses";
    }
}
