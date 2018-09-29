package com.framwork.utils.matcher;


import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

/**
 * Web请求路径的匹配器
 * @title 
 * @author joe
 * @date 2018年9月29日上午9:31:00
 */
public class WebPathMatcher {
    private static final String STAR = "*";

    /**
     * <p>该匹配器采用最轻量的设计，规则如下:</p>
     *
     * <ul>
     * <li>1. 星号匹配（零）个或者多个字符</li>
     * <li>2. 当星号在路径分隔符中表示目录结构是，匹配一个或者多个字符。
     *        如/{@literal *}/api/getInfo.do 表示 /api之前必须有一级及以上目录</li>
     * </ul>
     *
     * <pre>
     * WebPathMatcher.matches("/{@literal *}/index.html", 		            "/user/index.html");
     * WebPathMatcher.matches("{@literal *}/index.html", 			        "/user/index.html");
     * WebPathMatcher.matches("/{@literal *}/index.html", 		            "/user/index.html");
     * WebPathMatcher.matches("/user/{@literal *}/index.html", 	            "/user/api/index.html");
     * WebPathMatcher.matches("/user/{@literal *}", 				        "/user/index.html"));
     * WebPathMatcher.matches("/user{@literal *}/index.html", 	            "/user/index.html");
     * WebPathMatcher.matches("/user{@literal *}/index.html", 	            "/user/api/index.html");
     * WebPathMatcher.matches("/user/{@literal *}index.html", 				"/user/index.html");
     *
     * Above are true!!
     *
     * WebPathMatcher.matches("http://www.memedai.cn/*", 			        "http://www.memedai.cn/query.do");
     * WebPathMatcher.matches("http://www.memedai.cn*", 			        "http://www.memedai.cn.org/query.do"));
     * WebPathMatcher.matches("https://*.memedai.cn/api*", 		            "https://vip.1.memedai.cn/api/handle.do");
     * WebPathMatcher.matches("https://*.memedai.cn/api*", 		            "https://abc.def.memedai.cn/api/test/handle.do");
     * WebPathMatcher.matches("https://*.memedai.cn/api/*", 		        "https://vip.memedai.cn/api/handle.do");
     * WebPathMatcher.matches("https://*.memedai.cn/verify/{@literal *}/", 	"https://vip.memedai.cn/verify/api/handle.do");
     *
     * Above are true!!
     *
     * WebPathMatcher.matches("/user/{@literal *}/index.html", 				"/user/index.html");
     * WebPathMatcher.matches("/user/{@literal *}/index.html", 				"/index.html");
     * WebPathMatcher.matches("/user/{@literal *}/index.html", 				"/u/index.html");
     *
     * WebPathMatcher.matches("https://*.memedai.cn/api/{@literal *}/", 	"https://vip.memedai.cn/api/handle.do");
     * WebPathMatcher.matches("https://*.memedai.cn/api/{@literal *}/", 	"https://vip.memedai.cn/api");
     *
     * Above are false!!
     * </pre>
     */
    public static boolean matches(String pattern, String requestURI) {
        if (StringUtils.isAnyBlank(pattern, requestURI)) {
            return false;
        }

        pattern = pattern.replaceAll("\\s*", "");   //替换空格
        /*
         * 针对Pattern的指针变量
         * [variables]   beginOffset        formerStarOffset        latterStarOffset    ... loop
         *                         prefixPattern         suffixPattern      ... loop
         * [pattern]         |_____________________*______________________*___________________|
         */
        int beginOffset      = 0;   //pattern截取开始位置
        int formerStarOffset = -1;   //前星号的偏移位置
        int latterStarOffset = -1;   //后星号的偏移位置

        String remainingURI  = requestURI;
        String prefixPattern = "";
        String suffixPattern = "";

        boolean result = false;
        do {
            formerStarOffset = StringUtils.indexOf(pattern, STAR, beginOffset);
            prefixPattern = StringUtils.substring(pattern, beginOffset,
                    formerStarOffset > -1 ? formerStarOffset :  pattern.length());

            //匹配前缀Pattern
            result = remainingURI.contains(prefixPattern);
            //已经没有星号，直接返回
            if (formerStarOffset == -1) {
                return result;
            }

            //匹配失败，直接返回
            if (!result)  return false;

            if (StringUtils.isNotEmpty(prefixPattern)) {
                remainingURI = StringUtils.substringAfter(requestURI, prefixPattern);
            }

            //匹配后缀Pattern
            latterStarOffset = StringUtils.indexOf(pattern, STAR, formerStarOffset + 1);
            suffixPattern = StringUtils.substring(pattern, formerStarOffset + 1,
                    latterStarOffset > -1 ? latterStarOffset :  pattern.length());

            result = remainingURI.contains(suffixPattern);
            //匹配失败，直接返回
            if (!result)  return false;

            if (StringUtils.isNotEmpty(suffixPattern)) {
                remainingURI = StringUtils.substringAfter(requestURI, suffixPattern);
            }

            //移动指针
            beginOffset = latterStarOffset + 1;

        } while (StringUtils.isNotBlank(suffixPattern) && StringUtils.isNotBlank(remainingURI));

        return true;
    }

