package com.snackshop.config;

import com.snackshop.model.*;
import com.snackshop.repository.*;
import com.snackshop.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

@Configuration
public class DataInitializer {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private StoreRepository storeRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserService userService;
    @Autowired
    private StoreService storeService;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            // Initialize Merchant
            User merchantUser = userRepository.findByUsername("merchant").orElse(null);
            if (merchantUser == null) {
                merchantUser = new User();
                merchantUser.setUsername("merchant");
                merchantUser.setPassword("merchant123"); // Service will encode it
                merchantUser.setEmail("merchant@snackshop.com");
                merchantUser.setRole(Role.MERCHANT);
                userService.registerUser(merchantUser);
                merchantUser = userRepository.findByUsername("merchant").get();
                System.out.println("--- 初始化商家账号: merchant / merchant123 ---");
            }

            Store merchantStore = storeService.getStoreByOwner(merchantUser);

            // Initialize Products
            if (productRepository.count() == 0) {
                Product p1 = new Product("卫龙大面筋辣条", "经典儿时回忆", new BigDecimal("5.50"), 100, "https://img14.360buyimg.com/n0/jfs/t1/157567/2/22752/155097/61beed2eE10459347/a353683610931551.jpg", "辣条");
                p1.setStore(merchantStore);
                
                Product p2 = new Product("三只松鼠每日坚果", "健康混合坚果", new BigDecimal("29.90"), 50, "https://img14.360buyimg.com/n0/jfs/t1/135967/26/18260/256877/5fca087fE896350d6/d344585141258d4a.jpg", "坚果");
                p2.setStore(merchantStore);

                Product p3 = new Product("乐事薯片原味", "经典原味", new BigDecimal("7.80"), 200, "https://img14.360buyimg.com/n0/jfs/t1/211145/26/8788/247963/61824383E65543c8d/09a066491e0a2489.jpg", "膨化食品");
                
                Product p4 = new Product("奥利奥夹心饼干", "经典巧克力味", new BigDecimal("12.50"), 150, "https://img14.360buyimg.com/n0/jfs/t1/169829/4/23573/193214/61a72d3fE2161f366/0912198083818e8d.jpg", "饼干");

                productRepository.save(p1);
                productRepository.save(p2);
                productRepository.save(p3);
                productRepository.save(p4);
                System.out.println("--- 初始化示例商品数据完成 ---");
            }

            // Initialize Admin User
            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setEmail("admin@snackshop.com");
                admin.setRole(Role.ADMIN);
                userRepository.save(admin);
                System.out.println("--- 初始化管理员账号: admin / admin123 ---");
            }
        };
    }
}