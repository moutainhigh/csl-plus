package com.csl.plus.res.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.csl.plus.res.entity.ResProduct;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 需求表
 *
 * @author David
 * @email
 * @date 2020-02-15 22:12:07
 */
@Mapper
public interface ResProductMapper extends BaseMapper<ResProduct> {

    public List<ResProduct> getList();
}
