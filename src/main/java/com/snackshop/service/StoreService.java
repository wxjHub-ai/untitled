package com.snackshop.service;

import com.snackshop.model.Store;
import com.snackshop.model.User;
import com.snackshop.repository.StoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 店铺服务类，处理店铺的查询、创建和更新逻辑。
 */
@Service
public class StoreService {
    @Autowired
    private StoreRepository storeRepository;

    /**
     * 根据店主（用户）获取其关联的店铺信息。
     * 
     * @param owner 店主用户对象
     * @return 店铺对象，如果不存在则返回 null
     */
    public Store getStoreByOwner(User owner) {
        return storeRepository.findByOwner(owner).orElse(null);
    }

    /**
     * 为店主创建一个默认店铺。
     * 如果店主提供了店铺名称则使用之，否则根据用户名生成默认名称。
     * 
     * @param owner 店主用户对象
     * @return 已保存的店铺对象
     */
    @Transactional
    public Store createDefaultStore(User owner) {
        Store store = new Store();
        // 优先使用用户在注册时提供的店铺名称
        String storeName = (owner.getStoreName() != null && !owner.getStoreName().isEmpty()) 
                           ? owner.getStoreName() 
                           : owner.getUsername() + "的店铺";
        store.setName(storeName);
        store.setDescription("欢迎光临我的小店！");
        store.setOwner(owner);
        return storeRepository.save(store);
    }

    /**
     * 更新店铺信息。
     * 
     * @param store 包含新信息的店铺对象
     */
    @Transactional
    public void updateStore(Store store) {
        storeRepository.save(store);
    }
}