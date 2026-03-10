package com.snackshop.service;

import com.snackshop.model.User;
import com.snackshop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Spring Security 用户详情服务实现类。
 * 用于从数据库中加载用户信息，以供 Spring Security 进行身份认证和授权。
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    /**
     * 根据用户名加载用户的详细信息。
     * 
     * @param username 用户名
     * @return 包含用户名、密码和角色的 UserDetails 对象
     * @throws UsernameNotFoundException 如果用户不存在
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 从数据库查询用户，如果不存在则抛出异常
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("找不到用户: " + username));

        // 构建并返回 Spring Security 所需的 UserDetails 对象
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }
}
