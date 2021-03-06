package com.csl.plus.portal.res.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.csl.plus.portal.res.service.IResProjCategoryService;
import com.csl.plus.res.entity.ResProjectCategory;
import com.csl.plus.res.mapper.ResProjectCategoryMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;


@Service("resProjCategoryService")
public class ResProjCategoryServiceImpl extends ServiceImpl<ResProjectCategoryMapper, ResProjectCategory> implements IResProjCategoryService {

    @Resource
    private ResProjectCategoryMapper resProjCategoryMapper;

    @Transactional
    public boolean saves(ResProjectCategory entity) {
        entity.setCreateDate(new Date());
        resProjCategoryMapper.insert(entity);
        return true;
    }

}