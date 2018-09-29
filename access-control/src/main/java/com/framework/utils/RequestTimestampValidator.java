package com.framework.utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import com.framework.global.GlobalWebContext;

/**
 * 对HTTP请求头域中的timestamp进行校验
 * 
 * @title
 * @author joe
 * @date 2018年9月29日下午1:53:36
 */
public class RequestTimestampValidator implements IAppServletValidator {
	private static final Logger LOGGER = LoggerFactory.getLogger(RequestTimestampValidator.class);

	private static final long MAX_REQUEST_DURATION = 15;

	public boolean validate(HttpServletRequest request, HttpServletResponse response) throws Exception {
		/*
		 * 对请求的UTC时间戳（单位毫秒的Long类型）进行判定，超过15分钟则视为请求超时。默认该校验是开启的！ 两种方式可以关闭该校验： a.
		 * app.properties中 app.validations=xx,xx 将RequestTimestampValidator去掉 b. http
		 * POST/PUT请求:
		 * http://x.memedai.cn/App/sys/manage?q=appCfg&k=app.validation.toggle.request.
		 * ts&v=0 注: http DELETE请求可删除key:
		 * http://x.memedai.cn/App/sys/manage?q=appCfg&k=app.validation.toggle.request.
		 * ts http GET请求可查询key:
		 * http://x.memedai.cn/App/sys/manage?q=appCfg&k=app.validation.toggle.request.
		 * ts
		 */
		String mRequestTs = request.getHeader(GlobalWebContext.HTTP_HEAD_TIMESTAMP); // 单位毫秒

		boolean isValidRequestTs = (mRequestTs != null && mRequestTs.trim().length() > 0);

		try {
			if (isValidRequestTs) {
				long lRequestTs = Long.parseLong(mRequestTs);
				long currentRts = System.currentTimeMillis();

				long duration = Math.abs(currentRts - lRequestTs);

				// "app.validation.params.RequestTimestampValidator" 参数设置
				String maxDurationStr = GlobalWebContext.getAppCfg(
						GlobalWebContext.APP_VALIDATION_PARAMS + RequestTimestampValidator.class.getSimpleName());

				long maxDuration = MAX_REQUEST_DURATION; // 默认15分钟
				if (maxDurationStr != null && maxDurationStr.length() > 0) {
					maxDuration = Long.parseLong(maxDurationStr);
				}

				if (duration > maxDuration * 60 * 1000) {
					isValidRequestTs = false;
				}
			}
		} catch (Exception e) {
			isValidRequestTs = false;
		}

		if (!isValidRequestTs) {
			LOGGER.info("====This request is timeout====: " + mRequestTs);
			response.setStatus(HttpStatus.REQUEST_TIMEOUT.value());
			response.getWriter().write("request timeout");
			return false;
		}

		return isValidRequestTs;
	}
}
