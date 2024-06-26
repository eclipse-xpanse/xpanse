/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.logging;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.NonNull;

/**
 * The class wraps a ClientHttpResponse and provides easy access to its properties as a byte array.
 */
public record CustomClientHttpResponse(ClientHttpResponse originalResponse,
                                       byte[] responseBodyBytes) implements ClientHttpResponse {

    @NonNull
    @Override
    public HttpStatusCode getStatusCode() throws IOException {
        return originalResponse.getStatusCode();
    }

    @NonNull
    @Override
    public String getStatusText() throws IOException {
        return originalResponse.getStatusText();
    }

    @NonNull
    @Override
    public HttpHeaders getHeaders() {
        return originalResponse.getHeaders();
    }

    @NonNull
    @Override
    public InputStream getBody() {
        return new ByteArrayInputStream(responseBodyBytes);
    }

    @Override
    public void close() {
        originalResponse.close();
    }
}