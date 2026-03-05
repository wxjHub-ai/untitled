package com.snackshop.repository;

import com.snackshop.model.User;
import com.snackshop.model.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {
    List<UserAddress> findByUser(User user);
    List<UserAddress> findByUserAndIsDefaultTrue(User user);
}