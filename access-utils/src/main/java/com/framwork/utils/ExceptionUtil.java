package com.framwork.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

public abstract class ExceptionUtil {
	/**
	 * 取得异常的stacktrace字符串。
	 * 
	 * @param throwable
	 *            受检异常
	 * @return stacktrace字符串
	 */
	public static String getStackTrace(Throwable throwable) {
		return getStackTrace(throwable, -1);
	}

	/**
	 * 取得异常的stacktrace字符串。
	 *
	 * @param throwable
	 *            受检异常
	 * @param length
	 *            限定输出异常字符串的长度
	 * @return stacktrace字符串
	 */
	public static String getStackTrace(Throwable throwable, int length) {
		StringWriter buffer = new StringWriter();
		PrintWriter out = new PrintWriter(buffer);

		throwable.printStackTrace(out);
		out.flush();

		String traceStr = buffer.toString();
		if (length <= 0) {
			return traceStr;
		} else {
			return traceStr.substring(0, length);
		}
	}

}
