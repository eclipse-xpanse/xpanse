/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.credential;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.CreateCredential;
import org.eclipse.xpanse.modules.models.credential.CredentialVariable;
import org.eclipse.xpanse.modules.models.credential.CredentialVariables;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.credential.exceptions.CredentialCapabilityNotFound;
import org.eclipse.xpanse.modules.models.credential.exceptions.CredentialVariablesNotComplete;
import org.eclipse.xpanse.modules.models.credential.exceptions.CredentialsNotFoundException;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.orchestrator.OrchestratorPlugin;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * The credential center.
 */
@Component
public class CredentialCenter {

    private final PluginManager pluginManager;
    private final CredentialsStore credentialsStore;
    private final Pbkdf2PasswordEncoder passwordEncoder;
    private final CredentialOpenApiGenerator credentialOpenApiGenerator;

    /**
     * Constructor of CredentialCenter.
     */
    @Autowired
    public CredentialCenter(
            PluginManager pluginManager,
            CredentialsStore credentialsStore,
            Pbkdf2PasswordEncoder passwordEncoder,
            CredentialOpenApiGenerator credentialOpenApiGenerator) {
        this.pluginManager = pluginManager;
        this.credentialsStore = credentialsStore;
        this.passwordEncoder = passwordEncoder;
        this.credentialOpenApiGenerator = credentialOpenApiGenerator;
    }

    /**
     * List the available credential types by @Csp.
     *
     * @param csp The cloud service provider.
     * @return Returns list of credential types.
     */
    public List<CredentialType> getAvailableCredentialTypesByCsp(Csp csp) {
        OrchestratorPlugin plugin = pluginManager.getOrchestratorPlugin(csp);
        return plugin.getAvailableCredentialTypes();
    }

    /**
     * List the credential capabilities by @Csp and @type.
     *
     * @param csp  The cloud service provider.
     * @param type The type of credential.
     * @return Returns list of credential capabilities.
     */
    public List<AbstractCredentialInfo> getCredentialCapabilitiesByCsp(Csp csp,
                                                                       CredentialType type) {
        OrchestratorPlugin plugin = pluginManager.getOrchestratorPlugin(csp);
        List<AbstractCredentialInfo> abstractCredentialInfoList;
        if (Objects.isNull(type)) {
            abstractCredentialInfoList = plugin.getCredentialDefinitions();
        } else {
            abstractCredentialInfoList =
                    plugin.getCredentialDefinitions().stream().filter(credential ->
                                    Objects.equals(credential.getType(), type))
                            .collect(Collectors.toList());
        }

        return abstractCredentialInfoList;
    }

    /**
     * Get credential openApi Url.
     *
     * @param csp  The cloud service provider.
     * @param type The type of credential.
     * @return Returns credential openApi Url.
     */
    public String getCredentialOpenApiUrl(Csp csp, CredentialType type) {
        return credentialOpenApiGenerator.getCredentialOpenApiUrl(csp, type);
    }

    /**
     * List the credential info of the @xpanseUser.
     *
     * @param xpanseUser The user who provided the credential info.
     * @return the credentials.
     */
    public List<AbstractCredentialInfo> getCredentialsByUser(String xpanseUser) {
        if (StringUtils.isBlank(xpanseUser)) {
            return Collections.emptyList();
        }
        List<AbstractCredentialInfo> abstractCredentialInfos = new ArrayList<>();
        for (Csp csp : pluginManager.getPluginsMap().keySet()) {
            pluginManager.getOrchestratorPlugin(csp).getCredentialDefinitions().forEach(
                    credentialInfo -> {
                        List<AbstractCredentialInfo> credentials =
                                getCredentials(csp, xpanseUser, credentialInfo.getType());
                        abstractCredentialInfos.addAll(credentials);
                    }
            );
        }
        return abstractCredentialInfos;
    }

