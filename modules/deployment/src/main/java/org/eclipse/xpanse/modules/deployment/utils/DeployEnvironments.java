/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.credential.CredentialCenter;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.CredentialVariable;
import org.eclipse.xpanse.modules.models.credential.CredentialVariables;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.service.deployment.exceptions.FlavorInvalidException;
import org.eclipse.xpanse.modules.models.servicetemplate.AvailabilityZoneConfig;
import org.eclipse.xpanse.modules.models.servicetemplate.DeployVariable;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceFlavor;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployVariableKind;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.SensitiveScope;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceHostingType;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.eclipse.xpanse.modules.security.secrets.SecretsManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/** Environment variables utils for deployment. */
@Slf4j
@Component
public class DeployEnvironments {

    private static final String VAR_REGION = "region";
    private static final String VAR_SERVICE_ID = "service_id";
    private static final String VAR_AGENT_VERSION = "agent_version";
    private static final String VAR_XPANSE_API_ENDPOINT = "xpanse_api_endpoint";

    private final SecretsManager secretsManager;

    private final CredentialCenter credentialCenter;

    private final PluginManager pluginManager;

    private final Environment environment;

    /**
     * Constructor to initialize DeployEnvironments bean.
     *
     * @param credentialCenter CredentialCenter bean
     * @param secretsManager SecretsManager bean
     * @param pluginManager PluginManager bean
     * @param environment Environment bean
     */
    @Autowired
    public DeployEnvironments(
            CredentialCenter credentialCenter,
            SecretsManager secretsManager,
            PluginManager pluginManager,
            Environment environment) {
        this.credentialCenter = credentialCenter;
        this.secretsManager = secretsManager;
        this.pluginManager = pluginManager;
        this.environment = environment;
    }

    /**
     * Get environment variables for deployment.
     *
     * @param task the context of the task.
     */
    private Map<String, String> getEnvFromDeployTask(DeployTask task) {
        return getEnv(
                task.getOcl().getCloudServiceProvider().getName(),
                task.getDeployRequest().getServiceRequestProperties(),
                task.getOcl().getDeployment().getVariables());
    }

    /**
     * Build environment variables for serviceRequestProperties and deployVariables.
     *
     * @param serviceRequestProperties variables passed by end user during ordering.
     * @param deployVariables deploy variables defined in the service template.
     */
    private Map<String, String> getEnv(
            Csp csp,
            Map<String, Object> serviceRequestProperties,
            List<DeployVariable> deployVariables) {
        Map<String, String> variables = new HashMap<>();
        for (DeployVariable variable : deployVariables) {
            if (variable.getKind() == DeployVariableKind.ENV) {
                if (serviceRequestProperties.containsKey(variable.getName())
                        && serviceRequestProperties.get(variable.getName()) != null) {
                    variables.put(
                            variable.getName(),
                            !SensitiveScope.NONE
                                            .toValue()
                                            .equals(variable.getSensitiveScope().toValue())
                                    ? secretsManager.decrypt(
                                            serviceRequestProperties
                                                    .get(variable.getName())
                                                    .toString())
                                    : serviceRequestProperties.get(variable.getName()).toString());
                } else {
                    variables.put(variable.getName(), System.getenv(variable.getName()));
                }
            }

            if (variable.getKind() == DeployVariableKind.ENV_ENV) {
                variables.put(variable.getName(), System.getenv(variable.getName()));
            }

            if (variable.getKind() == DeployVariableKind.FIX_ENV) {
                variables.put(
                        variable.getName(),
                        !SensitiveScope.NONE
                                        .toValue()
                                        .equals(variable.getSensitiveScope().toValue())
                                ? secretsManager.decrypt(variable.getValue())
                                : variable.getValue());
            }
        }
        Map<String, String> envVarKeysMappingMap =
                pluginManager.getOrchestratorPlugin(csp).getEnvVarKeysMappingMap();
        if (!CollectionUtils.isEmpty(envVarKeysMappingMap)) {
            envVarKeysMappingMap.forEach(
                    (key, value) -> variables.put(key, environment.getProperty(value)));
        }
        return variables;
    }

    /**
     * Get flavor variables.
     *
     * @param task the DeployTask.
     */
    private Map<String, String> getFlavorVariables(DeployTask task) {
        return getFlavorVariables(task.getOcl(), task.getDeployRequest().getFlavor());
    }

