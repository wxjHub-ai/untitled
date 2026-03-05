package com.snackshop.controller;

import com.snackshop.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.bind.annotation.RequestParam;

/**
 * 首页控制器，处理面向普通用户的展示请求
 * @Controller 表示这是一个处理 Web 请求的控制器类
 */
@Controller
public class HomeController {

    // 自动注入商品服务类
    @Autowired
    private ProductService productService;

    /**
     * 处理首页请求 (支持搜索和分类过滤)
     * @param category 分类（可选）
     * @param query 搜索关键词（可选）
     * @param model 用于将数据传递给前端页面
     * @return 返回 home.html 模板页面
     */
    @GetMapping("/")
    public String home(@RequestParam(required = false) String category,
                       @RequestParam(required = false) String query,
                       Model model) {
        // 调用搜索服务获取匹配的商品
        model.addAttribute("products", productService.searchProducts(category, query));
        // 将当前的搜索条件传回前端，用于高亮显示或保留输入框内容
        model.addAttribute("selectedCategory", category != null ? category : "全部");
        model.addAttribute("searchQuery", query);
        return "home";
    }

    /**
     * 处理商品详情页面请求 (访问 "/product/{id}")
     * @param id 商品 ID，通过 URL 路径获取
     * @param model 传递数据到页面
     * @return 返回 product_details.html 页面名称
     */
    @GetMapping("/product/{id}")
    public String productDetails(@PathVariable Long id, Model model) {
        // 根据 ID 获取商品，如果不存在则返回 null
        model.addAttribute("product", productService.getProductById(id).orElse(null));
        return "product_details";
    }
}
