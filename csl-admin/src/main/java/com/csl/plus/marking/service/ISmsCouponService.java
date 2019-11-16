package com.csl.plus.marking.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.csl.plus.marking.entity.SmsCoupon;
import com.csl.plus.marking.vo.SmsCouponParam;

import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * 优惠卷表 服务类
 * </p>
 *
 * @since 2019-04-19
 */
public interface ISmsCouponService extends IService<SmsCoupon> {

    boolean saves(SmsCouponParam entity);

    boolean updateByIds(SmsCouponParam entity);

    /**
     * 获取优惠券详情
     *
     * @param id 优惠券表id
     */
    SmsCouponParam getItem(Long id);

    /**
     * 根据优惠券id删除优惠券
     */
    @Transactional
    int delete(Long id);

}
