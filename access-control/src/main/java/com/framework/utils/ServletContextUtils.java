package com.framework.utils;

import ch.qos.logback.classic.Level;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import com.framework.global.GlobalWebContext;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import java.util.Properties;

public class ServletContextUtils {
	public static final String INIT_PARAM_CONTEXT_PATH = "contextPath";
	public static final String INIT_PARAM_APP_CONFIG = "appConfig";

	public static final String APP_DEFAULT = "/app.properties";

	private static final String LINUX_LOG_PATH = "/usr/local/src/logs"; // 默认Linux日志地址
	private static final String WINDOWS_LOG_PATH = "C:\\logs"; // 默认Windows日志地址

	private static final String CELL_NAME = "cell"; // 默认Linux日志地址
	private static final String NODE_NAME = "node"; // 默认Windows日志地址

	private static final String LOG_ROOT_LEVEL_PRO = "INFO";
	private static final String LOG_ROOT_LEVEL_TEST = "INFO";

	private static final String LOG_SPRING_LEVEL_PRO = "INFO";
	private static final String LOG_SPRING_LEVEL_TEST = "INFO";
	
	/**
	 * 将servletContext进行解析处理，塞入 GlobalContext
	 * @title
	 * @date 2018年9月12日下午2:35:56
	 * @param sc
	 */
	public static void parseServletContext(ServletContext sc) {
		ServletContext scExists = GlobalWebContext.getServletContext();
		// 已载入过则不再加载
		if (scExists != null)
			return;
		GlobalWebContext.setServletContext(sc);
		// 设置应用名称(相对路径)
		String contextPath = sc.getInitParameter(INIT_PARAM_CONTEXT_PATH);
		GlobalWebContext.setContextPath(contextPath);
		sc.log("-=-=-= Load ServletContext =-=-=- contextPath=" + contextPath);
		// Aplication级别的配置属性，启动时载入
		loadProperties(sc);
		setSystemProperty(sc);
	}
	
	/***
	 * 加载属性文件
	 * @title
	 * @date 2018年9月12日下午2:35:23
	 * @param sc
	 */
	private static void loadProperties(ServletContext sc) {
		String appConfigFile = sc.getInitParameter(INIT_PARAM_APP_CONFIG);

		if (StringUtils.isBlank(appConfigFile)) {
			appConfigFile = APP_DEFAULT;
		}
		try {
			java.io.InputStream is = ServletContextUtils.class.getResourceAsStream(appConfigFile);
			if (is != null) {
				Properties app = new Properties();
				app.load(is);
				GlobalWebContext.putAppCfg(app);
				sc.log("-=-=-= Load app.properties Succ =-=-=- file=" + appConfigFile);
			}
		} catch (Exception e) {
			sc.log("-=-=-= Load app.properties Fail =-=-=- file=" + appConfigFile);
		}
	}