    private Map<String, String> getFlavorVariables(Ocl ocl, String requestedFlavor) {
        for (ServiceFlavor flavor : ocl.getFlavors().getServiceFlavors()) {
            if (flavor.getName().equals(requestedFlavor)) {
                return flavor.getProperties();
            }
        }
        throw new FlavorInvalidException("Can not get an available flavor.");
    }

    /**
     * Get availability zone variables.
     *
     * @param task the DeployTask.
     */
    private Map<String, String> getAvailabilityZoneVariables(DeployTask task) {
        Map<String, String> variables = new HashMap<>();
        List<AvailabilityZoneConfig> availabilityZoneConfigs =
                task.getOcl().getDeployment().getServiceAvailabilityConfig();

        Map<String, String> inputAvailabilityZones = task.getDeployRequest().getAvailabilityZones();

        if (!CollectionUtils.isEmpty(availabilityZoneConfigs)
                && !CollectionUtils.isEmpty(inputAvailabilityZones)) {
            for (AvailabilityZoneConfig config : availabilityZoneConfigs) {
                if (inputAvailabilityZones.containsKey(config.getVarName())) {
                    variables.put(
                            config.getVarName(), inputAvailabilityZones.get(config.getVarName()));
                }
            }
        }
        return variables;
    }

    /**
     * Get availability zone variables.
     *
     * @param task the DeployTask.
     */
    private Map<String, String> getAgentInstallationVariables(DeployTask task) {
        Map<String, String> variables = new HashMap<>();
        if (Objects.nonNull(task.getOcl().getServiceConfigurationManage())) {
            variables.put(VAR_SERVICE_ID, task.getServiceId().toString());
            variables.put(
                    VAR_AGENT_VERSION,
                    task.getOcl().getServiceConfigurationManage().getAgentVersion());
            variables.put(
                    VAR_XPANSE_API_ENDPOINT, this.environment.getProperty("agent.api.end.point"));
        }
        return variables;
    }

    /**
     * Get deployment variables.
     *
     * @param task the DeployTask.
     */
    private Map<String, Object> getVariablesFromDeployTask(
            DeployTask task, boolean isDeployRequest) {
        Map<String, Object> variables =
                getVariables(
                        task.getDeployRequest().getServiceRequestProperties(),
                        task.getOcl().getDeployment().getVariables(),
                        isDeployRequest);
        variables.put(VAR_REGION, task.getDeployRequest().getRegion().getName());
        return variables;
    }

    /**
     * Get deployment variables.
     *
     * @param serviceRequestProperties variables provided by the end user.
     * @param deployVariables variables configured in the service template.
     * @param isDeployRequest defines if the variables are required for deploying the service. False
     *     if it is for any other use cases.
     */
    private Map<String, Object> getVariables(
            Map<String, Object> serviceRequestProperties,
            List<DeployVariable> deployVariables,
            boolean isDeployRequest) {
        Map<String, Object> variables = new HashMap<>();
        if (!CollectionUtils.isEmpty(serviceRequestProperties)
                && !CollectionUtils.isEmpty(deployVariables)) {
            for (DeployVariable variable : deployVariables) {
                if (variable.getKind() == DeployVariableKind.VARIABLE) {
                    if (serviceRequestProperties.containsKey(variable.getName())
                            && serviceRequestProperties.get(variable.getName()) != null) {
                        variables.put(
                                variable.getName(),
                                (variable.getSensitiveScope() != SensitiveScope.NONE
                                                && isDeployRequest)
                                        ? secretsManager.decodeBackToOriginalType(
                                                variable.getDataType(),
                                                serviceRequestProperties
                                                        .get(variable.getName())
                                                        .toString())
                                        : serviceRequestProperties.get(variable.getName()));
                    } else {
                        variables.put(variable.getName(), System.getenv(variable.getName()));
                    }
                }

                if (variable.getKind() == DeployVariableKind.ENV_VARIABLE) {
                    variables.put(variable.getName(), System.getenv(variable.getName()));
                }

                if (variable.getKind() == DeployVariableKind.FIX_VARIABLE) {
                    variables.put(
                            variable.getName(),
                            (variable.getSensitiveScope() != SensitiveScope.NONE && isDeployRequest)
                                    ? secretsManager.decrypt(variable.getValue())
                                    : variable.getValue());
                }
            }
        }
        return variables;
    }

