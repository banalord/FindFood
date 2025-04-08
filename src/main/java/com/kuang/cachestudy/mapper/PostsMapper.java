package com.kuang.cachestudy.mapper;

import com.kuang.cachestudy.pojo.Posts;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author Administrator
* @description 针对表【posts】的数据库操作Mapper
* @createDate 2025-04-07 18:59:53
* @Entity com.kuang.cachestudy.pojo.Posts
*/
@Mapper
public interface PostsMapper extends BaseMapper<Posts> {

}