    /**
     * 用于解析xml中配置的路径，用逗号分隔
     * @param paths
     * @return
     */
    public static Set<String> parsePaths(String paths) {
        if (StringUtils.isBlank(paths)) return new HashSet<String>();

        return new HashSet<String>(Arrays.asList(paths.trim().split("\\s*,\\s*")));
    }

    /**
     * 判断请求URI是否与pattern内任一个匹配
     * @param patterns 通配集合
     * @param requestURI 被匹配的请求URI
     * @return
     */
    public static boolean matchAny(Set<String> patterns, String requestURI) {
        if (patterns == null) {
            return false;
        }

        if (!requestURI.startsWith("/")) {
            requestURI = "/" + requestURI;
        }

        for (String pattern : patterns) {
            if (WebPathMatcher.matches(pattern, requestURI)) {
                return true;
            }
        }

        return false;
    }

    public static void main(String[] args) {
        System.out.println(matches("/*/index.html", "/user/index.html"));
        System.out.println(matches("*/index.html", "/user/index.html"));
        System.out.println(matches("/*/index.html", "/user/index.html"));
        System.out.println(matches("/user/*/index.html", "/user/api/index.html"));
        System.out.println(matches("/user/*", "/user/index.html"));
        System.out.println(matches("/user*/index.html", "/user/index.html"));
        System.out.println(matches("/user*/index.html", "/user/api/index.html"));
        System.out.println(matches("/user/*index.html", "/user/index.html"));
        System.out.println(matches("/user/*index.html", "/user/api/index.html"));
        System.out.println("*********以上为true 1 ***********");

        System.out.println(matches("http://www.memedai.cn/*", "http://www.memedai.cn/query.do"));
        System.out.println(matches("http://www.memedai.cn*", "http://www.memedai.cn.org/query.do"));
        System.out.println(matches("https://*.memedai.cn/api*", "https://vip.1.memedai.cn/api/handle.do"));
        System.out.println(matches("https://*.memedai.cn/api*", "https://abc.def.memedai.cn/api/test/handle.do"));
        System.out.println(matches("https://*.memedai.cn/api/*", "https://vip.memedai.cn/api/handle.do"));
        System.out.println(matches("https://*.memedai.cn/verify/*/", "https://vip.memedai.cn/verify/api/handle.do"));

        System.out.println("*********以上为true 2 ***********");

        System.out.println(matches("/user/*/index.html", "/user/index.html"));  //false
        System.out.println(matches("/user/*/index.html", "/index.html"));
        System.out.println(matches("/user/*/index.html", "/u/index.html"));
        System.out.println(matches("https://*.memedai.cn/api/*/", "https://vip.memedai.cn/api/handle.do"));
        System.out.println(matches("https://*.memedai.cn/api/*/", "https://vip.memedai.cn/api"));
    }
}
