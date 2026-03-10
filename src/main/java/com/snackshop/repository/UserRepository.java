package com.snackshop.repository;

import com.snackshop.model.User;
import com.snackshop.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * 用户仓库接口
 */
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * 根据用户名查找用户
     * @param username 用户名
     * @return 用户对象的可选包装
     */
    Optional<User> findByUsername(String username);

    /**
     * 统计指定角色的用户数量
     * @param role 角色对象
     * @return 用户数量
     */
    long countByRole(Role role);
}
