/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.security.hmac;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.common.exceptions.ClientAuthenticationFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** This filter is executed only for certain URLs. */
@Component
@Slf4j
public class HmacAuthenticationFilter extends OncePerRequestFilter {

    private final HmacSignatureHeaderManage hmacSignatureHeaderManage;

    @Autowired
    public HmacAuthenticationFilter(HmacSignatureHeaderManage hmacSignatureHeaderManage) {
        this.hmacSignatureHeaderManage = hmacSignatureHeaderManage;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        log.debug("HMAC Authentication Filter executing.");
        RereadbleBodyHttpServletRequest cachedRequest =
                new RereadbleBodyHttpServletRequest(request);
        try {
            boolean isSignatureValidationSuccessful =
                    hmacSignatureHeaderManage.validateHmacSignature(cachedRequest);
            if (!isSignatureValidationSuccessful) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "HMAC Signature invalid");
            } else {
                doFilter(cachedRequest, response, filterChain);
            }
        } catch (ClientAuthenticationFailedException e) {
            log.error("HMAC Signature validation failed", e);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "HMAC Signature invalid");
        }
    }
}
