package com.snackshop.repository;

import com.snackshop.model.User;
import com.snackshop.model.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * 用户地址仓库接口
 */
public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {
    /**
     * 根据用户查找地址列表
     * @param user 用户对象
     * @return 地址列表
     */
    List<UserAddress> findByUser(User user);

    /**
     * 查找用户的默认地址
     * @param user 用户对象
     * @return 默认地址列表
     */
    List<UserAddress> findByUserAndIsDefaultTrue(User user);
}