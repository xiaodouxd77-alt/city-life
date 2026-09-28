package com.cn.controller.content;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cn.dto.Result;
import com.cn.entity.Blog;
import com.cn.service.BlogService;
import com.cn.utils.common.SystemConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/search")
public class SearchController {

    @Resource
    private BlogService blogService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static final String SEARCH_HOT_KEY = "search:hot";
    private static final String SEARCH_HISTORY_KEY = "search:history:";

    /**
     * 搜索笔记（关键词模糊匹配）
     */
    @GetMapping("/blog")
    public Result searchBlog(@RequestParam String keyword,
                              @RequestParam(defaultValue = "1") Integer current) {
        // 记录搜索词到热门搜索
        stringRedisTemplate.opsForZSet().incrementScore(SEARCH_HOT_KEY, keyword, 1);
        // 搜索，只展示正常状态的笔记
        Page<Blog> page = blogService.query()
                .eq("status", 0)
                .and(w -> w.like("title", keyword).or().like("content", keyword))
                .orderByDesc("create_time")
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        List<Blog> records = page.getRecords();
        blogService.batchQueryBlogUser(records);
        blogService.batchIsBlogLiked(records);
        return Result.ok(records);
    }

    /**
     * 热门搜索词
     */
    @GetMapping("/hot")
    public Result hotSearch() {
        Set<String> hot = stringRedisTemplate.opsForZSet()
                .reverseRange(SEARCH_HOT_KEY, 0, 9);
        return Result.ok(hot);
    }

    /**
     * 记录用户搜索历史
     */
    @PostMapping("/history")
    public Result recordHistory(@RequestBody String keyword) {
        Long userId = com.cn.utils.common.UserHolder.getUser().getId();
        String key = SEARCH_HISTORY_KEY + userId;
        stringRedisTemplate.opsForZSet().add(key, keyword, System.currentTimeMillis());
        // 只保留最近20条
        Long size = stringRedisTemplate.opsForZSet().zCard(key);
        if (size != null && size > 20) {
            stringRedisTemplate.opsForZSet().removeRange(key, 0, size - 21);
        }
        return Result.ok();
    }

    /**
     * 获取用户搜索历史
     */
    @GetMapping("/history")
    public Result getHistory() {
        Long userId = com.cn.utils.common.UserHolder.getUser().getId();
        String key = SEARCH_HISTORY_KEY + userId;
        Set<String> history = stringRedisTemplate.opsForZSet()
                .reverseRange(key, 0, 19);
        return Result.ok(history);
    }
}
