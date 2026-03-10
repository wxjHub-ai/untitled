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

/**
 * 数据初始化配置类
 * 
 * 该类用于在应用程序启动时，向数据库中填充初始的演示数据，
 * 包括商家账号、默认店铺信息、示例商品以及管理员账号。
 */
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

    /**
     * 定义一个 CommandLineRunner Bean，在 Spring Boot 启动完成后执行。
     * 
     * @return 包含初始化逻辑的 CommandLineRunner 实例
     */
    @Bean
    public CommandLineRunner initData() {
        return args -> {
            // --- 步骤 1: 初始化商家账号 ---
            // 检查数据库中是否已存在名为 "merchant" 的用户
            User merchantUser = userRepository.findByUsername("merchant").orElse(null);
            if (merchantUser == null) {
                merchantUser = new User();
                merchantUser.setUsername("merchant");
                // 设置初始密码，UserService.registerUser 内部会对密码进行加密处理
                merchantUser.setPassword("merchant123"); 
                merchantUser.setEmail("merchant@snackshop.com");
                merchantUser.setRole(Role.MERCHANT);
                // 调用服务层方法注册用户，确保业务逻辑的一致性（如创建关联数据）
                userService.registerUser(merchantUser);
                // 重新从数据库加载，以获取生成的主键等信息
                merchantUser = userRepository.findByUsername("merchant").get();
                System.out.println("--- 初始化商家账号: merchant / merchant123 ---");
            }

            // --- 步骤 2: 为商家补全店铺信息 ---
            // 每个商家账号都应该拥有一个关联的店铺
            Store merchantStore = storeService.getStoreByOwner(merchantUser);
            if (merchantStore == null && merchantUser.getRole() == Role.MERCHANT) {
                // 如果商家没有店铺，则为其创建一个默认店铺
                merchantStore = storeService.createDefaultStore(merchantUser);
                System.out.println("--- 为现有商家补全店铺信息 ---");
            }

            // --- 步骤 3: 初始化商品数据 ---
            // 如果商品表为空，则添加一些经典的零食数据作为演示
            if (productRepository.count() == 0) {
                // 创建辣条商品
                Product p1 = new Product("卫龙大面筋辣条", "经典儿时回忆", new BigDecimal("5.50"), 100, "/product_images/0a72c1c7-5a20-4749-acbe-7dc86b3d1520.jpeg", "辣条");
                p1.setStore(merchantStore);
                
                // 创建坚果商品
                Product p2 = new Product("三只松鼠每日坚果", "健康混合坚果", new BigDecimal("29.90"), 50, "/product_images/352486be-3f3f-4faa-b6bc-0ee83c20f50f.png", "坚果");
                p2.setStore(merchantStore);

                // 创建膨化食品商品
                Product p3 = new Product("乐事薯片原味", "经典原味", new BigDecimal("7.80"), 200, "/product_images/4342e65f-d338-4eeb-9ae5-0ed09093ee6d.jpg", "膨化食品");
                p3.setStore(merchantStore);
                
                // 创建饼干商品
                Product p4 = new Product("奥利奥夹心饼干", "经典巧克力味", new BigDecimal("12.50"), 150, "/product_images/50df0848-437f-40c4-839c-f888a9fdfdac.webp", "饼干");
                p4.setStore(merchantStore);

                // 批量保存商品到数据库
                productRepository.save(p1);
                productRepository.save(p2);
                productRepository.save(p3);
                productRepository.save(p4);
                System.out.println("--- 初始化示例商品数据完成 ---");
            }

            // --- 步骤 4: 初始化管理员账号 ---
            // 检查是否存在管理员账号 "admin"
            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                // 这里手动使用 passwordEncoder 进行加密，演示不同的加密调用方式
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setEmail("admin@snackshop.com");
                admin.setRole(Role.ADMIN);
                userRepository.save(admin);
                System.out.println("--- 初始化管理员账号: admin / admin123 ---");
            }
        };
    }
}