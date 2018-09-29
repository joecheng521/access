package com.framework.appender;

import com.framework.global.GlobalWebContext;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.RollingFileAppender;

/**
 * org.apache.log4j.RollingFileAppender（文件大小到达指定尺寸的时候产生一个新的文件
 * @title 
 * @author joe
 * @date 2018年9月28日下午3:25:10
 */
@SuppressWarnings("rawtypes")
public class RollingFileAppenderExt extends RollingFileAppender {

	private static Boolean enableAppender = true;

	public static void init() {
		enableAppender = null;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void append(Object eventObject) {
		if (enableAppender == null) {
			if (GlobalWebContext.isProEnv()) {
				if (eventObject instanceof ILoggingEvent) {
					ILoggingEvent event = (ILoggingEvent) eventObject;
					if (event.getLoggerName().equalsIgnoreCase("com.baidu.disconf.client.DisconfMgr")
							&& (event.getMessage().contains("Conf File Map:")
									|| event.getMessage().contains("Conf Item Map:"))) {
						enableAppender = false;
					}
				}
			}
		}
		if (enableAppender == true) {
			super.append(eventObject);
		}

	}
}
