/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.eclipse.xpanse.modules.credential.CredentialCenter;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.CredentialVariable;
import org.eclipse.xpanse.modules.models.credential.CredentialVariables;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.FlavorInvalidException;
import org.eclipse.xpanse.modules.models.servicetemplate.DeployVariable;
import org.eclipse.xpanse.modules.models.servicetemplate.Flavor;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployVariableKind;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.SensitiveScope;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.eclipse.xpanse.modules.security.common.AesUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Environment variables utils for deployment.
 */
@Component
public class DeployEnvironments {

    private final AesUtil aesUtil;

    private final CredentialCenter credentialCenter;

    private final PluginManager pluginManager;

    private final Environment environment;

    /**
     * Constructor to initialize DeployEnvironments bean.
     *
     * @param credentialCenter CredentialCenter bean
     * @param aesUtil          AesUtil bean
     * @param pluginManager    PluginManager bean
     * @param environment      Environment bean
     */

    @Autowired
    public DeployEnvironments(CredentialCenter credentialCenter, AesUtil aesUtil,
                              PluginManager pluginManager, Environment environment) {
        this.credentialCenter = credentialCenter;
        this.aesUtil = aesUtil;
        this.pluginManager = pluginManager;
        this.environment = environment;
    }

    /**
     * Get environment variables for deployment.
     *
     * @param task the context of the task.
     */
    public Map<String, String> getEnv(DeployTask task) {
        Map<String, String> variables = new HashMap<>();
        Map<String, Object> request = task.getDeployRequest().getServiceRequestProperties();
        for (DeployVariable variable : task.getOcl().getDeployment().getVariables()) {
            if (variable.getKind() == DeployVariableKind.ENV) {
                if (request.containsKey(variable.getName())
                        && request.get(variable.getName()) != null) {
                    variables.put(variable.getName(),
                            !SensitiveScope.NONE.toValue()
                                    .equals(variable.getSensitiveScope().toValue())
                                    ? aesUtil.decode(request.get(variable.getName()).toString())
                                    : request.get(variable.getName()).toString());
                } else {
                    variables.put(variable.getName(), System.getenv(variable.getName()));
                }
            }

            if (variable.getKind() == DeployVariableKind.ENV_ENV) {
                variables.put(variable.getName(), System.getenv(variable.getName()));
            }

            if (variable.getKind() == DeployVariableKind.FIX_ENV) {
                variables.put(variable.getName(),
                        !SensitiveScope.NONE.toValue()
                                .equals(variable.getSensitiveScope().toValue())
                                ? aesUtil.decode(variable.getValue()) : variable.getValue());
            }
        }

        return variables;
    }

    /**
     * Get flavor variables.
     *
     * @param task the DeployTask.
     */
    public Map<String, String> getFlavorVariables(DeployTask task) {
        for (Flavor flavor : task.getOcl().getFlavors()) {
            if (flavor.getName().equals(task.getDeployRequest().getFlavor())) {
                return flavor.getProperties();
            }
        }
        throw new FlavorInvalidException("Can not get an available flavor.");
    }

    /**
     * Get deployment variables.
     *
     * @param task the DeployTask.
     */
    public Map<String, Object> getVariables(DeployTask task, boolean isDeployRequest) {
        Map<String, Object> variables = new HashMap<>();
        Map<String, Object> request = task.getDeployRequest().getServiceRequestProperties();
        for (DeployVariable variable : task.getOcl().getDeployment().getVariables()) {
            if (variable.getKind() == DeployVariableKind.VARIABLE) {
                if (request.containsKey(variable.getName())
                        && request.get(variable.getName()) != null) {
                    variables.put(variable.getName(),
                            (variable.getSensitiveScope() != SensitiveScope.NONE
                                    && isDeployRequest)
                                    ? aesUtil.decodeBackToOriginalType(variable.getDataType(),
                                    request.get(variable.getName()).toString())
                                    : request.get(variable.getName()));
                } else {
                    variables.put(variable.getName(), System.getenv(variable.getName()));
                }
            }

            if (variable.getKind() == DeployVariableKind.ENV_VARIABLE) {
                variables.put(variable.getName(), System.getenv(variable.getName()));
            }

            if (variable.getKind() == DeployVariableKind.FIX_VARIABLE) {
                variables.put(variable.getName(),
                        (variable.getSensitiveScope() != SensitiveScope.NONE && isDeployRequest)
                                ? aesUtil.decode(variable.getValue()) : variable.getValue());
            }
        }

        return variables;
    }

    /**
     * Get credential variables.
     *
     * @param task the DeployTask.
     */
    public Map<String, String> getCredentialVariables(DeployTask task) {
        Map<String, String> variables = new HashMap<>();
        CredentialType credentialType = task.getOcl().getDeployment().getCredentialType();
        Csp csp = task.getOcl().getCloudServiceProvider().getName();

        AbstractCredentialInfo abstractCredentialInfo =
                this.credentialCenter.getCredential(csp, credentialType,
                        task.getDeployRequest().getUserId());
        if (Objects.nonNull(abstractCredentialInfo)) {
            for (CredentialVariable variable
                    : ((CredentialVariables) abstractCredentialInfo).getVariables()) {
                variables.put(variable.getName(), variable.getValue());
            }
        }
        return variables;
    }

    /**
     * Get plugin's mandatory variables.
     *
     * @param task the DeployTask.
     */
    public Map<String, String> getPluginMandatoryVariables(DeployTask task) {
        Map<String, String> variables = new HashMap<>();
        this.pluginManager.getOrchestratorPlugin(task.getDeployRequest().getCsp())
                .requiredProperties().forEach(variable -> variables.put(variable,
                        this.environment.getRequiredProperty(variable)));
        return variables;
    }
}
