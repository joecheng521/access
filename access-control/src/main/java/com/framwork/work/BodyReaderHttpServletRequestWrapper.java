package com.framwork.work;

import org.springframework.util.StreamUtils;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;

/**
 * 解决request只能获取一次getInputStream的问题
 * @title 
 * @author joe
 * @date 2018年9月29日上午10:48:20
 */
public class BodyReaderHttpServletRequestWrapper extends HttpServletRequestWrapper {
    private final byte[] bodyCopier;

    public BodyReaderHttpServletRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        bodyCopier = StreamUtils.copyToByteArray(request.getInputStream());
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(this.getInputStream()));
    }

    @Override
    public ServletInputStream getInputStream() throws IOException{
        return new ServletInputStreamCopier(bodyCopier);
    }

    public byte[] getCopy() {
        return this.bodyCopier;
    }

    public String getBody() throws UnsupportedEncodingException {
        return new String(this.bodyCopier, "UTF-8");
    }
}
