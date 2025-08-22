/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.common.proxy;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.common.exceptions.XpanseUnhandledException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * This class reads standard linux format proxy configuration and injects java like system
 * properties. The libraries which depend on standard java proxy system properties will work without
 * any further configuration.
 */
@Slf4j
@Getter
@Component
public final class ProxyConfigurationManager {

    private final ProxyDetails httpProxyDetails;
    private final ProxyDetails httpsProxyDetails;
    private final String nonProxyHosts;

    @Autowired
    private ProxyConfigurationManager(Environment environment) {
        String httpProxyUrl =
                getCaseInsensitiveProxyVariable(
                        environment, ProxyVariables.HTTP_PROXY_ENVIRONMENT_VAR);
        String httpsProxyUrl =
                getCaseInsensitiveProxyVariable(
                        environment, ProxyVariables.HTTPS_PROXY_ENVIRONMENT_VAR);
        this.nonProxyHosts =
                getCaseInsensitiveProxyVariable(
                        environment, ProxyVariables.NO_PROXY_ENVIRONMENT_VAR);
        if (Objects.nonNull(httpProxyUrl) && !httpProxyUrl.isBlank()) {
            this.httpProxyDetails = extractProxyDetails(httpProxyUrl, true);

        } else {
            this.httpProxyDetails = null;
        }
        if (Objects.nonNull(httpsProxyUrl) && !httpsProxyUrl.isBlank()) {
            this.httpsProxyDetails = extractProxyDetails(httpsProxyUrl, false);

        } else {
            this.httpsProxyDetails = null;
        }
    }

    /**
     * Method extracts the proxy credentials from the proxy URLs.
     *
     * @param proxyUrl proxy URLs read from the environment.
     * @return array of username and password.
     */
    private String[] extractProxyCredentials(String proxyUrl) {
        String[] parts = proxyUrl.split("@");
        if (parts.length == 2) {
            String credentials =
                    parts[0].replaceFirst("^(http://|https://)", ""); // Remove protocol
            return credentials.split(":");
        }
        return new String[0];
    }

    /** Priority is given to the lower case proxy variables which are the linux standards. */
    private String getCaseInsensitiveProxyVariable(Environment environment, String proxyVariable) {
        if (environment.getProperty(proxyVariable) != null) {
            return environment.getProperty(proxyVariable);
        } else if (environment.getProperty(proxyVariable.toUpperCase()) != null) {
            return environment.getProperty(proxyVariable.toUpperCase());
        }
        return null;
    }

    private ProxyDetails extractProxyDetails(String proxyUrl, boolean isHttpProxy) {
        try {
            URI uri = new URI(proxyUrl);
            String[] proxyCredentials = extractProxyCredentials(proxyUrl);
            ProxyDetails proxyDetails =
                    new ProxyDetails.ProxyDetailsBuilder()
                            .proxyUrl(proxyUrl)
                            .proxyHost(uri.getHost())
                            .proxyPort(
                                    uri.getPort() != -1
                                            ? uri.getPort()
                                            : URL.of(uri, null).getDefaultPort())
                            .proxyUsername(proxyCredentials.length > 0 ? proxyCredentials[0] : null)
                            .proxyPassword(proxyCredentials.length > 0 ? proxyCredentials[1] : null)
                            .build();
            System.setProperty(
                    isHttpProxy ? "http.proxyHost" : "https.proxyHost",
                    proxyDetails.getProxyHost());
            System.setProperty(
                    isHttpProxy ? "http.proxyPort" : "https.proxyPort",
                    String.valueOf(proxyDetails.getProxyPort()));
            return proxyDetails;
        } catch (URISyntaxException | MalformedURLException e) {
            log.error("Error parsing proxy information", e);
            throw new XpanseUnhandledException(e.getMessage());
        }
    }
}
