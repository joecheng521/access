package com.framwork.cat.ext.vaildator;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class CatExtValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(CatExtValidator.class);

    public static final String SUMMARY_KEY = "ROOSTER";

    private volatile static CatExtValidator INSTANCE = null;

    private static boolean isCatEnabled = false;   //是否类都存在
    private static boolean isCatClientConfig = false;   //存在client.xml的配置文件
    private static boolean isValidIp         = false;   //IPv4获取必须整数，即host.ip必须配置

    private static boolean isDisableCatAll   = false;   //禁用CAT，优先级高于其他

    private static boolean isDisableLogback  = false;   //禁用logback.error的拦截
    private static boolean isDisableDubbo    = false;   //禁用dubbo的拦截
    private static boolean isDisableHttp     = false;   //禁用http的拦截

    //开关全部的cat集成
    private static final String ENV_DISABLE_CAT_ALL         = "env.cat.disable.all";
    //开关logback.error的cat集成
    private static final String ENV_DISABLE_CAT_4_LOGBACK   = "env.cat.disable.logback";
    //开关dubbo的cat集成
    private static final String ENV_DISABLE_CAT_4_DUBBO     = "env.cat.disable.dubbo";
    //开关http的cat集成
    private static final String ENV_DISABLE_CAT_4_HTTP      = "env.cat.disable.http";

    private CatExtValidator() {}

    public static CatExtValidator getInstance() {
        if (INSTANCE == null) {
            synchronized (CatExtValidator.class) {
                if (INSTANCE == null) {
                    INSTANCE = new CatExtValidator();

                    INSTANCE.initialize();
                }
            }
        }

        return INSTANCE;
    }

    private String getProperty(String key) {
        String value = System.getProperty(key); // catalina.properties参数

        if (value == null) {
            value = System.getenv(key);     //setenv.bat中的 或者 java tomcat ops参数
        }

        return value;
    }

    private void initialize() {
        try {
            isCatEnabled = Cat.getManager().isCatEnabled();
            LOGGER.info("**** Cat classes in JVM and domain in app.properties ? [{}] ****", isCatEnabled);
        } catch (Throwable e) {
            LOGGER.error("**** Cat classes in JVM and domain in app.properties ? [false] ****", e);
            isCatEnabled = false;
        }

        //禁用Cat All的环境变量
        String disableCatAll = getProperty(ENV_DISABLE_CAT_ALL);
        LOGGER.info("**** Cat disableCatAll is [{}] ****", disableCatAll);
        if (Boolean.parseBoolean(disableCatAll)) {
            isDisableCatAll  = true;
            return;
        }

        //校验是否存在/data/appdatas/cat/client.xml配置文件
        File f = new File(Cat.getCatHome(), "client.xml");
        LOGGER.info("**** Cat client.xml exists? [{}] ****", f.exists());
        if (f.exists()) {
            isCatClientConfig = true;
        }

        //校验ip是否有效，否则需配置 host.ip
        String ip = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();
        LOGGER.info("**** Cat Host Ip is [{}] ****", ip);
        isValidIp = InetAddressValidator.getInstance().isValidInet4Address(ip);

        String disableCatLogback = getProperty(ENV_DISABLE_CAT_4_LOGBACK);
        String disableCatDubbo   = getProperty(ENV_DISABLE_CAT_4_DUBBO);
        String disableCatHttp    = getProperty(ENV_DISABLE_CAT_4_HTTP);

        isDisableLogback = Boolean.parseBoolean(disableCatLogback);
        isDisableDubbo   = Boolean.parseBoolean(disableCatDubbo);
        isDisableHttp    = Boolean.parseBoolean(disableCatHttp);

        LOGGER.info("\n" +
                "******************************************\n" +
                "Cat Environment Variables: \n" +
                "isCatEnabled       = {} \n" +
                "isCatClientConfig  = {} \n" +
                "isValidIp          = {} \n" +
                "\n" +
                "isDisableCatAll    = {} \n" +
                "isDisableLogback   = {} \n" +
                "isDisableDubbo     = {} \n" +
                "isDisableHttp      = {} \n" +
                "\n" +
                "^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
                "isCatWorks         = {} \n" +
                "******************************************",
                isCatEnabled, isCatClientConfig, isValidIp,
                isDisableCatAll, isDisableLogback, isDisableDubbo, isDisableHttp,
                isCatWorks());
    }

    /**
     * 判断Cat是否可以正常工作
     * @return
     */
    public boolean isCatWorks() {
        return isCatEnabled && !isDisableCatAll && isCatClientConfig && isValidIp;
    }

    /**
     * true=禁用logback.error()的CAT监控
     * @return
     */
    public boolean isDisableLogback() {
        return !isCatWorks() || isDisableLogback;
    }

    /**
     * true=禁用dubbo的CAT监控
     * @return
     */
    public boolean isDisableDubbo() {
        return !isCatWorks() || isDisableDubbo;
    }

    /**
     * true=禁用http的CAT监控
     * @return
     */
    public boolean isDisableHttp() {
        return !isCatWorks() || isDisableHttp;
    }
}
