package com.snackshop.model;

/**
 * 用户状态枚举类，用于控制用户的账户生命周期，特别是商家的入驻审核。
 */
public enum UserStatus {
    /**
     * 待审核：商家注册后的初始状态，需要管理员审批。
     */
    PENDING,
    /**
     * 已通过：账号处于正常激活状态，可以正常登录和使用功能。
     */
    APPROVED,
    /**
     * 已驳回：商家的入驻申请被管理员拒绝。
     */
    REJECTED,
    /**
     * 已禁用：账号被管理员手动封禁。
     */
    DISABLED
}
