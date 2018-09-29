package com.framework.utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 对HTTP请求中的参数进行校验，并组装应答
 * @title 
 * @author joe
 * @date 2018年9月29日下午1:53:15
 */
public interface IAppServletValidator {
    boolean validate(HttpServletRequest request, HttpServletResponse response) throws Exception;
}
