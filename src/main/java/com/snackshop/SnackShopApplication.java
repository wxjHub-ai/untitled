package com.snackshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 零食小铺 (Snack Shop) 应用程序的主入口启动类。
 * 
 * @SpringBootApplication 是一个复合注解，包含了以下核心功能：
 * 1. @SpringBootConfiguration: 标记这是一个 Spring Boot 配置类。
 * 2. @EnableAutoConfiguration: 开启 Spring Boot 的自动配置机制。
 * 3. @ComponentScan: 自动扫描当前包及其子包中的 Spring 组件（如 Controller, Service, Repository 等）。
 */
@SpringBootApplication
public class SnackShopApplication {

    /**
     * 应用程序的主入口方法。
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        // 运行 Spring 应用程序，启动嵌入式 Tomcat 服务器并初始化 Spring 上下文。
        SpringApplication.run(SnackShopApplication.class, args);
    }

}
