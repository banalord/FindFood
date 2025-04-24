package com.kuang.cachestudy.control;

import com.kuang.cachestudy.service.PostsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class PostController {
    @Resource
    private PostsService postsService;

    // 点赞操作
    @PostMapping("/{id}/like")
    public String likePost(@PathVariable Long id) {
        postsService.incrementLikeCount(id);
        return "点赞成功！";
    }

    // 获取点赞数
    @GetMapping("/{id}/like-count")
    public String getLikeCount(@PathVariable Long id) {
        Integer likeCount = postsService.getLikeCount(id);
        return "点赞数: " + likeCount;
    }
}
