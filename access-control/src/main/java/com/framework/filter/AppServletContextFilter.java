package com.framework.filter;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import com.framework.global.GlobalWebContext;
import com.framework.utils.ServletContextUtils;
import com.framework.utils.ValidatorUtil;
import com.framwork.utils.matcher.WebPathMatcher;
import com.framwork.work.BodyReaderHttpServletRequestWrapper;
import com.framwork.work.HttpContentTypeEnum;

/**
 * 记录请求/应答对象到ThreadLocal上下文中
 * @title 
 * @author joe
 * @date 2018年9月29日上午9:33:55
 */
public class AppServletContextFilter implements Filter {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppServletContextFilter.class);

    protected String contextPath;

    public static final String PARAM_NAME_INCLUSIONS = "inclusions";
    public static final String PARAM_NAME_EXCLUSIONS = "exclusions";
    public static final String DEFAULT_STATIC_RESOURCES = "default-static";

    private Set<String> includesPattern = new HashSet<String>();    //优先级1，优先于exclusions
    private Set<String> excludesPattern = new HashSet<String>();    //优先级2

	public void init(FilterConfig filterConfig) throws ServletException {
		LOGGER.info("\n================= Init AppServletContextFilter Begin ================");
        this.contextPath = filterConfig.getServletContext().getContextPath();
        if (StringUtils.isBlank(contextPath)) {
            this.contextPath = "/";
        }
		GlobalWebContext.setContextPath(this.contextPath);

        //需要拦截的路径
        String inclusions = filterConfig.getInitParameter(PARAM_NAME_INCLUSIONS);
        if (StringUtils.isNotBlank(inclusions)) {
            includesPattern = WebPathMatcher.parsePaths(inclusions);
        }

        //不需要拦截的路径
        String exclusions = filterConfig.getInitParameter(PARAM_NAME_EXCLUSIONS);
        if (StringUtils.isNotBlank(exclusions)) {
            excludesPattern = WebPathMatcher.parsePaths(exclusions);
        }
        LOGGER.info("\n================= Init AppServletContextFilter End ================");
	}

	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException,
			ServletException {
		LOGGER.debug("\n================= execute AppServletContextFilter ================");
		
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        String contentType = httpRequest.getContentType();

        if (contentType != null &&
                contentType.contains(HttpContentTypeEnum.APP_X_WWW_FORM_URLENCODE.getContentType())) {
            httpRequest.getParameterMap();
        }


        httpRequest = new BodyReaderHttpServletRequestWrapper(httpRequest);
        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;

        try {
            //0. 设置请求上下文
            GlobalWebContext.setRequest(httpRequest);

            String requestURI = httpRequest.getRequestURI();

            //1. 获取头部和token信息，并塞入到本地线程变量中
            //access_token 不在头域中
            String accessToken  = httpRequest.getParameter(GlobalWebContext.HTTP_PARAM_ACCESS_TOKEN);

            String ticket      = httpRequest.getHeader(GlobalWebContext.HTTP_HEAD_TICKET);
            String channel     = httpRequest.getHeader(GlobalWebContext.HTTP_HEAD_CHANNEL);
            String requestId   = httpRequest.getHeader(GlobalWebContext.HTTP_HEAD_REQUEST_ID);
            String requestTs   = httpRequest.getHeader(GlobalWebContext.HTTP_HEAD_TIMESTAMP); //单位毫秒
            String version     = httpRequest.getHeader(GlobalWebContext.HTTP_HEAD_VERSION);
            String clientId    = httpRequest.getHeader(GlobalWebContext.HTTP_CLIENT_ID);

            // 设置requestId上下文，如果header中没有设置requestId，生成默认32位(UUID)
            if (StringUtils.isNotBlank(requestId)) {
                GlobalWebContext.setRequestId(requestId);
            }

            GlobalWebContext.setAccessToken (accessToken);

            GlobalWebContext.setTicket      (ticket    );
            GlobalWebContext.setChannel     (channel   );
            GlobalWebContext.setRequestTs   (requestTs );
            GlobalWebContext.setVersion     (version   );
            GlobalWebContext.setClientId    (clientId  );

            //2. 无需过滤的请求直接跳过 + 应用根目录欢迎页不过滤
            if (!WebPathMatcher.matchAny(includesPattern, requestURI)       //includePattern优先级最高
                    && (requestURI.endsWith(this.contextPath)                   //根路径不拦截
                        || requestURI.endsWith(this.contextPath + "/")          //根路径不拦截
                        || (excludesPattern.contains(DEFAULT_STATIC_RESOURCES)  //设置默认的静态资源不拦截
                            && ServletContextUtils.isStaticResource(requestURI))
                        || WebPathMatcher.matchAny(excludesPattern, requestURI))) {

                chain.doFilter(httpRequest, httpResponse);
                return;
            }

            //3. 用于测试的特殊URL请包含/test或者/mock在路径中，这种路径只有测试环境才能使用, 确保生产环境不暴露
            if (WebPathMatcher.matchAny(GlobalWebContext.getProductBlockUrlPatterns(), requestURI)) {
                if (GlobalWebContext.isProEnv()) {
                    httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
                    return;
                }
            }

            //4. 获取验证器Loop验证
            Set<String> validators = GlobalWebContext.getValidators();
            for (String validatorName : validators) {
                try {
                    boolean isValid = ValidatorUtil.doValidation(validatorName, httpRequest, httpResponse);
                    if (!isValid) return;
                } catch (Exception e) {
                    httpResponse.setStatus(HttpStatus.FORBIDDEN.value());
                    return;
                }
            }

            chain.doFilter(httpRequest, httpResponse);
        } finally {
            //最后清理所有的线程副本
            GlobalWebContext.clearThreadLocals();
        }
    }

	public void destroy() {
	}



}