    /**
     * List the credential info of the @csp and @xpanseUser and @type. This method is called from
     * REST API and hence all sensitive fields are masked in the response.
     *
     * @param csp                     The cloud service provider.
     * @param xpanseUser              The user who provided the credential info.
     * @param requestedCredentialType The type of the credential.
     * @return the credentials.
     */
    public List<AbstractCredentialInfo> getCredentials(
            Csp csp, String xpanseUser, CredentialType requestedCredentialType) {
        if (StringUtils.isBlank(xpanseUser)) {
            return Collections.emptyList();
        }
        List<AbstractCredentialInfo> abstractCredentialInfos = new ArrayList<>();
        if (Objects.nonNull(requestedCredentialType)) {
            AbstractCredentialInfo abstractCredentialInfo = credentialsStore.getCredential(
                    csp, requestedCredentialType, xpanseUser);
            if (Objects.nonNull(abstractCredentialInfo)) {
                abstractCredentialInfos.add(abstractCredentialInfo);
            }
        } else {
            pluginManager.getOrchestratorPlugin(csp).getCredentialDefinitions().forEach(
                    credentialInfo -> {
                        AbstractCredentialInfo abstractCredentialInfo =
                                credentialsStore.getCredential(
                                        csp, credentialInfo.getType(), xpanseUser);
                        if (Objects.nonNull(abstractCredentialInfo)) {
                            abstractCredentialInfos.add(abstractCredentialInfo);
                        }
                    });
        }
        return maskSensitiveValues(abstractCredentialInfos);
    }

    /**
     * Add credential info for the csp.
     *
     * @param createCredential The credential for the service.
     */
    public void addCredential(CreateCredential createCredential) {
        checkInputCredentialIsValid(createCredential);
        encodeSensitiveVariables(createCredential);
        AbstractCredentialInfo credential;
        OrchestratorPlugin orchestratorPlugin =
                pluginManager.getOrchestratorPlugin(createCredential.getCsp());
        if (orchestratorPlugin.getAvailableCredentialTypes().contains(createCredential.getType())) {
            credential = new CredentialVariables(createCredential);
            createCredential(credential);
        } else {
            throw new CredentialCapabilityNotFound(
                    String.format("Not supported credential type Csp:%s, Type: %s.",
                            createCredential.getCsp(), createCredential.getType()));
        }
    }

    private void encodeSensitiveVariables(CreateCredential createCredential) {
        List<CredentialVariable> variables = createCredential.getVariables();
        variables.stream().forEach(variable -> {
            if (!Objects.isNull(variable) && variable.getIsSensitive()) {
                variable.setValue(passwordEncoder.encode(variable.getValue()));
            }
        });
    }

    /**
     * Update credential info for the service.
     *
     * @param updateCredential The credential for the service.
     */
    public void updateCredential(CreateCredential updateCredential) {
        checkInputCredentialIsValid(updateCredential);
        encodeSensitiveVariables(updateCredential);
        AbstractCredentialInfo credential;
        if (updateCredential.getType() == CredentialType.VARIABLES) {
            credential = new CredentialVariables(updateCredential);
        } else {
            throw new CredentialCapabilityNotFound(
                    String.format("Not supported credential type Csp:%s, Type: %s.",
                            updateCredential.getCsp(), updateCredential.getType()));
        }
        deleteCredentialByType(updateCredential.getCsp(), updateCredential.getXpanseUser(),
                updateCredential.getType());
        createCredential(credential);
    }

    /**
     * Delete credential for the @Csp with @xpanseUser.
     *
     * @param csp        The cloud service provider.
     * @param xpanseUser The user who provided the credential info.
     */
    public void deleteCredential(Csp csp, String xpanseUser, CredentialType credentialType) {
        credentialsStore.deleteCredential(csp, credentialType, xpanseUser);
    }

    /**
     * Delete credential for the @Csp with @xpanseUser.
     *
     * @param csp        The cloud service provider.
     * @param xpanseUser The user who provided the credential info.
     */
    public void deleteCredentialByType(Csp csp, String xpanseUser, CredentialType
            credentialType) {
        credentialsStore.deleteCredential(csp, credentialType, xpanseUser);
    }

    /**
     * Get credential for the @Csp with @xpanseUser. This method is used only within Xpanse
     * application. This method joins credential variables from all sources.
     *
     * @param csp            The cloud service provider.
     * @param xpanseUser     The user who provided the credential info.
     * @param credentialType Type of the credential
     */
    public AbstractCredentialInfo getCredential(Csp csp,
                                                String xpanseUser,
                                                CredentialType credentialType) {
        List<AbstractCredentialInfo> credentialInfos = joinCredentialsFromAllSources(
                csp, credentialType, xpanseUser);
        if (credentialInfos.isEmpty()) {
            throw new CredentialsNotFoundException(
                    String.format("No credential information found for the given Csp:%s.", csp));
        }
        Optional<AbstractCredentialInfo> credentialWithAllVariables = credentialInfos.stream()
                .filter(credentialInfo -> !isAnyMandatoryCredentialVariableMissing(
                        (CredentialVariables) credentialInfo)).findFirst();
        if (credentialWithAllVariables.isEmpty()) {
            throw new CredentialVariablesNotComplete(Set.of(String.format(
                    "All mandatory variables for credential of type %s for Csp:%s and user %s is "
                            + "not available",
                    csp,
                    credentialType, xpanseUser)));
        }
        return credentialWithAllVariables.get();
    }

