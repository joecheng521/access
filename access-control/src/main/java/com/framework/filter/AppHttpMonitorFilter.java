package com.framework.filter;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import com.dianping.cat.servlet.CatFilter;
import com.framework.utils.ServletContextUtils;
import com.framwork.cat.ext.vaildator.CatExtValidator;
import com.framwork.utils.matcher.WebPathMatcher;

public class AppHttpMonitorFilter extends CatFilter {
	public static final String PARAM_NAME_EXCLUSIONS = "exclusions";
	public static final String DEFAULT_STATIC_RESOURCES = "default-static";

	private String contextPath;
	private Set<String> excludesPattern = new HashSet<String>();

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		this.contextPath = filterConfig.getServletContext().getContextPath();
		if (StringUtils.isBlank(contextPath)) {
			this.contextPath = "/";
		}

		// 不需要拦截的路径
		String exclusions = filterConfig.getInitParameter(PARAM_NAME_EXCLUSIONS);
		if (StringUtils.isNotBlank(exclusions)) {
			excludesPattern = WebPathMatcher.parsePaths(exclusions);
		}

		super.init(filterConfig);
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		// 禁用http监控
		if (CatExtValidator.getInstance().isDisableHttp()) {
			chain.doFilter(request, response);
			return;
		}

		// 2. 无需过滤的请求直接跳过 + 应用根目录欢迎页不过滤
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		String requestURI = httpRequest.getRequestURI();
		if (requestURI.endsWith(this.contextPath) // 根路径不拦截
				|| requestURI.endsWith(this.contextPath + "/") // 根路径不拦截
				|| (excludesPattern.contains(DEFAULT_STATIC_RESOURCES) // 设置默认的静态资源不拦截
						&& ServletContextUtils.isStaticResource(requestURI))
				|| WebPathMatcher.matchAny(excludesPattern, requestURI)) {
			chain.doFilter(httpRequest, response);
			return;
		}

		super.doFilter(request, response, chain);
	}
}
