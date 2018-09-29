package com.framework.global;

import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import com.framwork.utils.matcher.WebPathMatcher;

/**
 * 
 * @title 全局配置
 * @author joe
 * @date 2018年9月12日下午2:07:48
 */
public class GlobalWebContext {
	protected static final ConcurrentHashMap<String, Object> appConfig = new ConcurrentHashMap<String, Object>();
	protected static String contextPath = "DEFAULT";
	protected static ServletContext servletContext; // servlet上下文

	public static final String APP_NAME = "app.name";
	public static final String APP_ENV = "app.env";
	public static final String APP_PROD_BLOCK_URL = "app.prod.block.url";
	public static final String APP_VALIDATORS = "app.validators";
	public static final String APP_VALIDATION_PARAMS    = "app.validation.params.";
	public static final String SYSTEM_SYS = "sys."; // 系统环境变量前缀
	public static final String LOG_PATH = SYSTEM_SYS + "log.path";
	public static final String LOG_FULL_PATH = SYSTEM_SYS + "log.fullpath"; // 拼装了 logPath和app,cell,node
	public static final String LOG_ROOT = SYSTEM_SYS + "log.root.level";
	public static final String LOG_SPRING = SYSTEM_SYS + "log.spring.level";
	public static final String LOG_LOWEST = SYSTEM_SYS + "log.lowest.level";

	public static final String HTTP_PARAM_ACCESS_TOKEN = "access_token";

	public static final String HTTP_ATTR_PREFIX = "com.haimi.";
	public static final String HTTP_ATTR_TICKET_CRACK = "mmTicketCrack";

	public static final String HTTP_HEAD_TICKET = "mmTicket";
	public static final String HTTP_HEAD_CHANNEL = "mmChannel";
	public static final String HTTP_HEAD_REQUEST_ID = "mmRid";
	public static final String HTTP_HEAD_TIMESTAMP = "mmTs";
	public static final String HTTP_HEAD_VERSION = "mmVer";
	public static final String HTTP_CLIENT_ID = "mmClientId";

	// 渠道号channel - pc, wechat, iphone, andriod
	protected static final ThreadLocal<String> TL_CHANNEL = new ThreadLocal<String>();

	// 请求接口的版本号
	protected static final ThreadLocal<String> TL_VERSION = new ThreadLocal<String>();

	// 客户端Id
	protected static final ThreadLocal<String> TL_CLIENT_ID = new ThreadLocal<String>();

	// 访问令牌
	protected static final ThreadLocal<String> TL_ACCESS_TOKEN = new ThreadLocal<String>();

	// 访问票据
	protected static final ThreadLocal<String> TL_TICKET = new ThreadLocal<String>();
	protected static final ThreadLocal<HttpServletRequest> TL_REQUEST = new ThreadLocal<HttpServletRequest>();
	// 请求时间戳
	protected static final ThreadLocal<String> TL_REQUEST_TS = new ThreadLocal<String>();
	// 请求ID(客户端传过来的UUID, 必须保证全局唯一)
	protected static final ThreadLocal<String> TL_REQUEST_ID = new ThreadLocal<String>() {
		@Override
		public String initialValue() {
			return UUID.randomUUID().toString().replaceAll("-", "");
		}
	};

	public static HttpServletRequest getRequest() {
		return (HttpServletRequest) TL_REQUEST.get();
	}

	public static void setRequest(HttpServletRequest request) {
		TL_REQUEST.set(request);
	}

	/**
	 * 将http请求的监控返回结果设置为失败
	 * 
	 * @param failMessage
	 *            不能过长，尽量保证在20个字符以内
	 */
	public static void setHttpMonitorFail(String failMessage) {
		getRequest().setAttribute("cat-state", failMessage);
	}

	public static String getAccessToken() {
		return TL_ACCESS_TOKEN.get();
	}

	public static void setAccessToken(String access_token) {
		TL_ACCESS_TOKEN.set(access_token);
	}

	public static String getTicket() {
		return TL_TICKET.get();
	}

	public static void setTicket(String ticket) {
		TL_TICKET.set(ticket);
	}

	public static String getRequestId() {
		return TL_REQUEST_ID.get();
	}

	public static void setRequestId(String requestId) {
		TL_REQUEST_ID.set(requestId);
	}

	public static String getRequestTs() {
		return TL_REQUEST_TS.get();
	}

	public static void setRequestTs(String requestTs) {
		TL_REQUEST_TS.set(requestTs);
	}

	public static String getChannel() {
		return TL_CHANNEL.get();
	}

	public static void setChannel(String channel) {
		TL_CHANNEL.set(channel);
	}

	public static String getVersion() {
		return TL_VERSION.get();
	}

	public static void setVersion(String version) {
		TL_VERSION.set(version);
	}

	public static String getClientId() {
		return TL_CLIENT_ID.get();
	}

	public static void setClientId(String clientId) {
		TL_CLIENT_ID.set(clientId);
	}

	public static ServletContext getServletContext() {
		return servletContext;
	}

	public static void setServletContext(ServletContext servletContext) {
		GlobalWebContext.servletContext = servletContext;
	}

	public static String getContextPath() {
		return contextPath;
	}

	public static void setContextPath(String contextPath) {
		GlobalWebContext.contextPath = contextPath;
	}

	/**
	 * 判断是否是生产环境
	 * 
	 * @title
	 * @date 2018年9月12日下午2:09:02
	 * @return
	 */
	public static boolean isProEnv() {
		String appEnv = getEnv();
		if (appEnv == null || StringUtils.containsIgnoreCase(appEnv, "PRO")) {
			return true;
		}
		return false;
	}

	public static String getEnv() {
		return (String) appConfig.get(APP_ENV);
	}

	public static ConcurrentHashMap<String, Object> getAppconfig() {
		return appConfig;
	}

	@SuppressWarnings("unchecked")
	public static <T> T getAppCfg(String key) {
		return (T) appConfig.get(key);
	}

	public static void putAppCfg(String key, Object value) {
		appConfig.put(key, value);
	}

	public static void putAppCfg(Properties properties) {
		if (properties == null || properties.size() == 0) {
			return;
		}

		for (@SuppressWarnings("rawtypes")
		Map.Entry entry : properties.entrySet()) {
			appConfig.put((String) entry.getKey(), entry.getValue());
		}
	}

	/**
	 * 将生产环境不能暴露的路径接口匹配类型，解析为Set集合。
	 * 
	 * @return
	 */
	public static Set<String> getProductBlockUrlPatterns() {
		String blockUrl = (String) appConfig.get(APP_PROD_BLOCK_URL);
		return WebPathMatcher.parsePaths(blockUrl);
	}

	/**
	 * 获取所有的validators，用于Filter校验
	 * 
	 * @return
	 */
	public static Set<String> getValidators() {
		String validators = (String) appConfig.get(APP_VALIDATORS);
		return WebPathMatcher.parsePaths(validators);
	}

	/**
	 * 清除ThreadLocal引用(建议在每个请求线程的最后清理)
	 */
	public static void clearThreadLocals() {
		TL_REQUEST.remove();
		TL_REQUEST_ID.remove();
		TL_REQUEST_TS.remove();
		TL_CHANNEL.remove();
		TL_ACCESS_TOKEN.remove();
		TL_TICKET.remove();
		TL_VERSION.remove();
		TL_CLIENT_ID.remove();
	}
}
