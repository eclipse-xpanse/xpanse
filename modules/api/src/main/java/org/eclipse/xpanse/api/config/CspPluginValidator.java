/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.api.config;

import jakarta.annotation.Resource;
import java.net.URI;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.eclipse.xpanse.api.controllers.ServiceTemplateApi;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.credential.CreateCredential;
import org.eclipse.xpanse.modules.models.policy.userpolicy.UserPolicyCreateRequest;
import org.eclipse.xpanse.modules.models.policy.userpolicy.UserPolicyUpdateRequest;
import org.eclipse.xpanse.modules.models.service.deploy.DeployRequest;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
import org.eclipse.xpanse.modules.models.workflow.migrate.MigrateRequest;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.springframework.stereotype.Component;

/**
 * Validate whether the plugin for csp is enabled.
 */
@Slf4j
@Aspect
@Component
public class CspPluginValidator {

    @Resource
    private PluginManager pluginManager;
    @Resource
    private OclLoader oclLoader;

    private void validatePluginForCspIsActive(Csp csp) {
        if (Objects.nonNull(csp)) {
            pluginManager.getOrchestratorPlugin(csp);
        }
    }


    /**
     * Pointcut for all controller methods.
     */
    @Pointcut("execution(* org.eclipse.xpanse.api.controllers.*.*(..))")
    public void controllerMethods() {
    }

    /**
     * Validate request parameters.
     *
     * @param joinPoint The join point.
     */
    @Before("controllerMethods()")
    public void validateRequest(JoinPoint joinPoint) {
        // Validate request parameters here
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg instanceof Ocl ocl) {
                validatePluginForCspIsActive(ocl.getCloudServiceProvider().getName());
            } else if (arg instanceof DeployRequest deployRequest) {
                validatePluginForCspIsActive(deployRequest.getCsp());
            } else if (arg instanceof MigrateRequest migrateRequest) {
                validatePluginForCspIsActive(migrateRequest.getCsp());
            } else if (arg instanceof CreateCredential createCredential) {
                validatePluginForCspIsActive(createCredential.getCsp());
            } else if (arg instanceof UserPolicyCreateRequest userPolicyCreateRequest) {
                validatePluginForCspIsActive(userPolicyCreateRequest.getCsp());
            } else if (arg instanceof UserPolicyUpdateRequest userPolicyUpdateRequest) {
                validatePluginForCspIsActive(userPolicyUpdateRequest.getCsp());
            } else if (arg instanceof Csp csp) {
                validatePluginForCspIsActive(csp);
            }
        }
        // Validate Ocl in fetch methods
        validateOclInFetchMethods(joinPoint);
    }


    private void validateOclInFetchMethods(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        log.info("Validate request parameters for method: {}", methodName);
        String fetchMethodName = null;
        String fetchUpdateMethodName = null;
        try {
            fetchMethodName = ServiceTemplateApi.class.getDeclaredMethod("fetch", String.class)
                    .getName();
            fetchUpdateMethodName = ServiceTemplateApi.class.getDeclaredMethod("fetchUpdate",
                    String.class, String.class).getName();
        } catch (NoSuchMethodException e) {
            log.error("Failed to get fetch or fetchUpdate method name", e);
        }
        if (StringUtils.equals(fetchMethodName, methodName)) {
            Object[] args = joinPoint.getArgs();
            String oclLocation = (String) args[0];
            Ocl ocl = getOclByLocation(oclLocation);
            validatePluginForCspIsActive(ocl.getCloudServiceProvider().getName());
        }
        if (StringUtils.equals(fetchUpdateMethodName, methodName)) {
            Object[] args = joinPoint.getArgs();
            String oclLocation = (String) args[1];
            Ocl ocl = getOclByLocation(oclLocation);
            validatePluginForCspIsActive(ocl.getCloudServiceProvider().getName());
        }
    }

    private Ocl getOclByLocation(String oclLocationValue) {
        try {
            return oclLoader.getOcl(URI.create(oclLocationValue).toURL());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
