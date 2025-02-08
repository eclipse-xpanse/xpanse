/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.security.hmac;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.util.StreamUtils;

/**
 * Custom HTTP Servlet Request object which reads the request input stream and caches it. Rest of
 * the filters will re-use the request from the cached stream. This is at the moment used only in
 * HMAC authentication filter.
 */
public class RereadbleBodyHttpServletRequest extends HttpServletRequestWrapper {
    byte[] cachedBody;

    RereadbleBodyHttpServletRequest(HttpServletRequest request) throws IOException {
        super(request);
        this.cachedBody = StreamUtils.copyToByteArray(request.getInputStream());
    }

    @Override
    public ServletInputStream getInputStream() {
        return new ReadBodyServletInputStream(this.cachedBody);
    }

    private static class ReadBodyServletInputStream extends ServletInputStream {
        private final InputStream cachedBodyServletInputStream;

        public ReadBodyServletInputStream(byte[] cachedBody) {
            this.cachedBodyServletInputStream = new ByteArrayInputStream(cachedBody);
        }

        @Override
        public boolean isFinished() {
            try {
                return cachedBodyServletInputStream.available() == 0;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener listener) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int read() throws IOException {
            return cachedBodyServletInputStream.read();
        }
    }
}
