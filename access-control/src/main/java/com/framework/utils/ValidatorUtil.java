package com.framework.utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 验证器工具类
 * @title 
 * @author joe
 * @date 2018年9月29日上午11:33:36
 */
public class ValidatorUtil {
    public static boolean doValidation(String validatorName,
                                       HttpServletRequest request,
                                       HttpServletResponse response) throws Exception {
        try {
            Class<?> validatorCls = Class.forName(validatorName);
            IAppServletValidator validator = (IAppServletValidator) validatorCls.newInstance();
            return validator.validate(request, response);
        } catch (Exception e) {

            return false;
        }
    }
}
