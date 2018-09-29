package com.framework.converter;

import java.util.concurrent.atomic.AtomicInteger;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
/**
 * 打印线程
 * @title 
 * @author joe
 * @date 2018年9月12日下午3:37:11
 */
public class ThreadIdConverter extends ClassicConverter {
	private static AtomicInteger nextId = new AtomicInteger(0);

	@Override
	public String convert(ILoggingEvent arg0) {
		return threadId.get();
	}

	private static final ThreadLocal<String> threadId = new ThreadLocal<String>() {
		@Override
		protected String initialValue() {
			nextId.incrementAndGet();
			try {
				return "Id(" + String.format("%02d", nextId.get()) + ")-" + Thread.currentThread().getName();
			} catch (Exception e) {
				return "Id(ERROR):" + Thread.currentThread().getName();
			}
		}
	};
}
