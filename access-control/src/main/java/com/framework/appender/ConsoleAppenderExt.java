package com.framework.appender;

import com.framework.global.GlobalWebContext;

import ch.qos.logback.core.ConsoleAppender;

/**
 * org.apache.log4j.ConsoleAppender（控制台）
 * @title 
 * @author joe
 * @date 2018年9月28日下午3:14:18
 */
@SuppressWarnings("rawtypes")
public class ConsoleAppenderExt extends ConsoleAppender {

	private static Boolean enableConsole = null;

	public static void init() {
		enableConsole = null;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void append(Object eventObject) {
		if (enableConsole == null) {
			if (GlobalWebContext.isProEnv() || "SIT".equalsIgnoreCase(GlobalWebContext.getEnv())
					|| "UAT".equalsIgnoreCase(GlobalWebContext.getEnv())) {
				enableConsole = false;
			} else {
				enableConsole = true;
			}
		}

		if (enableConsole == true) {
			super.append(eventObject);
		}
	}
}
