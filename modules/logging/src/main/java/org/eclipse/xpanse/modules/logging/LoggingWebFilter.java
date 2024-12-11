/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.logging;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** HTTP Filter to set tracking ID to the thread. This filter runs first. */
@Component
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LoggingWebFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String trackingId = UUID.randomUUID().toString();
        MDC.put(LoggingKeyConstant.TRACKING_ID, trackingId);
        log.debug("Intercept incoming request and set MDC context information");
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        httpResponse.setHeader(LoggingKeyConstant.HEADER_TRACKING_ID, trackingId);
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
