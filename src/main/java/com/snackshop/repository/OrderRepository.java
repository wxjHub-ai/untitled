package com.snackshop.repository;

import com.snackshop.model.Order;
import com.snackshop.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * 订单仓库接口
 */
public interface OrderRepository extends JpaRepository<Order, Long> {
    /**
     * 根据用户查找订单列表，并按订单日期降序排列
     * @param user 用户对象
     * @return 订单列表
     */
    List<Order> findByUserOrderByOrderDateDesc(User user);

    /**
     * 查找所有订单，并按订单日期降序排列
     * @return 所有订单列表
     */
    List<Order> findAllByOrderByOrderDateDesc();
    
    /**
     * 根据状态查找订单，并按订单日期降序排列
     * @param status 订单状态
     * @return 订单列表
     */
    List<Order> findByStatusOrderByOrderDateDesc(String status);

    /**
     * 查找在指定日期范围内的所有订单，并按订单日期降序排列
     * @param start 开始日期时间
     * @param end 结束日期时间
     * @return 订单列表
     */
    List<Order> findByOrderDateBetweenOrderByOrderDateDesc(java.time.LocalDateTime start, java.time.LocalDateTime end);

    /**
     * 根据状态查找在指定日期范围内的订单，并按订单日期降序排列
     * @param status 订单状态
     * @param start 开始日期时间
     * @param end 结束日期时间
     * @return 订单列表
     */
    List<Order> findByStatusAndOrderDateBetweenOrderByOrderDateDesc(String status, java.time.LocalDateTime start, java.time.LocalDateTime end);
}
