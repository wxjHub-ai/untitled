package com.snackshop.service;

import com.snackshop.model.Store;
import com.snackshop.model.User;
import com.snackshop.repository.StoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StoreService {
    @Autowired
    private StoreRepository storeRepository;

    public Store getStoreByOwner(User owner) {
        return storeRepository.findByOwner(owner).orElse(null);
    }

    @Transactional
    public Store createDefaultStore(User owner) {
        Store store = new Store();
        String storeName = (owner.getStoreName() != null && !owner.getStoreName().isEmpty()) 
                           ? owner.getStoreName() 
                           : owner.getUsername() + "的店铺";
        store.setName(storeName);
        store.setDescription("欢迎光临我的小店！");
        store.setOwner(owner);
        return storeRepository.save(store);
    }

    @Transactional
    public void updateStore(Store store) {
        storeRepository.save(store);
    }
}