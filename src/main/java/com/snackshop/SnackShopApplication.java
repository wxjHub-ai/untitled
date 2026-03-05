package com.snackshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 零食小铺应用程序的启动类
 * @SpringBootApplication 是一个组合注解，它包括了:
 * 1. @Configuration: 标记这是一个配置类
 * 2. @EnableAutoConfiguration: 开启自动配置，Spring Boot 会根据依赖自动设置项目
 * 3. @ComponentScan: 自动扫描当前包及其子包下的 Spring 组件（如 Controller, Service, Repository 等）
 */
@SpringBootApplication
public class SnackShopApplication {

    public static void main(String[] args) {
        // 启动 Spring 应用，初始化 Spring 容器
        SpringApplication.run(SnackShopApplication.class, args);
    }

}
