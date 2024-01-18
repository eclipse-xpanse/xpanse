/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment;

import jakarta.annotation.Resource;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.DeployerNotFoundException;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.eclipse.xpanse.modules.orchestrator.deployment.Deployer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * Bean to wrap logic which decide which Deployment bean must be used for the service.
 */
@Component
public class DeployerKindManager implements
        ApplicationListener<ContextRefreshedEvent> {
    private final Map<DeployerKind, Deployer> deploymentMap = new ConcurrentHashMap<>();

    @Resource
    private ApplicationContext applicationContext;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        applicationContext.getBeansOfType(Deployer.class)
                .forEach((key, value) -> deploymentMap.put(value.getDeployerKind(), value));
    }

    /**
     * Get Deployment bean available for the requested DeployerKind.
     *
     * @param deployerKind kind of deployer.
     * @return Deployment bean for the provided deployerKind.
     */
    public Deployer getDeployment(DeployerKind deployerKind) {
        Deployer deployment = deploymentMap.get(deployerKind);
        if (Objects.isNull(deployment)) {
            throw new DeployerNotFoundException("Can't find suitable deployer for the Task.");
        }
        return deployment;
    }
}
