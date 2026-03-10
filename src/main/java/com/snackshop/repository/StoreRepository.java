package com.snackshop.repository;

import com.snackshop.model.Store;
import com.snackshop.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * 店铺仓库接口
 */
public interface StoreRepository extends JpaRepository<Store, Long> {
    /**
     * 根据所有者（用户）查找店铺
     * @param owner 用户对象
     * @return 店铺的可选包装对象
     */
    Optional<Store> findByOwner(User owner);
}