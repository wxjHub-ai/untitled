package com.snackshop.config;

import com.snackshop.service.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 安全配置类，控制网站的访问权限和用户登录验证
 * @Configuration 表示这是一个配置类
 * @EnableWebSecurity 开启 Spring Security 的 Web 安全支持
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    /**
     * 定义用于加载用户详细信息的 Service
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return new UserDetailsServiceImpl();
    }

    /**
     * 定义密码加密器 (使用 BCrypt 强哈希算法)
     * 存储在数据库中的密码将是加密后的密文，安全性更高
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 配置身份验证提供者，结合了用户信息服务和密码加密器
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * 将配置好的验证提供者注册到认证管理器中
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authenticationProvider());
    }

    /**
     * 配置具体的 HTTP 访问控制权限
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                // 允许所有人访问首页、注册页以及静态资源 (CSS, JS, 图片)
                .antMatchers("/", "/register", "/css/**", "/js/**", "/images/**", "/product_images/**").permitAll()
                // 只有角色为 "ADMIN" (管理员) 或 "MERCHANT" (商家) 的用户才能访问以 "/admin/" 开头的路径
                .antMatchers("/admin/**").hasAnyRole("ADMIN", "MERCHANT")
                // 其他所有请求都必须登录后才能访问
                .anyRequest().authenticated()
                .and()
                // 配置基于表单的登录
                .formLogin()
                    .loginPage("/login") // 自定义登录页面的路径
                    .defaultSuccessUrl("/?loginSuccess=true") // 登录成功后默认跳转到首页并带上成功标志
                    .permitAll() // 登录页面允许所有人访问
                .and()
                // 配置退出登录
                .logout()
                    .logoutSuccessUrl("/login?logout") // 退出成功后跳转到登录页并带上标志
                    .permitAll(); // 登出功能允许所有人访问
    }
}
