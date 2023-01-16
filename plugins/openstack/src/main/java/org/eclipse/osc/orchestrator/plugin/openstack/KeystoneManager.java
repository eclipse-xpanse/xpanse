package org.eclipse.osc.orchestrator.plugin.openstack;

import org.apache.karaf.minho.boot.service.ConfigService;
import org.apache.karaf.minho.boot.service.ServiceRegistry;
import org.openstack4j.api.OSClient;
import org.openstack4j.core.transport.Config;
import org.openstack4j.model.common.Identifier;
import org.openstack4j.openstack.OSFactory;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Objects;

public class KeystoneManager {

    public static OSClient.OSClientV3 getClient(ServiceRegistry serviceRegistry) {
        ConfigService configService = serviceRegistry.get(ConfigService.class);
        OSClient.OSClientV3 osClient = null;
        if (configService == null) {
            throw new IllegalStateException("Config service is not available");
        }

        String endpoint = configService.getProperty("openstack.endpoint", "http://127.0.0.1:5000/v3");
        String domainId = null;
        if (configService.getProperty("openstack.domainId") != null) {
            domainId = configService.getProperty("openstack.domainId");
        }
        String domainName = null;
        if (configService.getProperty("openstack.domainName") != null) {
            domainName = configService.getProperty("openstack.domainName");
        }
        String projectId = null;
        if (configService.getProperty("openstack.projectId") != null) {
            projectId = configService.getProperty("openstack.projectId");
        }
        String projectName = null;
        if (configService.getProperty("openstack.projectName") != null) {
            projectName = configService.getProperty("openstack.projectName");
        }
        String username = configService.getProperty("openstack.username", "admin");
        String secret = configService.getProperty("openstack.secret", "secret");
        boolean isSslValidationEnabled = Boolean.getBoolean(configService.getProperty(
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
            OSFactory.enableHttpLoggingFilter(true);
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
}
