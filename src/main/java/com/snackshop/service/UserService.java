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

/**
 * 用户服务类，处理用户注册、查询、个人信息更新、角色管理等业务逻辑。
 */
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private StoreService storeService;

    /**
     * 注册新用户。
     * 为密码加密，并根据用户角色（如商家）设置初始状态。
     * 
     * @param user 用户实体
     */
    @Transactional
    public void registerUser(User user) {
        // 加密用户密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // 如果未选择角色或尝试通过注册获得管理员权限，默认设为普通用户
        if (user.getRole() == null || user.getRole() == Role.ADMIN) {
            user.setRole(Role.USER);
        }

        // 逻辑调整：如果是商家注册，状态设为 PENDING (待审核)
        if (user.getRole() == Role.MERCHANT) {
            user.setStatus(com.snackshop.model.UserStatus.PENDING);
        } else {
            user.setStatus(com.snackshop.model.UserStatus.APPROVED);
        }

        userRepository.save(user);
        
        // 注意：店铺创建现在延迟到管理员审核通过后
    }

    /**
     * 审核通过商家申请
     * @param id 用户ID
     */
    @Transactional
    public void approveUser(Long id) {
        userRepository.findById(id).ifPresent(user -> {
            user.setStatus(com.snackshop.model.UserStatus.APPROVED);
            userRepository.save(user);
            // 审核通过时，如果是商家且没有店铺，为其创建店铺
            if (user.getRole() == Role.MERCHANT && user.getStore() == null) {
                storeService.createDefaultStore(user);
            }
        });
    }

    /**
     * 驳回商家申请
     * @param id 用户ID
     */
    @Transactional
    public void rejectUser(Long id) {
        userRepository.findById(id).ifPresent(user -> {
            user.setStatus(com.snackshop.model.UserStatus.REJECTED);
            userRepository.save(user);
        });
    }

    /**
     * 根据用户名查找用户。
     * 
     * @param username 用户名
     * @return 包含用户的 Optional 对象
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * 更新用户个人资料。
     * 
     * @param updatedUser 包含新资料的用户对象
     * @param newPassword 新密码（如果需要更新）
     */
    @Transactional
    public void updateUserProfile(User updatedUser, String newPassword) {
        User user = userRepository.findById(updatedUser.getId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        user.setEmail(updatedUser.getEmail());
        // 如果提供了新密码且不为空，则更新加密后的密码
        if (newPassword != null && !newPassword.isEmpty()) {
            user.setPassword(passwordEncoder.encode(newPassword));
        }
        userRepository.save(user);
    }

    /**
     * 获取系统中所有用户的列表。
     * 
     * @return 用户列表
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * 更新指定用户的角色。
     * 
     * @param id 用户ID
     * @param role 新角色
     */
    public void updateUserRole(Long id, Role role) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            User u = user.get();
            u.setRole(role);
            userRepository.save(u);
        }
    }

    /**
     * 删除指定ID的用户。
     * 
     * @param id 用户ID
     */
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
