package com.snackshop.model;

import javax.persistence.*;

/**
 * 用户收货地址实体类，对应数据库中的 user_addresses 表
 */
@Entity
@Table(name = "user_addresses")
public class UserAddress {
    /**
     * 地址唯一标识符
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 所属用户
     */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 收货人姓名
     */
    private String receiverName;

    /**
     * 收货人电话
     */
    private String receiverPhone;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 区/县
     */
    private String district;

    /**
     * 详细地址
     */
    private String detailAddress;

    /**
     * 是否为默认地址
     */
    private boolean isDefault = false;

    /**
     * 地址标签 (例如：家, 公司)
     */
    private String label;

    /**
     * 默认构造函数
     */
    public UserAddress() {}

    /**
     * 获取地址ID
     * @return 地址ID
     */
    public Long getId() { return id; }

    /**
     * 设置地址ID
     * @param id 地址ID
     */
    public void setId(Long id) { this.id = id; }

    /**
     * 获取所属用户
     * @return 用户对象
     */
    public User getUser() { return user; }

    /**
     * 设置所属用户
     * @param user 用户对象
     */
    public void setUser(User user) { this.user = user; }

    /**
     * 获取收货人姓名
     * @return 收货人姓名
     */
    public String getReceiverName() { return receiverName; }

    /**
     * 设置收货人姓名
     * @param receiverName 收货人姓名
     */
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }

    /**
     * 获取收货人电话
     * @return 收货人电话
     */
    public String getReceiverPhone() { return receiverPhone; }

    /**
     * 设置收货人电话
     * @param receiverPhone 收货人电话
     */
    public void setReceiverPhone(String receiverPhone) { this.receiverPhone = receiverPhone; }

    /**
     * 获取省份
     * @return 省份
     */
    public String getProvince() { return province; }

    /**
     * 设置省份
     * @param province 省份
     */
    public void setProvince(String province) { this.province = province; }

    /**
     * 获取城市
     * @return 城市
     */
    public String getCity() { return city; }

    /**
     * 设置城市
     * @param city 城市
     */
    public void setCity(String city) { this.city = city; }

    /**
     * 获取区/县
     * @return 区/县
     */
    public String getDistrict() { return district; }

    /**
     * 设置区/县
     * @param district 区/县
     */
    public void setDistrict(String district) { this.district = district; }

    /**
     * 获取详细地址
     * @return 详细地址
     */
    public String getDetailAddress() { return detailAddress; }

    /**
     * 设置详细地址
     * @param detailAddress 详细地址
     */
    public void setDetailAddress(String detailAddress) { this.detailAddress = detailAddress; }

    /**
     * 获取是否为默认地址
     * @return true 如果是默认地址
     */
    public boolean isDefault() { return isDefault; }

    /**
     * 设置是否为默认地址
     * @param isDefault 是否默认标志
     */
    public void setDefault(boolean isDefault) { this.isDefault = isDefault; }

    /**
     * 获取地址标签
     * @return 地址标签
     */
    public String getLabel() { return label; }

    /**
     * 设置地址标签
     * @param label 地址标签
     */
    public void setLabel(String label) { this.label = label; }

    /**
     * 获取拼接后的完整地址
     * @return 完整地址字符串
     */
    public String getFullAddress() {
        return (province != null ? province : "") + (city != null ? city : "") + 
               (district != null ? district : "") + detailAddress;
    }
}