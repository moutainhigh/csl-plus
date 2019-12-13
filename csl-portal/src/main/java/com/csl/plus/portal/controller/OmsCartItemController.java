package com.csl.plus.portal.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.csl.plus.oms.entity.OmsCartItem;
import com.csl.plus.oms.vo.CartProduct;
import com.csl.plus.oms.vo.CartPromotionItem;
import com.csl.plus.pms.entity.PmsSkuStock;
import com.csl.plus.portal.oms.service.IOmsCartItemService;
import com.csl.plus.portal.pms.service.IPmsSkuStockService;
import com.csl.plus.portal.ums.service.IUmsMemberService;
import com.csl.plus.portal.util.UserUtils;
import com.csl.plus.ums.entity.UmsMember;
import com.csl.plus.utils.CommonResult;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 购物车管理Controller
 */
@Controller
@Api(tags = "OmsCartItemController", description = "购物车管理")
@RequestMapping("/api/cart")
public class OmsCartItemController {
	@Autowired
	private IOmsCartItemService cartItemService;
	@Autowired
	private IUmsMemberService memberService;

	@Autowired
	private IPmsSkuStockService pmsSkuStockService;

	@ApiOperation("添加商品到购物车")
	@RequestMapping(value = "/addCart")
	@ResponseBody
	public Object addCart(@RequestParam(value = "id", defaultValue = "0") Long id,
			@RequestParam(value = "count", defaultValue = "1") Integer count) {
		UmsMember umsMember = UserUtils.getCurrentMember();
		PmsSkuStock pmsSkuStock = pmsSkuStockService.getById(id);
		if (pmsSkuStock != null && umsMember != null && umsMember.getId() != null) {
			OmsCartItem cartItem = new OmsCartItem();
			cartItem.setPrice(pmsSkuStock.getPrice());
			cartItem.setProductId(pmsSkuStock.getProductId());
			cartItem.setProductSkuCode(pmsSkuStock.getSkuCode());
			cartItem.setQuantity(count);
			cartItem.setProductSkuId(id);
			cartItem.setProductAttr(pmsSkuStock.getMeno1());
			cartItem.setProductPic(pmsSkuStock.getPic());
			cartItem.setSp1(pmsSkuStock.getSp1());
			cartItem.setSp2(pmsSkuStock.getSp2());
			cartItem.setSp3(pmsSkuStock.getSp3());
			OmsCartItem omsCartItem = cartItemService.addCart(cartItem);
			return new CommonResult().success(omsCartItem.getId());

		}
		return new CommonResult().failed();
	}

	@ApiOperation("获取某个会员的购物车列表")
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@ResponseBody
	public Object list() {
		UmsMember umsMember = UserUtils.getCurrentMember();
		if (umsMember != null && umsMember.getId() != null) {
			List<OmsCartItem> cartItemList = cartItemService.list(umsMember.getId(), null);
			return new CommonResult().success(cartItemList);
		}
		return new ArrayList<OmsCartItem>();
	}

	@ApiOperation("获取某个会员的购物车列表,包括促销信息")
	@RequestMapping(value = "/list/promotion", method = RequestMethod.GET)
	@ResponseBody
	public Object listPromotion() {
		List<CartPromotionItem> cartPromotionItemList = cartItemService
				.listPromotion(UserUtils.getCurrentMember().getId(), null);
		return new CommonResult().success(cartPromotionItemList);
	}

	@ApiOperation("修改购物车中某个商品的数量")
	@RequestMapping(value = "/update/quantity", method = RequestMethod.GET)
	@ResponseBody
	public Object updateQuantity(@RequestParam Long id, @RequestParam Integer quantity) {
		int count = cartItemService.updateQuantity(id, UserUtils.getCurrentMember().getId(), quantity);
		if (count > 0) {
			return new CommonResult().success(count);
		}
		return new CommonResult().failed();
	}

	@ApiOperation("获取购物车中某个商品的规格,用于重选规格")
	@RequestMapping(value = "/getProduct/{productId}", method = RequestMethod.GET)
	@ResponseBody
	public Object getCartProduct(@PathVariable Long productId) {
		CartProduct cartProduct = cartItemService.getCartProduct(productId);
		return new CommonResult().success(cartProduct);
	}

	@ApiOperation("修改购物车中商品的规格")
	@RequestMapping(value = "/update/attr", method = RequestMethod.POST)
	@ResponseBody
	public Object updateAttr(@RequestBody OmsCartItem cartItem) {
		int count = cartItemService.updateAttr(cartItem);
		if (count > 0) {
			return new CommonResult().success(count);
		}
		return new CommonResult().failed();
	}

	@ApiOperation("删除购物车中的某个商品")
	@RequestMapping(value = "/delete")
	@ResponseBody
	public Object delete(String cart_id_list) {
		if (StringUtils.isEmpty(cart_id_list)) {
			return new CommonResult().failed("参数为空");
		}
		List<Long> resultList = new ArrayList<>(cart_id_list.split(",").length);
		for (String s : cart_id_list.split(",")) {
			resultList.add(Long.valueOf(s));
		}
		int count = cartItemService.delete(UserUtils.getCurrentMember().getId(), resultList);
		if (count > 0) {
			return new CommonResult().success(count);
		}
		return new CommonResult().failed();
	}

	@ApiOperation("清空购物车")
	@RequestMapping(value = "/clear", method = RequestMethod.POST)
	@ResponseBody
	public Object clear() {
		int count = cartItemService.clear(UserUtils.getCurrentMember().getId());
		if (count > 0) {
			return new CommonResult().success(count);
		}
		return new CommonResult().failed();
	}

}
