package com.csl.plus.portal.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.csl.plus.marking.entity.UserFormId;
import com.csl.plus.portal.annotation.IgnoreAuth;
import com.csl.plus.portal.single.ApiBaseAction;
import com.csl.plus.portal.ums.service.IUmsMemberService;
import com.csl.plus.ums.entity.UmsMember;
import com.csl.plus.utils.CommonResult;
import com.csl.plus.utils.ValidatorUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 会员登录注册管理Controller
 */
@Controller
@Api(tags = "UmsMemberController", description = "会员管理系统")
@RequestMapping("/api/member")
public class UmsMemberController extends ApiBaseAction {
	@Autowired
	private IUmsMemberService memberService;
	@Value("${jwt.tokenHeader}")
	private String tokenHeader;
	@Value("${jwt.tokenHead}")
	private String tokenHead;

	@IgnoreAuth
	@ApiOperation(value = "登录以后返回token")
	@GetMapping(value = "/login")
	@ResponseBody
	public Object login(UmsMember umsMember) {
		if (umsMember == null) {
			return new CommonResult().validateFailed("用户名或密码错误");
		}
		try {
			Map<String, Object> token = memberService.login(umsMember.getUsername(), umsMember.getPassword());
			if (token.get("token") == null) {
				return new CommonResult().validateFailed("用户名或密码错误");
			}
			return new CommonResult().success(token);
		} catch (AuthenticationException e) {
			return new CommonResult().validateFailed("用户名或密码错误");
		}

	}

	@IgnoreAuth
	@ApiOperation("注册")
	@PostMapping(value = "/reg")
	@ResponseBody
	public Object register(@RequestBody UmsMember umsMember) {
		if (umsMember == null) {
			return new CommonResult().validateFailed("用户名或密码错误");
		}
		return memberService.register(umsMember);
	}

	@IgnoreAuth
	@ApiOperation("获取验证码")
	@RequestMapping(value = "/getAuthCode", method = RequestMethod.GET)
	@ResponseBody
	public Object getAuthCode(@RequestParam String telephone) {
		return memberService.generateAuthCode(telephone);
	}

	@ApiOperation("修改密码")
	@RequestMapping(value = "/updatePassword", method = RequestMethod.POST)
	@ResponseBody
	public Object updatePassword(@RequestParam String telephone, @RequestParam String password,
			@RequestParam String authCode) {
		return memberService.updatePassword(telephone, password, authCode);
	}

	@IgnoreAuth
	@GetMapping("/user")
	@ResponseBody
	public Object user() {
		UmsMember umsMember = memberService.getCurrentMember();
		if (umsMember != null && umsMember.getId() != null) {
			return new CommonResult().success(umsMember);
		}
		return new CommonResult().failed();
	}

	@ApiOperation(value = "刷新token")
	@RequestMapping(value = "/token/refresh", method = RequestMethod.GET)
	@ResponseBody
	public Object refreshToken(HttpServletRequest request) {
		String token = request.getHeader(tokenHeader);
		String refreshToken = memberService.refreshToken(token);
		if (refreshToken == null) {
			return new CommonResult().failed();
		}
		Map<String, String> tokenMap = new HashMap<>();
		tokenMap.put("token", token);
		tokenMap.put("tokenHead", tokenHead);
		return new CommonResult().success(tokenMap);
	}

	@ApiOperation(value = "登出功能")
	@RequestMapping(value = "/logout", method = RequestMethod.POST)
	@ResponseBody
	public Object logout() {
		return new CommonResult().success(null);
	}

	/**
	 * 提交小程序推送formid
	 * 
	 * @param request
	 * @param response
	 * @param formId   小程序推送formId
	 * @param source   @see
	 *                 com.fittime.health.market.model.PushUserFormIdRecord.source
	 * @return
	 */
	@RequestMapping(value = "submitFormId")
	@ApiOperation(value = "提交小程序推送formid")
	@ResponseBody
	public Object submitFormId(HttpServletRequest request, HttpServletResponse response, String formId,
			Integer source) {

		UserFormId entity = new UserFormId();

		if (ValidatorUtils.empty(formId)) {
			return new CommonResult().validateFailed("前置参数错误，formId不能为空");
		}

		if (ValidatorUtils.empty(source)) {
			return new CommonResult().validateFailed("前置参数错误，source不能为空");
		}

		// 校验formId是否已经存在
		/*
		 * if(memberService.exists(formId)) { return new
		 * CommonResult().validateFailed("前置参数错误，formId已经存在 formId：" + formId); }
		 * 
		 * entity.setUserId(this.getCurrentMember().getId()); entity.setFormId(formId);
		 * entity.setSource(source); entity.setStatus(1);
		 * 
		 * memberService.insert(entity);
		 */

		return new CommonResult().success("添加成功");
	}

	@IgnoreAuth
	@GetMapping("/list")
	@ResponseBody
	public Object list() {
		UmsMember umsMember = memberService.getById(1);
		if (umsMember != null && umsMember.getId() != null) {
			return new CommonResult().success(umsMember);
		}
		return new CommonResult().failed();
	}
}
