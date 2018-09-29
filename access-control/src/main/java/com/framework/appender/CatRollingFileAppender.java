package com.framework.appender;

import org.apache.commons.lang3.StringUtils;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Trace;
import com.framwork.cat.ext.vaildator.CatExtValidator;
import com.framwork.utils.ExceptionUtil;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.rolling.RollingFileAppender;

public class CatRollingFileAppender extends RollingFileAppender<Object> {
	/**
	 * 日志类型
	 */
	private static final String TYPE = "Logback";
	/**
	 * 日志报错类型
	 */
	private static final String TYPE_ERROR = "LogErr|";
	/**
	 * length the length to abbreviate {@code str} to. 字段最大长度
	 */
	private static final int MAX_LENTH = 100;
	/**
	 * middle the String to replace the middle characters with, may be null 中间字符串替换
	 */
	private static final String MIDDLE = " ... ";

	protected void append(Object eventObject) {
		super.append(eventObject);
		if (CatExtValidator.getInstance().isDisableLogback())
			return;
		boolean isTraceMode = Cat.getManager().isTraceMode();
		LoggingEvent event = null;
		if (eventObject instanceof LoggingEvent) {
			event = (LoggingEvent) eventObject;
		}
		if (event != null) {
			Level level = event.getLevel();
			if (level.isGreaterOrEqual(Level.ERROR)) {
				logError(event);
			} else if (isTraceMode) {
				logTrace(event);
			}
		}
	}

	private void logError(LoggingEvent event) {
		Throwable t = getThrowable(event);

		String message = event.getMessage();

		if (t != null) {
			if (message != null) {
				Cat.logError(message, t);
			} else {
				Cat.logError(t);
			}
		}
		String messageWithExp = TYPE_ERROR + message + (t != null ? "|" + t.getClass().getName() : "");
		Cat.logEvent(CatExtValidator.SUMMARY_KEY, StringUtils.abbreviateMiddle(messageWithExp, MIDDLE, MAX_LENTH),
				"ERROR", messageWithExp);
	}

	private void logTrace(LoggingEvent event) {
		String name = event.getLevel().toString();
		Object message = event.getMessage();
		String data;

		if (message instanceof Throwable) {
			data = ExceptionUtil.getStackTrace((Throwable) message);
		} else {
			data = event.getMessage();
		}

		Throwable t = getThrowable(event);

		if (t != null) {
			data = data + '\n' + ExceptionUtil.getStackTrace(t);
		}
		Cat.logTrace(TYPE, name, Trace.SUCCESS, data);
	}

	private Throwable getThrowable(LoggingEvent event) {
		Throwable t = null;

		try {
			IThrowableProxy throwableProxy = event.getThrowableProxy();

			if (throwableProxy != null) {
				if (throwableProxy instanceof ThrowableProxy) {
					t = ((ThrowableProxy) throwableProxy).getThrowable();
				} else {
					t = new Throwable(throwableProxy.getMessage());
				}
			}
		} catch (Exception e) {
		}

		return t;
	}
}