    /**
     * Check the input credential whether is valid.
     *
     * @param inputCredential The credential to create or update.
     */
    public void checkInputCredentialIsValid(CreateCredential inputCredential) {

        // filter defined credential abilities by csp and type.
        List<AbstractCredentialInfo> credentialAbilities =
                getCredentialCapabilitiesByCsp(inputCredential.getCsp(), inputCredential.getType());

        if (CollectionUtils.isEmpty(credentialAbilities)) {
            throw new CredentialCapabilityNotFound(
                    String.format("Defined credentials with type %s provided by csp %s not found.",
                            inputCredential.getType(), inputCredential.getCsp()));
        }
        // filter defined credential abilities by name.
        List<AbstractCredentialInfo> sameNameAbilities = credentialAbilities.stream().filter(
                credentialAbility -> StringUtils.equals(credentialAbility.getName(),
                        inputCredential.getName())).toList();
        if (CollectionUtils.isEmpty(sameNameAbilities)) {
            throw new CredentialCapabilityNotFound(
                    String.format("Defined credentials with type %s and name %s "
                                    + "provided by csp %s not found.",
                            inputCredential.getType(), inputCredential.getName(),
                            inputCredential.getCsp()));
        }
        // check all fields in the input credential are valid based on the defined credentials.
        for (AbstractCredentialInfo credentialAbility : credentialAbilities) {
            if (CredentialType.VARIABLES.equals(credentialAbility.getType())) {
                Set<String> errorReasons = new HashSet<>();
                CredentialVariables inputVariables = new CredentialVariables(inputCredential);
                CredentialVariables definedVariables = (CredentialVariables) credentialAbility;
                Set<String> definedVariableNameSet =
                        definedVariables.getVariables().stream()
                                .map(CredentialVariable::getName).collect(Collectors.toSet());
                Set<String> inputVariableNameSet = inputVariables.getVariables().stream()
                        .map(CredentialVariable::getName).collect(Collectors.toSet());
                if (!inputVariableNameSet.containsAll(definedVariableNameSet)) {
                    definedVariableNameSet.removeAll(inputVariableNameSet);
                    errorReasons.add(String.format("Missing variables with names %s.",
                            definedVariableNameSet));
                }
                Set<String> definedMandatoryVariableNameSet =
                        definedVariables.getVariables().stream()
                                .filter(CredentialVariable::getIsMandatory)
                                .map(CredentialVariable::getName).collect(Collectors.toSet());
                for (CredentialVariable inputVariable : inputCredential.getVariables()) {
                    if (definedMandatoryVariableNameSet.contains(inputVariable.getName())
                            && StringUtils.isBlank(inputVariable.getValue())) {
                        errorReasons.add(
                                String.format("The value of mandatory variable with name %s"
                                        + " could not be empty.", inputVariable.getName()));
                    }
                }
                if (!errorReasons.isEmpty()) {
                    throw new CredentialVariablesNotComplete(errorReasons);
                }
                return;
            }
        }
    }


    /**
     * Create credential for the @Csp with @xpanseUser.
     *
     * @param abstractCredentialInfo Instance of any class that implements AbstractCredentialInfo
     */

    public void createCredential(AbstractCredentialInfo abstractCredentialInfo) {
        credentialsStore.storeCredential(abstractCredentialInfo);
    }

    /**
     * Get credentialInfo from the environment using @Csp.
     *
     * @param csp The cloud service provider.
     * @return Returns credentialInfo.
     */
    private AbstractCredentialInfo getCompleteCredentialDefinitionFromEnv(Csp csp) {
        List<AbstractCredentialInfo> credentialAbilities =
                getCredentialCapabilitiesByCsp(csp, null);
        if (CollectionUtils.isEmpty(credentialAbilities)) {
            return null;
        }
        for (AbstractCredentialInfo credentialAbility : credentialAbilities) {
            if (CredentialType.VARIABLES == credentialAbility.getType()) {
                CredentialVariables credentialVariables = (CredentialVariables) credentialAbility;
                List<CredentialVariable> variables = credentialVariables.getVariables();
                for (CredentialVariable variable : variables) {
                    String envValue = System.getenv(variable.getName());
                    if (StringUtils.isNotBlank(envValue)) {
                        variable.setValue(envValue);
                    }
                }
                // Check if all variables have been successfully set.
                if (!isAnyMandatoryCredentialVariableMissing(credentialVariables)) {
                    return credentialAbility;
                }
            }
        }
        return null;
    }

