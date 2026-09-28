package com.cn.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cn.dto.Result;
import com.cn.entity.Blog;
import com.cn.entity.BlogComments;
import com.cn.entity.Shop;
import com.cn.entity.User;
import com.cn.entity.Voucher;
import com.cn.entity.VoucherOrder;
import com.cn.service.BlogCommentsService;
import com.cn.service.BlogService;
import com.cn.service.ShopService;
import com.cn.service.FollowService;
import com.cn.service.VoucherOrderService;
import com.cn.service.VoucherService;
import com.cn.service.UserService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/stats")
public class StatisticsController {

    @Resource private UserService userService;
    @Resource private ShopService shopService;
    @Resource private BlogService blogService;
    @Resource private VoucherOrderService voucherOrderService;
    @Resource private VoucherService voucherService;
    @Resource private FollowService followService;
    @Resource private BlogCommentsService blogCommentsService;

    @GetMapping("/dashboard")
    public Result dashboard() {
        Map<String, Object> data = new HashMap<>();
        data.put("users", userService.count());
        data.put("shops", shopService.count());
        data.put("blogs", blogService.count());
        data.put("orders", voucherOrderService.count());
        data.put("follows", followService.count());
        data.put("comments", blogCommentsService.count());
        return Result.ok(data);
    }

    @GetMapping("/users")
    public Result users(@RequestParam(value = "page", defaultValue = "1") long current,
                        @RequestParam(value = "size", defaultValue = "10") long size,
                        @RequestParam(value = "keyword", required = false) String keyword) {
        // 按昵称或手机号分页查询后台用户列表。
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .and(StringUtils.hasText(keyword), query -> query
                        .like(User::getNickName, keyword)
                        .or()
                        .like(User::getPhone, keyword))
                .orderByDesc(User::getId);
        return pageResult(userService.page(createPage(current, size), wrapper));
    }

    @GetMapping("/blogs")
    public Result blogs(@RequestParam(value = "page", defaultValue = "1") long current,
                        @RequestParam(value = "size", defaultValue = "10") long size,
                        @RequestParam(value = "keyword", required = false) String keyword) {
        // 按标题或正文分页查询后台笔记列表。
        LambdaQueryWrapper<Blog> wrapper = new LambdaQueryWrapper<Blog>()
                .and(StringUtils.hasText(keyword), query -> query
                        .like(Blog::getTitle, keyword)
                        .or()
                        .like(Blog::getContent, keyword))
                .orderByDesc(Blog::getId);
        return pageResult(blogService.page(createPage(current, size), wrapper));
    }

    @GetMapping("/comments")
    public Result comments(@RequestParam(value = "page", defaultValue = "1") long current,
                           @RequestParam(value = "size", defaultValue = "10") long size,
                           @RequestParam(value = "keyword", required = false) String keyword) {
        // 按评论内容分页查询后台评论列表。
        LambdaQueryWrapper<BlogComments> wrapper = new LambdaQueryWrapper<BlogComments>()
                .like(StringUtils.hasText(keyword), BlogComments::getContent, keyword)
                .orderByDesc(BlogComments::getId);
        return pageResult(blogCommentsService.page(createPage(current, size), wrapper));
    }

    @GetMapping("/shops")
    public Result shops(@RequestParam(value = "page", defaultValue = "1") long current,
                        @RequestParam(value = "size", defaultValue = "10") long size,
                        @RequestParam(value = "keyword", required = false) String keyword,
                        @RequestParam(value = "typeId", required = false) Long typeId) {
        // 按名称、地址和店铺分类分页查询后台商户列表。
        LambdaQueryWrapper<Shop> wrapper = new LambdaQueryWrapper<Shop>()
                .and(StringUtils.hasText(keyword), query -> query
                        .like(Shop::getName, keyword)
                        .or()
                        .like(Shop::getAddress, keyword))
                .eq(typeId != null, Shop::getTypeId, typeId)
                .orderByDesc(Shop::getId);
        return pageResult(shopService.page(createPage(current, size), wrapper));
    }

