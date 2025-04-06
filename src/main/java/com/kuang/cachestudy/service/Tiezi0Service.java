package com.kuang.cachestudy.service;

import com.kuang.cachestudy.pojo.Tiezi0;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
* @author Administrator
* @description 针对表【tiezi0】的数据库操作Service
* @createDate 2025-04-03 21:33:08
*/
public interface Tiezi0Service extends IService<Tiezi0> {
    List<Tiezi0> getTieziFromRange(int from,int to);
}