    private boolean isAnyMandatoryCredentialVariableMissing(
            CredentialVariables credentialVariables) {
        return credentialVariables.getVariables().stream()
                .anyMatch(credentialVariable -> credentialVariable.getIsMandatory()
                        && Objects.isNull(credentialVariable.getValue()));
    }

    private void checkNullParamAndFillValueFromEnv(AbstractCredentialInfo abstractCredentialInfo) {
        if (abstractCredentialInfo.getType().equals(CredentialType.VARIABLES)) {
            CredentialVariables credentialVariables =
                    (CredentialVariables) abstractCredentialInfo;
            for (CredentialVariable variable : credentialVariables.getVariables()) {
                if (Objects.isNull(variable.getValue())
                        || Objects.equals(variable.getValue(), "null")) {
                    variable.setValue(System.getenv(variable.getName()));
                }
            }
        }
    }

    private void addCredentialInfoFromEnv(Csp csp,
                                          List<AbstractCredentialInfo> abstractCredentialInfos) {
        AbstractCredentialInfo credentialInfoFromEnv = getCompleteCredentialDefinitionFromEnv(csp);
        if (Objects.nonNull(credentialInfoFromEnv)) {
            abstractCredentialInfos.add(credentialInfoFromEnv);
        }
    }

    private List<AbstractCredentialInfo> maskSensitiveValues(
            List<AbstractCredentialInfo> abstractCredentialInfos) {
        List<AbstractCredentialInfo> maskedCredentialInfos = new ArrayList<>();
        for (AbstractCredentialInfo abstractCredentialInfo : abstractCredentialInfos) {
            CredentialVariables credentialVariables =
                    (CredentialVariables) abstractCredentialInfo;
            List<CredentialVariable> maskedCredentialVariableList = new ArrayList<>();
            for (CredentialVariable variable : credentialVariables.getVariables()) {
                CredentialVariable maskedCredentialVariable =
                        new CredentialVariable(variable.getName(), variable.getDescription(),
                                variable.getIsMandatory(), variable.getIsSensitive(),
                                variable.getValue());
                if (maskedCredentialVariable.getIsSensitive()) {
                    maskedCredentialVariable.setValue("*********");
                }
                maskedCredentialVariableList.add(maskedCredentialVariable);
            }
            CredentialVariables maskedCredentialVariables =
                    new CredentialVariables(credentialVariables.getCsp(),
                            credentialVariables.getXpanseUser(), credentialVariables.getName(),
                            credentialVariables.getDescription(), credentialVariables.getType(),
                            maskedCredentialVariableList);
            maskedCredentialInfos.add(maskedCredentialVariables);
        }
        return maskedCredentialInfos;
    }

    private List<AbstractCredentialInfo> joinCredentialsFromAllSources(
            Csp csp, CredentialType requestedCredentialType, String xpanseUser) {
        List<AbstractCredentialInfo> abstractCredentialInfos = new ArrayList<>();
        if (Objects.nonNull(requestedCredentialType)) {
            AbstractCredentialInfo abstractCredentialInfo =
                    this.credentialsStore.getCredential(csp, requestedCredentialType, xpanseUser);
            if (Objects.nonNull(abstractCredentialInfo)) {
                checkNullParamAndFillValueFromEnv(abstractCredentialInfo);
                abstractCredentialInfos.add(abstractCredentialInfo);
            } else {
                addCredentialInfoFromEnv(csp, abstractCredentialInfos);
            }
        } else {
            for (CredentialType credentialType : CredentialType.values()) {
                AbstractCredentialInfo abstractCredentialInfo =
                        this.credentialsStore.getCredential(csp, credentialType, xpanseUser);
                if (Objects.nonNull(abstractCredentialInfo)) {
                    checkNullParamAndFillValueFromEnv(abstractCredentialInfo);
                    abstractCredentialInfos.add(abstractCredentialInfo);
                }
            }
            if (CollectionUtils.isEmpty(abstractCredentialInfos)) {
                addCredentialInfoFromEnv(csp, abstractCredentialInfos);
            }
        }
        return abstractCredentialInfos;
    }
}
