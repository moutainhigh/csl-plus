package com.csl.plus.portal.component;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.csl.plus.portal.util.JsonUtil;
import com.csl.plus.utils.CommonResult;

/**
 * 当访问接口没有权限时，自定义的返回结果
 */
@Component
public class RestfulAccessDeniedHandler implements AccessDeniedHandler {
	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e)
			throws IOException, ServletException {
		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/json");
		response.getWriter().println(JsonUtil.objectToJson(new CommonResult().forbidden(e.getMessage())));
		response.getWriter().flush();
	}
}