    /**
     * Get credential variables by deploy task.
     *
     * @param task deployment task.
     */
    private Map<String, String> getCredentialVariables(DeployTask task) {
        ServiceHostingType serviceHostingType = task.getDeployRequest().getServiceHostingType();
        CredentialType credentialType = task.getOcl().getDeployment().getCredentialType();
        String site = task.getDeployRequest().getRegion().getSite();
        Csp csp = task.getDeployRequest().getCsp();
        String userId =
                serviceHostingType == ServiceHostingType.SELF
                        ? task.getUserId()
                        : task.getServiceVendor();
        Map<String, String> variables = new HashMap<>();
        AbstractCredentialInfo abstractCredentialInfo =
                this.credentialCenter.getCredential(csp, site, credentialType, userId);
        if (Objects.nonNull(abstractCredentialInfo)) {
            for (CredentialVariable variable :
                    ((CredentialVariables) abstractCredentialInfo).getVariables()) {
                variables.put(variable.getName(), variable.getValue());
            }
        }
        return variables;
    }

    /**
     * Get plugin's mandatory variables.
     *
     * @param csp CSP for which the mandatory variables defined in its plugins must be returned.
     */
    private Map<String, String> getPluginMandatoryVariables(Csp csp) {
        Map<String, String> variables = new HashMap<>();
        this.pluginManager
                .getOrchestratorPlugin(csp)
                .requiredProperties()
                .forEach(
                        variable ->
                                variables.put(
                                        variable, this.environment.getRequiredProperty(variable)));
        return variables;
    }

    /**
     * Get all variables that are considered for a service.
     *
     * @param serviceRequestProperties variables provided by the end user.
     * @param deployVariables variables configured in the service template.
     * @param requestedFlavor Flavor of the service ordered.
     * @param ocl OCL of the requested service template.
     */
    public Map<String, Object> getAllDeploymentVariablesForService(
            Map<String, Object> serviceRequestProperties,
            List<DeployVariable> deployVariables,
            String requestedFlavor,
            Ocl ocl) {
        Csp csp = ocl.getCloudServiceProvider().getName();
        Map<String, Object> allVariables = new HashMap<>();
        allVariables.putAll(getVariables(serviceRequestProperties, deployVariables, false));
        allVariables.putAll(getEnv(csp, serviceRequestProperties, deployVariables));
        allVariables.putAll(getFlavorVariables(ocl, requestedFlavor));
        return allVariables;
    }

    /** Builds a map of all environment variables that must be passed to the deployer. */
    public Map<String, String> getEnvironmentVariables(DeployTask deployTask) {
        Map<String, String> envVariables = new HashMap<>();
        envVariables.putAll(getEnvFromDeployTask(deployTask));
        envVariables.putAll(getCredentialVariables(deployTask));
        envVariables.putAll(getPluginMandatoryVariables(deployTask.getDeployRequest().getCsp()));
        return envVariables;
    }

    /** Builds a map of all variables that must be passed to the deployer. */
    public Map<String, Object> getInputVariables(DeployTask deployTask, boolean isDeployRequest) {
        Map<String, Object> inputVariables = new HashMap<>();
        inputVariables.putAll(getVariablesFromDeployTask(deployTask, isDeployRequest));
        inputVariables.putAll(getFlavorVariables(deployTask));
        inputVariables.putAll(getAvailabilityZoneVariables(deployTask));
        inputVariables.putAll(getAgentInstallationVariables(deployTask));
        return inputVariables;
    }

    /** Returns the map of fixed variables in a service template. */
    public Map<String, Object> getFixedVariablesFromTemplate(
            ServiceTemplateEntity serviceTemplateEntity) {
        Map<String, Object> fixedVariables = new HashMap<>();
        for (DeployVariable variable :
                serviceTemplateEntity.getOcl().getDeployment().getVariables()) {
            if (variable.getKind() == DeployVariableKind.FIX_VARIABLE) {
                fixedVariables.put(
                        variable.getName(),
                        (variable.getSensitiveScope() != SensitiveScope.NONE)
                                ? secretsManager.decrypt(variable.getValue())
                                : variable.getValue());
            }
            if (variable.getKind() == DeployVariableKind.FIX_ENV) {
                fixedVariables.put(
                        variable.getName(),
                        (variable.getSensitiveScope() != SensitiveScope.NONE)
                                ? secretsManager.decrypt(variable.getValue())
                                : variable.getValue());
            }
        }
        return fixedVariables;
    }
}
