package com.kuang.cachestudy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kuang.cachestudy.pojo.Tiezi0;
import com.kuang.cachestudy.service.Tiezi0Service;
import com.kuang.cachestudy.mapper.Tiezi0Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
* @author Administrator
* @description 针对表【tiezi0】的数据库操作Service实现
* @createDate 2025-04-03 21:33:08
*/
@Service
public class Tiezi0ServiceImpl extends ServiceImpl<Tiezi0Mapper, Tiezi0>
    implements Tiezi0Service{

    @Autowired
    private Tiezi0Mapper tiezi0Mapper;
    @Override
    public List<Tiezi0> getTieziFromRange(int from, int to) {
        QueryWrapper<Tiezi0> qw = new QueryWrapper<>();
        QueryWrapper<Tiezi0> Tiezi = qw.between("id", from, to);
        return this.list(Tiezi);
        //return tiezi0Mapper.selectList(Tiezi);
    }
}




