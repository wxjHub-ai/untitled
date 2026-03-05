package com.snackshop.service;

import com.snackshop.model.User;
import com.snackshop.model.UserAddress;
import com.snackshop.repository.UserAddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserAddressService {
    @Autowired
    private UserAddressRepository userAddressRepository;

    public List<UserAddress> getAddressesByUser(User user) {
        return userAddressRepository.findByUser(user);
    }

    public UserAddress getDefaultAddress(User user) {
        return userAddressRepository.findByUserAndIsDefaultTrue(user).stream()
                .findFirst()
                .orElse(null);
    }

    @Transactional
    public void saveAddress(UserAddress address) {
        if (address.isDefault()) {
            // Reset existing defaults
            userAddressRepository.findByUser(address.getUser()).forEach(a -> {
                if (a.isDefault()) {
                    a.setDefault(false);
                    userAddressRepository.save(a);
                }
            });
        }
        userAddressRepository.save(address);
    }

    @Transactional
    public void deleteAddress(Long id) {
        userAddressRepository.deleteById(id);
    }
}