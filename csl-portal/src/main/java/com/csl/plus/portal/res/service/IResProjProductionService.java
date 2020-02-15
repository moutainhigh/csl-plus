package com.csl.plus.portal.res.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.csl.plus.res.entity.ResProjProduction;


/**
 * 项目表
 *
 * @author David
 * @email 
 * @date 2020-02-15 22:12:07
 */
public interface IResProjProductionService extends IService<ResProjProduction> {

    
    boolean saves(ResProjProduction entity);
}
