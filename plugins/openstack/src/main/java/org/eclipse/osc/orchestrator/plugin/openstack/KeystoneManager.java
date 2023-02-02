/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.osc.orchestrator.plugin.openstack;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Objects;
import org.openstack4j.api.OSClient;
import org.openstack4j.core.transport.Config;
import org.openstack4j.model.common.Identifier;
import org.openstack4j.openstack.OSFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Bean to handle all requests to the Keystone API.
 */
@Component
public class KeystoneManager {

    Environment environment;

    @Autowired
    public KeystoneManager(Environment environment) {
        this.environment = environment;
    }

    private static String getIpAddressFromUrl(String url) {
        try {
            return InetAddress.getByName(new URL(url).getHost()).getHostAddress();
        } catch (UnknownHostException | MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private static Config buildClientConfig(String endpoint, boolean isSslVerificationEnabled) {
        Config config = Config.newConfig().withEndpointNATResolution(getIpAddressFromUrl(endpoint));
        if (!isSslVerificationEnabled) {
            config.withSSLVerificationDisabled();
        }
        return config;
    }

    /**
     * Method fully instantiates an Openstack client object which must be used in all further
     * communications to the Openstack.
     *
     * @return OpenStack Client object which is authenticated with the Openstack instance.
     */
    public OSClient.OSClientV3 getClient() {
        OSClient.OSClientV3 osClient = null;
        OSFactory.enableHttpLoggingFilter(true);
        String endpoint =
                this.environment.getProperty("openstack.endpoint", "http://127.0.0.1:5000/v3");
        String domainId = null;
        if (this.environment.getProperty("openstack.domainId") != null) {
            domainId = this.environment.getProperty("openstack.domainId");
        }
        String domainName = null;
        if (this.environment.getProperty("openstack.domainName") != null) {
            domainName = this.environment.getProperty("openstack.domainName");
        }
        String projectId = null;
        if (this.environment.getProperty("openstack.projectId") != null) {
            projectId = this.environment.getProperty("openstack.projectId");
        }
        String projectName = null;
        if (this.environment.getProperty("openstack.projectName") != null) {
            projectName = this.environment.getProperty("openstack.projectName");
        }
        String username = this.environment.getProperty("openstack.username", "admin");
        String secret = this.environment.getProperty("openstack.secret", "secret");
        boolean isSslValidationEnabled = Boolean.getBoolean(this.environment.getProperty(
                "openstack.enableSslCertificateValidation", "true"));

        Identifier domainIdentifier = null;
        if (domainId != null) {
            domainIdentifier = Identifier.byId(domainId);
        }
        if (domainName != null) {
            domainIdentifier = Identifier.byName(domainName);
        }

        Identifier projectIdentifier = null;
        if (projectId != null) {
            projectIdentifier = Identifier.byId(projectId);
        }
        if (projectName != null) {
            projectIdentifier = Identifier.byName(projectName);
        }

        if (domainIdentifier != null && projectIdentifier != null) {
            osClient = OSFactory.builderV3()
                    .endpoint(endpoint)
                    .credentials(username, secret)
                    .scopeToProject(projectIdentifier, domainIdentifier)
                    .authenticate();
        }
        if (domainIdentifier == null && projectIdentifier != null) {
            osClient = OSFactory.builderV3()
                    .endpoint(endpoint)
                    .credentials(username, secret)
                    .scopeToProject(projectIdentifier)
                    .authenticate();
        }
        if (domainIdentifier != null && projectIdentifier == null) {
            osClient = OSFactory.builderV3()
                    .endpoint(endpoint)
                    .credentials(username, secret, domainIdentifier)
                    .withConfig(buildClientConfig(endpoint, isSslValidationEnabled))
                    .authenticate();
        }
        if (domainIdentifier == null && projectIdentifier == null) {
            osClient = OSFactory.builderV3()
                    .endpoint(endpoint)
                    .credentials(username, secret)
                    .withConfig(buildClientConfig(endpoint, isSslValidationEnabled))
                    .authenticate();
        }
        if (Objects.isNull(osClient)) {
            throw new IllegalStateException("Openstack client could not be instantiated");
        }
        return osClient;
    }
}
