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
 * 安全配置类，用于控制网站的访问权限、用户登录认证及退出登录逻辑。
 * 
 * @Configuration 标识这是一个 Spring 配置类。
 * @EnableWebSecurity 启用 Spring Security 的 Web 安全支持，并提供 Spring MVC 集成。
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    /**
     * 配置并向 Spring 容器注册自定义的 UserDetailsService 实现。
     * UserDetailsService 用于根据用户名在数据库中查找用户信息。
     * 
     * @return UserDetailsServiceImpl 实例
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return new UserDetailsServiceImpl();
    }

    /**
     * 配置并向 Spring 容器注册密码加密器。
     * 使用 BCrypt 强哈希算法进行加密。BCrypt 会自动处理加盐（salt），
     * 从而防止彩虹表攻击，确保数据库中存储的密码安全性。
     * 
     * @return BCryptPasswordEncoder 实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 配置身份验证提供者 (AuthenticationProvider)。
     * DaoAuthenticationProvider 是 Spring Security 提供的一个简单实现，
     * 它通过 UserDetailsService 获取用户信息，并使用 PasswordEncoder 进行密码比对。
     * 
     * @return DaoAuthenticationProvider 实例
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        // 设置自定义的用户信息服务
        authProvider.setUserDetailsService(userDetailsService());
        // 设置密码加密器
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * 配置身份验证管理器 (AuthenticationManager)。
     * 将上面定义的身份验证提供者注册到认证管理器中。
     * 
     * @param auth 身份验证管理器构建器
     * @throws Exception 如果配置过程中出错
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authenticationProvider());
    }

    /**
     * 配置具体的 HTTP 访问控制权限 (URL 拦截规则)。
     * 
     * @param http HttpSecurity 对象，用于构建安全策略
     * @throws Exception 如果配置过程中出错
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                // --- 开放访问权限 ---
                // 允许所有人（匿名访问）访问首页、注册页以及所有的静态资源文件夹
                .antMatchers("/", "/register", "/css/**", "/js/**", "/images/**", "/product_images/**").permitAll()
                
                // --- 角色权限控制 ---
                // 只有拥有 "ADMIN" 或 "MERCHANT" 角色的用户才能进入管理后台 ("/admin/**")
                .antMatchers("/admin/**").hasAnyRole("ADMIN", "MERCHANT")
                
                // --- 其他请求控制 ---
                // 除了上述明确定义的路径外，其他所有请求都必须经过登录认证
                .anyRequest().authenticated()
                .and()
                
                // --- 登录配置 ---
                .formLogin()
                    // 指定自定义登录页面的访问路径
                    .loginPage("/login") 
                    // 登录成功后的默认跳转目标路径，true 表示总是跳转到此路径
                    .defaultSuccessUrl("/?loginSuccess=true") 
                    // 登录页面本身必须允许所有人访问，否则会陷入死循环
                    .permitAll() 
                .and()
                
                // --- 登出配置 ---
                .logout()
                    // 指定退出登录后的跳转路径，并带上退出参数以便前端显示提示信息
                    .logoutSuccessUrl("/login?logout") 
                    // 登出功能也必须允许所有人访问
                    .permitAll();
    }
}
