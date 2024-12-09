/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.plugins.openstack.common.auth;

import jakarta.annotation.Resource;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.credential.CredentialCenter;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentEntity;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentStorage;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateStorage;
import org.eclipse.xpanse.modules.deployment.utils.DeployEnvironments;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.common.exceptions.ClientAuthenticationFailedException;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.plugins.openstack.common.auth.constants.OpenstackCommonEnvironmentConstants;
import org.eclipse.xpanse.plugins.openstack.common.auth.keystone.OpenstackKeystoneManager;
import org.eclipse.xpanse.plugins.openstack.common.auth.keystone.ScsKeystoneManager;
import org.openstack4j.api.OSClient;
import org.openstack4j.api.exceptions.AuthenticationException;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.stereotype.Component;

/**
 * Defines methods to resolve all the auth info of the cloud service provider.
 */
@Slf4j
@Component
public class ProviderAuthInfoResolver {

    @Resource
    private CredentialCenter credentialCenter;
    @Resource
    private DeployEnvironments deployEnvironments;
    @Resource
    private ServiceDeploymentStorage serviceDeploymentStorage;
    @Resource
    private Environment environment;
    @Resource
    private ServiceTemplateStorage serviceTemplateStorage;
    @Resource
    private OpenstackKeystoneManager openstackKeystoneManager;
    @Resource
    private ScsKeystoneManager scsKeystoneManager;

    /**
     * Get the mapping key of the env variable OS_AUTH_URL by csp.
     *
     * @param csp cloud service provider
     * @return the mapping key of the env variable OS_AUTH_URL.
     */
    public String getAuthUrlKeyByCsp(Csp csp) {
        return switch (csp) {
            case OPENSTACK_TESTLAB ->
                    OpenstackCommonEnvironmentConstants.OPENSTACK_TESTLAB_AUTH_URL;
            case PLUS_SERVER -> OpenstackCommonEnvironmentConstants.PLUS_SERVER_AUTH_URL;
            case REGIO_CLOUD -> OpenstackCommonEnvironmentConstants.REGIO_CLOUD_AUTH_URL;
            default -> OpenstackCommonEnvironmentConstants.OS_AUTH_URL;
        };
    }


    /**
     * Get the authenticated client for csp.
     *
     * @param csp cloud service provider
     * @return authenticated client
     */
    public OSClient.OSClientV3 getAuthenticatedClientForCsp(
            Csp csp, String site, String userId, UUID serviceId, UUID serviceTemplateId) {
        String authUrl = getProviderAuthUrl(csp, serviceId, serviceTemplateId);
        AbstractCredentialInfo credential = getAuthCredential(csp, site, userId);
        return switch (csp) {
            case PLUS_SERVER, REGIO_CLOUD ->
                    scsKeystoneManager.getAuthenticatedClient(authUrl, credential);
            default -> openstackKeystoneManager.getAuthenticatedClient(authUrl, credential);
        };
    }

    /**
     * Get the auth url by csp.
     *
     * @param csp cloud service provider
     * @return auth url
     */
    public String getProviderAuthUrl(Csp csp, UUID serviceId, UUID serviceTemplateId) {
        String authUrl;
        if (Objects.isNull(serviceId) && Objects.isNull(serviceTemplateId)) {
            authUrl = this.environment.getProperty(getAuthUrlKeyByCsp(csp));
            log.info("Using auth url {} of provider {} from environment", authUrl, csp.toValue());
        } else {
            authUrl = getAuthUrlFromDeploymentVariables(csp, serviceId, serviceTemplateId);
            log.info("Using auth url {} of provider {} from the deploy variables of {} {}",
                    authUrl,
                    csp.toValue(),
                    Objects.nonNull(serviceTemplateId) ? "service template" : "service",
                    Objects.nonNull(serviceTemplateId) ? serviceTemplateId : serviceId);
        }
        if (StringUtils.isBlank(authUrl)) {
            String errorMsg = String.format("The value of auth url of the provider "
                    + "%s is not configured.", csp);
            throw new IllegalArgumentException(errorMsg);
        }
        try {
            URI uri = new URI(authUrl);
            return uri.toString();
        } catch (URISyntaxException e) {
            String errorMsg = String.format("The configured value [%s] of auth url of the provider "
                    + "%s is not a valid url.", authUrl, csp);
            throw new IllegalArgumentException(errorMsg);
        }
    }

