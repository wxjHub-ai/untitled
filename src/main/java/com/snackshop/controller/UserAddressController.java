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

@Controller
@RequestMapping("/addresses")
public class UserAddressController {

    @Autowired
    private UserAddressService userAddressService;

    @Autowired
    private UserAddressRepository userAddressRepository;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByUsername(auth.getName()).orElse(null);
    }

    @GetMapping
    public String listAddresses(Model model) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/login";
        model.addAttribute("addresses", userAddressService.getAddressesByUser(user));
        model.addAttribute("newAddress", new UserAddress());
        return "my_addresses";
    }

    @PostMapping("/save")
    public String saveAddress(@ModelAttribute UserAddress address) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/login";
        
        // If updating, verify ownership
        if (address.getId() != null) {
            UserAddress existing = userAddressRepository.findById(address.getId()).orElse(null);
            if (existing == null || !existing.getUser().getId().equals(user.getId())) {
                return "redirect:/addresses";
            }
        }
        
        address.setUser(user);
        userAddressService.saveAddress(address);
        return "redirect:/addresses";
    }

    @GetMapping("/{id}")
    @ResponseBody
    public UserAddress getAddress(@PathVariable Long id) {
        User user = getCurrentUser();
        if (user == null) return null;
        
        UserAddress address = userAddressRepository.findById(id).orElse(null);
        if (address != null && address.getUser().getId().equals(user.getId())) {
            return address;
        }
        return null;
    }

    @GetMapping("/delete/{id}")
    public String deleteAddress(@PathVariable Long id) {
        userAddressService.deleteAddress(id);
        return "redirect:/addresses";
    }
}