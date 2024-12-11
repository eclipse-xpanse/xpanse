/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.common.openapi;

import java.net.InetAddress;
import java.net.UnknownHostException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/** Bean to provide helper methods for generating OpenAPI files. */
@Slf4j
@Component
public class OpenApiUrlManage {

    private final String openapiPath;
    private final Integer port;

    /** OpenApiUrlManage constructor. */
    @Autowired
    public OpenApiUrlManage(
            @Value("${openapi.path:openapi/}") String openapiPath,
            @Value("${server.port:8080}") Integer port) {
        this.openapiPath = openapiPath;
        this.port = port;
    }

    /**
     * Get API url from headers. This ensures the correct URL returned to client even when the
     * request is routed via a load balancer.
     *
     * @return serviceUrl to access openapi URL.
     */
    public String getServiceUrl() {
        try {
            return ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        } catch (Exception e) {
            String host = "localhost";
            try {
                InetAddress address = InetAddress.getLocalHost();
                host = address.getHostAddress();
            } catch (UnknownHostException ex) {
                log.error("Get localHost error.", ex);
            }
            return "http://" + host + ":" + port;
        }
    }

    /**
     * Get openApi Url.
     *
     * @return openApiUrl
     */
    public String getOpenApiUrl(String id) {
        if (openapiPath.endsWith("/")) {
            return getServiceUrl() + "/" + openapiPath + id + ".html";
        }
        return getServiceUrl() + "/" + openapiPath + "/" + id + ".html";
    }
}
