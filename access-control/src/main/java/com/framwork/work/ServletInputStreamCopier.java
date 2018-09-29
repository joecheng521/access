package com.framwork.work;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;


public class ServletInputStreamCopier extends ServletInputStream {
    private ByteArrayInputStream bais;

    public ServletInputStreamCopier(byte[] in) {
        this.bais = new ByteArrayInputStream(in);
    }

    @Override
    public boolean isFinished() {
        return bais.available() == 0;
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setReadListener(ReadListener readListener) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public int read() throws IOException {
        return this.bais.read();
    }
}