    @GetMapping("/orders")
    public Result orders(@RequestParam(value = "page", defaultValue = "1") long current,
                         @RequestParam(value = "size", defaultValue = "10") long size,
                         @RequestParam(value = "keyword", required = false) String keyword,
                         @RequestParam(value = "status", required = false) Integer status) {
        // 按订单号、用户 ID 和订单状态分页查询后台订单列表。
        LambdaQueryWrapper<VoucherOrder> wrapper = new LambdaQueryWrapper<VoucherOrder>()
                .and(StringUtils.hasText(keyword), query -> query
                        .like(VoucherOrder::getId, keyword)
                        .or()
                        .like(VoucherOrder::getUserId, keyword))
                .eq(status != null, VoucherOrder::getStatus, status)
                .orderByDesc(VoucherOrder::getId);
        return pageResult(voucherOrderService.page(createPage(current, size), wrapper));
    }

    @GetMapping("/vouchers")
    public Result vouchers(@RequestParam(value = "page", defaultValue = "1") long current,
                           @RequestParam(value = "size", defaultValue = "10") long size,
                           @RequestParam(value = "keyword", required = false) String keyword) {
        // 按标题分页查询后台优惠券列表。
        LambdaQueryWrapper<Voucher> wrapper = new LambdaQueryWrapper<Voucher>()
                .like(StringUtils.hasText(keyword), Voucher::getTitle, keyword)
                .orderByDesc(Voucher::getId);
        return pageResult(voucherService.page(createPage(current, size), wrapper));
    }

    /**
     * 约束管理员列表的分页参数，避免一次请求读取过多数据。
     */
    private <T> Page<T> createPage(long current, long size) {
        long validCurrent = Math.max(current, 1);
        long validSize = Math.min(Math.max(size, 1), 100);
        return new Page<>(validCurrent, validSize);
    }

    /**
     * 将 MyBatis-Plus 分页结果转换为统一返回结构。
     */
    private Result pageResult(IPage<?> page) {
        return Result.ok(page.getRecords(), page.getTotal());
    }

    @GetMapping("/trend")
    public Result trend() {
        LocalDate today = LocalDate.now();
        List<String> days = new ArrayList<>();
        List<Long> newUsers = new ArrayList<>();
        List<Long> newBlogs = new ArrayList<>();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            days.add(date.format(DateTimeFormatter.ofPattern("MM-dd")));

            long userCount = userService.query()
                    .ge("create_time", date + " 00:00:00")
                    .le("create_time", date + " 23:59:59")
                    .count();
            newUsers.add(userCount);

            long blogCount = blogService.query()
                    .ge("create_time", date + " 00:00:00")
                    .le("create_time", date + " 23:59:59")
                    .count();
            newBlogs.add(blogCount);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("days", days);
        data.put("newUsers", newUsers);
        data.put("newBlogs", newBlogs);
        return Result.ok(data);
    }

    @GetMapping("/overview")
    public Result overview() {
        String monthAgo = LocalDate.now().minusDays(30).toString();

        // 活跃用户：近30天内发过笔记 或 发过评论的去重用户（两集合合并去重）
        Set<Long> activeUserIds = blogService.query()
                .ge("create_time", monthAgo + " 00:00:00")
                .list()
                .stream()
                .map(Blog::getUserId)
                .collect(Collectors.toCollection(HashSet::new));
        blogCommentsService.query()
                .ge("create_time", monthAgo + " 00:00:00")
                .list()
                .stream()
                .map(BlogComments::getUserId)
                .forEach(activeUserIds::add);

        long orderUsers = voucherOrderService.query()
                .ge("create_time", monthAgo + " 00:00:00")
                .list()
                .stream()
                .map(VoucherOrder::getUserId)
                .distinct()
                .count();

        Map<String, Object> data = new HashMap<>();
        data.put("activeUsers", activeUserIds.size());
        data.put("orderUsers", orderUsers);
        return Result.ok(data);
    }
}
