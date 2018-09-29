package com.framwork.work;

import org.apache.commons.lang3.StringUtils;

/**
 * 访问类型
 * @title 
 * @author joe
 * @date 2018年9月29日上午10:03:25
 */
public enum HttpContentTypeEnum {
    TEXT_PLAIN                  ("text/plain"),
    TEXT_HTML                   ("text/html"),
    APP_JSON                    ("application/json"),
    APP_JSON_CHARSET            ("application/json;charset=UTF-8"),
    APP_X_WWW_FORM_URLENCODE    ("application/x-www-form-urlencoded"),
    MUL_FORM                    ("multipart/form-data");

    private String contentType;

    private HttpContentTypeEnum(String contentType) {
        this.contentType = contentType;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * 判断传入的contentType是否与枚举一致
     *
     * @param requestContentType
     * @return
     */
    public boolean equals(String requestContentType) {
        return StringUtils.startsWithIgnoreCase(requestContentType, this.contentType);
    }
}
