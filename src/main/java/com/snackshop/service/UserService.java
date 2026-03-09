package com.snackshop.service;

import com.snackshop.model.Role;
import com.snackshop.model.User;
import com.snackshop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private StoreService storeService;

    @Transactional
    public void registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // If no role is selected or someone tries to hack ADMIN role via registration
        if (user.getRole() == null || user.getRole() == Role.ADMIN) {
            user.setRole(Role.USER);
        }
        User savedUser = userRepository.save(user);

        // If merchant, create a default store
        if (savedUser.getRole() == Role.MERCHANT) {
            storeService.createDefaultStore(user); // Use user which contains storeName
        }
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional
    public void updateUserProfile(User updatedUser, String newPassword) {
        User user = userRepository.findById(updatedUser.getId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        user.setEmail(updatedUser.getEmail());
        if (newPassword != null && !newPassword.isEmpty()) {
            user.setPassword(passwordEncoder.encode(newPassword));
        }
        userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void updateUserRole(Long id, Role role) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            User u = user.get();
            u.setRole(role);
            userRepository.save(u);
        }
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