	/**
	 * 添加系统变量
	 * @title
	 * @date 2018年9月12日下午2:28:54
	 * @param sc
	 */
	private static void setSystemProperty(ServletContext sc) {
		boolean isProdEnv = GlobalWebContext.isProEnv();
		boolean isWindows = SystemUtils.IS_OS_WINDOWS;
		String currentEnv = getSysValue(GlobalWebContext.APP_ENV);
		if (StringUtils.isBlank(currentEnv)) {
			currentEnv = "UNKNOWN";
		}
		String appName = getAppName();
		System.setProperty(GlobalWebContext.SYSTEM_SYS + GlobalWebContext.APP_NAME, appName);
		System.setProperty(GlobalWebContext.SYSTEM_SYS + GlobalWebContext.APP_ENV, currentEnv);
		// 设置log路径，区分windows或者linux
		String logPath = getSysValue(GlobalWebContext.LOG_PATH);
		if (StringUtils.isBlank(logPath)) {
			if (isWindows) {
				logPath = WINDOWS_LOG_PATH;
			} else {
				logPath = LINUX_LOG_PATH;
			}
		}
		System.setProperty(GlobalWebContext.LOG_PATH, logPath);

		// 设置 Root level
		String logRoot = getSysValue(GlobalWebContext.LOG_ROOT);
		if (StringUtils.isBlank(logRoot)) {
			// 非生产环境使用TRACE查错用
			logRoot = isProdEnv ? LOG_ROOT_LEVEL_PRO : LOG_ROOT_LEVEL_TEST;
		}
		System.setProperty(GlobalWebContext.LOG_ROOT, logRoot);


		// 设置 Spring level
		String logSpring = getSysValue(GlobalWebContext.LOG_SPRING);
		if (StringUtils.isBlank(logSpring)) {
			logSpring = isProdEnv ? LOG_SPRING_LEVEL_PRO : LOG_SPRING_LEVEL_TEST;
		}
		System.setProperty(GlobalWebContext.LOG_SPRING, logSpring);

		// log.file.level取三者中最小的一个
		Level logRootLevel = Level.toLevel(logRoot);
		Level logSpringLevel = Level.toLevel(logSpring);

		Level logLowestLevel = Level.INFO;
		logLowestLevel = logRootLevel.isGreaterOrEqual(logLowestLevel) ? logLowestLevel : logRootLevel;
		logLowestLevel = logSpringLevel.isGreaterOrEqual(logLowestLevel) ? logLowestLevel : logSpringLevel;
		System.setProperty(GlobalWebContext.LOG_LOWEST, logLowestLevel.toString());

		// log完整路径，请与logback.xml中的local.log.fullpath保持一致
		String logFullPathStr = getSysValue(GlobalWebContext.LOG_FULL_PATH);
		if (StringUtils.isBlank(logFullPathStr)) {
			StringBuilder logFullPath = new StringBuilder(logPath);

			logFullPath.append(SystemUtils.FILE_SEPARATOR).append(appName);

			String env_cell_name = getSysValue("env.cell.name");
			if (StringUtils.isBlank(env_cell_name)) {
				logFullPath.append("-").append(CELL_NAME);
			} else {
				logFullPath.append("-").append(env_cell_name);
			}

			String env_node_name = getSysValue("env.node.name");
			if (StringUtils.isBlank(env_node_name)) {
				logFullPath.append("-").append(NODE_NAME);
			} else {
				logFullPath.append("-").append(env_node_name);
			}

			logFullPathStr = logFullPath.toString();
		}
		System.setProperty(GlobalWebContext.LOG_FULL_PATH, logFullPathStr);
		 //INFO 在ServletContext打印的时候有BUG
        sc.log("-=-=-= Print System Env =-=-=- \n" +
                " sys.app.name               = " + appName + "\n" +
                " sys.app.env                = " + currentEnv + "\n" +
                " isProd                        = " + isProdEnv + "\n" +
                " isWindows                     = " + isWindows + "\n" +
                " sys.log.path               = " + logPath + "\n" +
                " sys.log.fullpath           = " + logFullPathStr + "\n" +
                " sys.log.root.level         = " + StringUtils.replace(logRoot, "INFO", "I N F O") + "\n" +
                " sys.log.lowest.level       = " + StringUtils.replace(logLowestLevel.toString(), "INFO", "I N F O") + "\n" +
                " sys.log.spring.level       = " + StringUtils.replace(logSpring, "INFO", "I N F O") + "\n");
	}
	/**
	 * 优化系统变量获取顺序 myApp.properties > catalina.properties > setenv.bat/jdk/tomcat ops > context.xml
	 * @title
	 * @date 2018年9月12日下午2:18:21
	 * @param key
	 * @return
	 */
    public static String getSysValue(String key) {
        //优先取应用 myApp.properties中变量
        String value = GlobalWebContext.getAppCfg(key);
        if (value == null) {
            value = System.getProperty(key); // catalina.properties参数
        }
        if (value == null) {
            value = System.getenv(key);     //setenv.bat中的 或者 java tomcat ops参数
        }

        if (value == null) {
            try {
                value = InitialContext.doLookup("java:comp/env/" + key); //context.xml中的environment
            } catch (NamingException e) {
            }
        }
        return value;
    }
    /**
     * 获取项目应用名
     * @title
     * @date 2018年9月12日下午2:20:24
     * @return
     */
    private static String getAppName() {
        String appName = getSysValue(GlobalWebContext.APP_NAME);

        if (StringUtils.isBlank(appName)) {
            appName = getSysValue(GlobalWebContext.SYSTEM_SYS + GlobalWebContext.APP_NAME);
        }

        if (StringUtils.isBlank(appName)) {
            //文件夹路径防止有分隔符的出现
            appName = StringUtils.replace(GlobalWebContext.getContextPath(), "/", "");
            appName = StringUtils.replace(appName, "\\", "");
        }

        if (StringUtils.isBlank(appName)) {
            //获取基于Tomcat的目录应用名
            String userDir = getSysValue("user.dir");

            String webAppsFolder = StringUtils.substringAfter(userDir, "webapps" + SystemUtils.FILE_SEPARATOR);

            appName = StringUtils.substringBefore(webAppsFolder, SystemUtils.FILE_SEPARATOR);
        }

        if (StringUtils.isBlank(appName)) {
            appName = "UnkownApp";
        }

        return appName;
    }
    
    /***
     * 判断是否为静态资源, 则Filter不过滤静态请求
     *
     * 注：目前模板项目自带404 500页面，会有默认的图片，需要exlcude掉
     * @title
     * @date 2018年9月28日上午10:07:19
     * @param requestURI
     * @return
     */
    public static boolean isStaticResource(String requestURI) {
        return requestURI.endsWith(".htm") || requestURI.endsWith(".html")
                || requestURI.endsWith(".jsp")      //JSP默认也不拦截！
                || requestURI.endsWith(".js") || requestURI.endsWith(".css")
                || requestURI.endsWith(".jpg") || requestURI.endsWith(".jpeg")
                || requestURI.endsWith(".png") || requestURI.endsWith(".gif")
                || requestURI.endsWith(".ico");
    }
}
