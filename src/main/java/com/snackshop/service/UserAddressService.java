package com.snackshop.service;

import com.snackshop.model.User;
import com.snackshop.model.UserAddress;
import com.snackshop.repository.UserAddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户地址服务类，管理用户的收货地址，包括查询、设为默认、保存和删除。
 */
@Service
public class UserAddressService {
    @Autowired
    private UserAddressRepository userAddressRepository;

    /**
     * 获取指定用户的所有收货地址。
     * 
     * @param user 用户对象
     * @return 地址列表
     */
    public List<UserAddress> getAddressesByUser(User user) {
        return userAddressRepository.findByUser(user);
    }

    /**
     * 获取指定用户的默认收货地址。
     * 
     * @param user 用户对象
     * @return 默认地址对象，若不存在则返回 null
     */
    public UserAddress getDefaultAddress(User user) {
        return userAddressRepository.findByUserAndIsDefaultTrue(user).stream()
                .findFirst()
                .orElse(null);
    }

    /**
     * 保存收货地址。
     * 如果新地址设为默认，则需将该用户已有的其他默认地址设为非默认。
     * 
     * @param address 要保存的地址对象
     */
    @Transactional
    public void saveAddress(UserAddress address) {
        if (address.isDefault()) {
            // 重置该用户已有的所有默认地址为非默认
            userAddressRepository.findByUser(address.getUser()).forEach(a -> {
                if (a.isDefault()) {
                    a.setDefault(false);
                    userAddressRepository.save(a);
                }
            });
        }
        userAddressRepository.save(address);
    }

    /**
     * 根据 ID 删除收货地址。
     * 
     * @param id 地址ID
     */
    @Transactional
    public void deleteAddress(Long id) {
        userAddressRepository.deleteById(id);
    }
}