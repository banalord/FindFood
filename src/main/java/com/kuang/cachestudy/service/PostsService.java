package com.kuang.cachestudy.service;

import com.kuang.cachestudy.pojo.Posts;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author ch
* @description 针对表【posts】的数据库操作Service
* @createDate 2025-04-07 18:59:53
*/
public interface PostsService extends IService<Posts> {

    //点赞方法
    public void incrementLikeCount(Long postId);

    // 获取点赞数
    Integer getLikeCount(Long postId);

}