    /**
     * Get the auth credential by csp and userId.
     *
     * @param csp    cloud service provider
     * @param userId user id
     * @return credential
     */
    public AbstractCredentialInfo getAuthCredential(Csp csp, String site, String userId) {
        return credentialCenter.getCredential(csp, site, CredentialType.VARIABLES, userId);
    }


    /**
     * Get the auth url from deployment variables.
     *
     * @param csp       csp
     * @param serviceId serviceId
     * @return auth url
     */
    private String getAuthUrlFromDeploymentVariables(
            Csp csp, UUID serviceId, UUID serviceTemplateId) {
        Object defaultAuthUrl = null;
        if (Objects.nonNull(serviceId)) {
            ServiceDeploymentEntity serviceDeploymentEntity =
                    serviceDeploymentStorage.findServiceDeploymentById(serviceId);
            ServiceTemplateEntity serviceTemplateEntity =
                    serviceTemplateStorage.getServiceTemplateById(
                            serviceDeploymentEntity.getServiceTemplateId());
            Map<String, Object> serviceRequestVariables =
                    this.deployEnvironments.getAllDeploymentVariablesForService(
                            serviceDeploymentEntity.getDeployRequest()
                                    .getServiceRequestProperties(),
                            serviceTemplateEntity.getOcl().getDeployment()
                                    .getVariables(),
                            serviceDeploymentEntity.getFlavor(),
                            serviceTemplateEntity.getOcl()
                    );
            defaultAuthUrl = serviceRequestVariables.get(
                    OpenstackCommonEnvironmentConstants.OS_AUTH_URL);
        }
        if (Objects.nonNull(serviceTemplateId)) {
            ServiceTemplateEntity serviceTemplateEntity =
                    serviceTemplateStorage.getServiceTemplateById(serviceTemplateId);
            Map<String, Object> getServiceTemplateVariables =
                    this.deployEnvironments.getFixedVariablesFromTemplate(serviceTemplateEntity);
            defaultAuthUrl = getServiceTemplateVariables.get(
                    OpenstackCommonEnvironmentConstants.OS_AUTH_URL);
        }
        if (Objects.isNull(defaultAuthUrl)) {
            return this.environment.getProperty(getAuthUrlKeyByCsp(csp));
        } else {
            return (String) defaultAuthUrl;
        }
    }


    /**
     * Handle auth exception for spring retry.
     *
     * @param ex Exception
     */
    public void handleAuthExceptionForSpringRetry(Exception ex) {
        int retryCount = Objects.isNull(RetrySynchronizationManager.getContext())
                ? 0 : RetrySynchronizationManager.getContext().getRetryCount();
        log.error(ex.getMessage() + System.lineSeparator() + "Retry count:" + retryCount);
        if (ex instanceof ClientAuthenticationFailedException) {
            throw new ClientAuthenticationFailedException(ex.getMessage());
        }
        AuthenticationException authenticationException = getAuthenticationException(ex);
        if (Objects.nonNull(authenticationException)) {
            int statusCode = authenticationException.getStatus();
            if (statusCode == HttpStatus.UNAUTHORIZED.value()
                    || statusCode == HttpStatus.FORBIDDEN.value()) {
                throw new ClientAuthenticationFailedException(ex.getMessage());
            }
        }
    }


    private AuthenticationException getAuthenticationException(Throwable ex) {
        if (Objects.isNull(ex)) {
            return null;
        }
        if (ex instanceof AuthenticationException authException) {
            return authException;
        }
        if (Objects.nonNull(ex.getCause())) {
            return getAuthenticationException(ex.getCause());
        }
        return null;
    }
}
