package com.framework.listener;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.framework.appender.ConsoleAppenderExt;
import com.framework.utils.ServletContextUtils;
import com.framwork.utils.ExceptionUtil;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import ch.qos.logback.ext.spring.web.WebLogbackConfigurer;

/**
 * 日志监听器 1. 设置系统环境变量上下文 2. 载入日志配置文件
 * 
 * @title
 * @author joe
 * @date 2018年9月12日下午2:40:44
 */
public class AppLogConfigListener implements ServletContextListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(AppLogConfigListener.class);

	/**
	 * 重新加载Logback.xml
	 *
	 * 说明： 在某些极端情况下比如使用了druid的数据源且jndi的配置方式，tomcat启动时会优先解析数据源，并同时加载logback.xml
	 * 而LoggerContext在web启动仅加载一次，所以若配置了一些参数变量，必须reset一下。 *
	 *
	 *
	 * @param event
	 */
	public void contextInitialized(ServletContextEvent event) {
		event.getServletContext().log("-=-=-= Init AppLogConfigListener Begin =-=-=-");
		ServletContext sc = event.getServletContext();

		// 对ServletContext进行解析
		ServletContextUtils.parseServletContext(sc);

		// 根据设置的系统变量，重置logback的等级
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

		// 进入Web之前已加载了Logger，需要重置!
		if (loggerContext != null && loggerContext.getLoggerList().size() > 0) {
			// 必须清空一下，否则之前加载的logger堆栈信息还保留着StatusPrinter.print会打印出之前的状态
			loggerContext.getStatusManager().clear();
			loggerContext.reset();
			ContextInitializer ci = new ContextInitializer(loggerContext);
			try {
				ci.autoConfig();
			} catch (JoranException e) {
				sc.log("-=-=-= Reset Logback status Failed =-=-=- \n" + ExceptionUtil.getStackTrace(e));
			}
		}

		WebLogbackConfigurer.initLogging(sc);
		// 载入环境变量之后重新初始化 Appender
		ConsoleAppenderExt.init();
		StatusPrinter.print(loggerContext);
		LOGGER.info("\n================= Init AppLogConfigListener End ================");
	}

	public void contextDestroyed(ServletContextEvent event) {
		WebLogbackConfigurer.shutdownLogging(event.getServletContext());
	}

}
