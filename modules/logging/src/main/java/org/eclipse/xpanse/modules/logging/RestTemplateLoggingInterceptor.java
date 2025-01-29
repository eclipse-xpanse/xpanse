/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.logging;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.util.StreamUtils;

/** The class logs HTTP requests and responses made by RestTemplate. */
@Configuration
@Slf4j
public class RestTemplateLoggingInterceptor implements ClientHttpRequestInterceptor {

    @Override
    @NonNull
    public ClientHttpResponse intercept(
            @NonNull HttpRequest request,
            @NonNull byte[] body,
            @NonNull ClientHttpRequestExecution execution)
            throws IOException {
        long startTime = System.currentTimeMillis();
        logRequest(request, body);
        ClientHttpResponse originalResponse = execution.execute(request, body);
        byte[] responseBodyBytes = StreamUtils.copyToByteArray(originalResponse.getBody());
        logResponse(originalResponse, responseBodyBytes, startTime);
        return new CustomClientHttpResponse(originalResponse, responseBodyBytes);
    }

    private void logRequest(HttpRequest request, byte[] body) {
        if (log.isInfoEnabled()) {
            String requestBody = new String(body, StandardCharsets.UTF_8);
            final StringBuilder requestResult = new StringBuilder(requestBody.length() + 2048);
            requestResult.append("Request: ");
            requestResult.append(request.getMethod());
            requestResult.append(' ');
            requestResult.append(request.getURI());
            requestResult.append(' ');
            writeBody(requestBody, requestResult);
            log.info(requestResult.toString());
        }
    }

    private void logResponse(ClientHttpResponse response, byte[] responseBodyBytes, long startTime)
            throws IOException {
        if (log.isInfoEnabled()) {
            String responseBody = new String(responseBodyBytes, StandardCharsets.UTF_8);
            final StringBuilder responseResult = new StringBuilder(responseBody.length() + 2048);
            responseResult.append("Response: ");
            responseResult.append(response.getStatusCode());
            String statusText = response.getStatusText();
            responseResult.append(' ');
            responseResult.append(statusText);
            responseResult.append(" Duration: ");
            responseResult.append(System.currentTimeMillis() - startTime);
            responseResult.append("ms");
            responseResult.append(' ');
            writeBody(responseBody, responseResult);
            log.info(responseResult.toString());
        }
    }

    private void writeBody(final String body, final StringBuilder output) {
        if (!body.isEmpty()) {
            output.append(' ');
            output.append(body);
        } else {
            output.setLength(output.length() - 1); // discard last newline
        }
    }
}
