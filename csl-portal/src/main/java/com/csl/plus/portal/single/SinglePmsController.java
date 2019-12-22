package com.csl.plus.portal.single;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.csl.plus.marking.entity.SmsGroup;
import com.csl.plus.pms.entity.PmsProduct;
import com.csl.plus.pms.entity.PmsProductCategory;
import com.csl.plus.pms.vo.PmsProductAndGroup;
import com.csl.plus.portal.annotation.IgnoreAuth;
import com.csl.plus.portal.annotation.SysLog;
import com.csl.plus.portal.cms.service.ICmsSubjectCategoryService;
import com.csl.plus.portal.cms.service.ICmsSubjectCommentService;
import com.csl.plus.portal.cms.service.ICmsSubjectService;
import com.csl.plus.portal.marking.service.ISmsGroupService;
import com.csl.plus.portal.pms.service.IPmsProductAttributeCategoryService;
import com.csl.plus.portal.pms.service.IPmsProductCategoryService;
import com.csl.plus.portal.pms.service.IPmsProductService;
import com.csl.plus.portal.ums.service.IUmsMemberLevelService;
import com.csl.plus.portal.util.DateUtils;
import com.csl.plus.ums.entity.UmsMember;
import com.csl.plus.ums.entity.UmsMemberLevel;
import com.csl.plus.utils.CommonResult;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @Date: 2019/4/2 15:02
 * @Description:
 */
@RestController
@Api(tags = "CmsController", description = "商品关系管理")
@RequestMapping("/api/single/pms")
public class SinglePmsController extends ApiBaseAction {

	@Resource
	private ISmsGroupService groupService;
	@Resource
	private IUmsMemberLevelService memberLevelService;
	@Resource
	private IPmsProductService pmsProductService;
	@Resource
	private IPmsProductAttributeCategoryService productAttributeCategoryService;
	@Resource
	private IPmsProductCategoryService productCategoryService;

	@Resource
	private ICmsSubjectCategoryService subjectCategoryService;
	@Resource
	private ICmsSubjectService subjectService;
	@Resource
	private ICmsSubjectCommentService commentService;

	@SysLog(MODULE = "pms", REMARK = "查询商品详情信息")
	@IgnoreAuth
	@GetMapping(value = "/goods/detail")
	@ApiOperation(value = "查询商品详情信息")
	public Object queryProductDetail(@RequestParam(value = "id", required = false, defaultValue = "0") Long id) {
		PmsProductAndGroup productResult = pmsProductService.getProductAndGroup(id);
		Map<String, Object> map = new HashMap<>();
		SmsGroup queryGoods = new SmsGroup();
		queryGoods.setGoodsId(id);
		SmsGroup group = groupService.getOne(new QueryWrapper<>(queryGoods));
		if (group != null) {
			Date endTime = DateUtils.convertStringToDate(DateUtils.addHours(group.getEndTime(), group.getHours()),
					"yyyy-MM-dd HH:mm:ss");
			Long nowT = System.currentTimeMillis();
			if (group != null && nowT > group.getStartTime().getTime() && nowT < endTime.getTime()) {
				map.put("group", group);
				map.put("isGroup", 1);
			} else {
				map.put("isGroup", 2);
			}
		}

		map.put("goods", productResult);
		return new CommonResult().success(map);
	}

	@SysLog(MODULE = "pms", REMARK = "查询商品列表")
	@IgnoreAuth
	@ApiOperation(value = "查询商品列表")
	@GetMapping(value = "/goods/list")
	public Object goodsList(PmsProduct product,
			@RequestParam(value = "pageSize", required = false, defaultValue = "5") Integer pageSize,
			@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum) {
		product.setPublishStatus(1);
		product.setVerifyStatus(1);
		return new CommonResult()
				.success(pmsProductService.page(new Page<PmsProduct>(pageNum, pageSize), new QueryWrapper<>(product)));
	}

	@SysLog(MODULE = "pms", REMARK = "查询商品分类列表")
	@IgnoreAuth
	@ApiOperation(value = "查询商品分类列表")
	@GetMapping(value = "/productCategory/list")
	public Object productCategoryList(PmsProductCategory productCategory,
			@RequestParam(value = "pageSize", required = false, defaultValue = "5") Integer pageSize,
			@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum) {
		return new CommonResult().success(productCategoryService.page(new Page<PmsProductCategory>(pageNum, pageSize),
				new QueryWrapper<>(productCategory)));
	}

	@ApiOperation("创建商品")
	@SysLog(MODULE = "pms", REMARK = "创建商品")
	@PostMapping(value = "/createGoods")
	public Object createGoods(PmsProduct productParam) {
		CommonResult commonResult;
		UmsMember member = this.getCurrentMember();
		if (member.getMemberLevelId() > 0) {
			UmsMemberLevel memberLevel = memberLevelService.getById(member.getMemberLevelId());
			PmsProduct newSubject = new PmsProduct();
			newSubject.setSupplyId(member.getId());
			newSubject.setPublishStatus(1);
			newSubject.setVerifyStatus(1);
			List<PmsProduct> subjects = pmsProductService.list(new QueryWrapper<>(newSubject));
			if (subjects != null && subjects.size() > memberLevel.getGoodscount()) {
				commonResult = new CommonResult().failed("你今天已经有发" + memberLevel.getGoodscount() + "个商品");
				return commonResult;
			}
		}
		productParam.setSupplyId(member.getId());
		productParam.setCreateTime(new Date());
		boolean count = pmsProductService.save(productParam);
		if (count) {
			return new CommonResult().success(count);
		} else {
			return new CommonResult().failed();
		}
	}

}
