package com.snackshop.model;

import javax.persistence.*;
import java.util.Set;

/**
 * 用户实体类，对应数据库中的 users 表
 * @Entity 表示这是一个 JPA 实体类，将映射到数据库表
 */
@Entity
@Table(name = "users")
public class User {
    // 主键，自动递增
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 用户名，唯一且不能为空
    @Column(unique = true, nullable = false)
    private String username;

    // 密码，不能为空
    @Column(nullable = false)
    private String password;

    // 电子邮件
    private String email;

    // 用户的角色（ADMIN 或 USER），以字符串形式存储在数据库中
    @Enumerated(EnumType.STRING)
    private Role role;

    // 默认构造函数，JPA 必须
    public User() {}

    // 带参数的构造函数，方便在代码中创建用户对象
    public User(String username, String password, String email, Role role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
    }

    // Getter 和 Setter 方法，用于获取和设置对象的属性值
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
