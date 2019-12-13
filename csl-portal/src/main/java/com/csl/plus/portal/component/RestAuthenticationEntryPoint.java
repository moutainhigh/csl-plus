package com.csl.plus.portal.component;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.csl.plus.portal.util.JsonUtil;
import com.csl.plus.utils.CommonResult;

/**
 * 当未登录或者token失效访问接口时，自定义的返回结果 https://github.com/shenzhuan/mallplus on
 * 2018/5/14.
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException, ServletException {
		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/json");
		response.getWriter()
				.println(JsonUtil.objectToJson(new CommonResult().unauthorized(authException.getMessage())));
		response.getWriter().flush();
	}
}
