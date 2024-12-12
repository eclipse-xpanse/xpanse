/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.credential;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.cache.credential.CredentialCacheKey;
import org.eclipse.xpanse.modules.cache.credential.CredentialsStore;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.CreateCredential;
import org.eclipse.xpanse.modules.models.credential.CredentialVariable;
import org.eclipse.xpanse.modules.models.credential.CredentialVariables;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialTypeMessage;
import org.eclipse.xpanse.modules.models.credential.exceptions.CredentialCapabilityNotFound;
import org.eclipse.xpanse.modules.models.credential.exceptions.CredentialVariablesNotComplete;
import org.eclipse.xpanse.modules.models.credential.exceptions.CredentialsNotFoundException;
import org.eclipse.xpanse.modules.orchestrator.OrchestratorPlugin;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.eclipse.xpanse.modules.security.common.AesUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/** The credential center. */
@Slf4j
@Component
public class CredentialCenter {

    private final AesUtil aesUtil;
    private final PluginManager pluginManager;
    private final CredentialsStore credentialsStore;
    private final CredentialOpenApiGenerator credentialOpenApiGenerator;

    /** Constructor of CredentialCenter. */
    @Autowired
    public CredentialCenter(
            AesUtil aesUtil,
            PluginManager pluginManager,
            CredentialsStore credentialsStore,
            CredentialOpenApiGenerator credentialOpenApiGenerator) {
        this.aesUtil = aesUtil;
        this.pluginManager = pluginManager;
        this.credentialsStore = credentialsStore;
        this.credentialOpenApiGenerator = credentialOpenApiGenerator;
    }

    /**
     * List the available credential types by @Csp.
     *
     * @param csp The cloud service provider.
     * @return Returns list of credential types.
     */
    public List<CredentialType> listAvailableCredentialTypesByCsp(Csp csp) {
        OrchestratorPlugin plugin = pluginManager.getOrchestratorPlugin(csp);
        return plugin.getAvailableCredentialTypes();
    }

    /**
     * List the credential capabilities by @Csp and @type.
     *
     * @param csp The cloud service provider.
     * @param type The type of credential.
     * @param name The name of credential.
     * @return Returns list of credential capabilities.
     */
    public List<AbstractCredentialInfo> listCredentialCapabilities(
            Csp csp, CredentialType type, String name) {
        OrchestratorPlugin plugin = pluginManager.getOrchestratorPlugin(csp);
        List<AbstractCredentialInfo> abstractCredentialInfoList = plugin.getCredentialDefinitions();
        if (!CollectionUtils.isEmpty(abstractCredentialInfoList)) {
            if (Objects.nonNull(type)) {
                abstractCredentialInfoList =
                        abstractCredentialInfoList.stream()
                                .filter(
                                        abstractCredentialInfo ->
                                                Objects.equals(
                                                        type, abstractCredentialInfo.getType()))
                                .toList();
            }
            if (StringUtils.isNotBlank(name)) {
                abstractCredentialInfoList =
                        abstractCredentialInfoList.stream()
                                .filter(
                                        abstractCredentialInfo ->
                                                StringUtils.equals(
                                                        name, abstractCredentialInfo.getName()))
                                .toList();
            }
        }
        return abstractCredentialInfoList;
    }

    /**
     * Get credential openApi Url.
     *
     * @param csp The cloud service provider.
     * @param type The type of credential.
     * @return Returns credential openApi Url.
     */
    public String getCredentialOpenApiUrl(Csp csp, CredentialType type) {
        return credentialOpenApiGenerator.getCredentialOpenApiUrl(csp, type);
    }

    /**
     * List the credential info of the @csp and @type and @userId. This method is called from REST
     * API and hence all sensitive fields are masked in the response.
     *
     * @param csp The cloud service provider.
     * @param type The type of the credential.
     * @param userId The id of user who provided the credential info.
     * @return the credentials.
     */
    public List<AbstractCredentialInfo> listCredentials(
            Csp csp, CredentialType type, String userId) {
        List<AbstractCredentialInfo> abstractCredentialInfos = new ArrayList<>();
        if (Objects.isNull(csp)) {
            for (Csp key : pluginManager.getPluginsMap().keySet()) {
                pluginManager
                        .getOrchestratorPlugin(key)
                        .getSites()
                        .forEach(
                                site -> {
                                    abstractCredentialInfos.addAll(
                                            listUserCredentials(key, site, type, userId));
                                });
            }
        } else {
            pluginManager
                    .getOrchestratorPlugin(csp)
                    .getSites()
                    .forEach(
                            site -> {
                                abstractCredentialInfos.addAll(
                                        listUserCredentials(csp, site, type, userId));
                            });
        }
        return maskSensitiveValues(abstractCredentialInfos);
    }

    private List<AbstractCredentialInfo> listUserCredentials(
            Csp csp, String site, CredentialType type, String userId) {
        List<AbstractCredentialInfo> userCredentials = new ArrayList<>();
        List<AbstractCredentialInfo> definedCredentialInfos =
                pluginManager.getOrchestratorPlugin(csp).getCredentialDefinitions();
        if (!CollectionUtils.isEmpty(definedCredentialInfos)) {
            if (Objects.nonNull(type)) {
                definedCredentialInfos =
                        definedCredentialInfos.stream()
                                .filter(
                                        abstractCredentialInfo ->
                                                Objects.equals(
                                                        type, abstractCredentialInfo.getType()))
                                .toList();
            }
            definedCredentialInfos.forEach(
                    credential -> {
                        AbstractCredentialInfo credentialInfo =
                                getCredentialFromCache(
                                        csp,
                                        site,
                                        credential.getType(),
                                        credential.getName(),
                                        userId);
                        if (Objects.nonNull(credentialInfo)) {
                            userCredentials.add(credentialInfo);
                        }
                    });
        }
        return userCredentials;
    }

    private AbstractCredentialInfo getCredentialFromCache(
            Csp csp, String site, CredentialType type, String credentialName, String userId) {
        CredentialCacheKey cacheKey =
                new CredentialCacheKey(csp, site, type, credentialName, userId);
        AbstractCredentialInfo credentialInfo = null;
        try {
            credentialInfo = credentialsStore.getCredential(cacheKey);
            credentialsStore.updateCredentialCacheTimeToLive(cacheKey);
        } catch (Exception e) {
            log.error(
                    "Get credential from cache by key {} failed. error:{}",
                    cacheKey,
                    e.getMessage());
        }
        return credentialInfo;
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
        } else {
            throw new CredentialCapabilityNotFound(
                    String.format(
                            "Not supported credential type Csp:%s, Type: %s.",
                            createCredential.getCsp(), createCredential.getType()));
        }
        CredentialCacheKey cacheKey =
                new CredentialCacheKey(
                        credential.getCsp(),
                        credential.getSite(),
                        credential.getType(),
                        credential.getName(),
                        credential.getUserId());
        credentialsStore.storeCredential(cacheKey, credential);
        credentialsStore.updateCredentialCacheTimeToLive(cacheKey);
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
                    String.format(
                            "Not supported credential type Csp:%s, Type: %s.",
                            updateCredential.getCsp(), updateCredential.getType()));
        }
        CredentialCacheKey cacheKey =
                new CredentialCacheKey(
                        credential.getCsp(),
                        credential.getSite(),
                        credential.getType(),
                        credential.getName(),
                        credential.getUserId());
        credentialsStore.storeCredential(cacheKey, credential);
        credentialsStore.updateCredentialCacheTimeToLive(cacheKey);
    }

    /**
     * Delete credential info for the service.
     *
     * @param csp cloud service provider
     * @param siteName site name
     * @param credentialType credential type
     * @param credentialName credential name
     * @param userId user id
     */
    public void deleteCredential(
            Csp csp,
            String siteName,
            CredentialType credentialType,
            String credentialName,
            String userId) {
        CredentialCacheKey cacheKey =
                new CredentialCacheKey(csp, siteName, credentialType, credentialName, userId);
        credentialsStore.deleteCredential(cacheKey);
    }

    /**
     * Get credential for the @Csp with @userId. This method is used only within Xpanse application.
     * This method joins credential variables from all sources.
     *
     * @param csp The cloud service provider.
     * @param site The site which the credential belongs to.
     * @param credentialType Type of the credential
     * @param userId The user who provided the credential info.
     */
    public AbstractCredentialInfo getCredential(
            Csp csp, String site, CredentialType credentialType, String userId) {
        List<AbstractCredentialInfo> credentialInfos =
                joinCredentialsFromAllSources(csp, site, credentialType, userId);
        if (credentialInfos.isEmpty()) {
            throw new CredentialsNotFoundException(
                    String.format(
                            "No credential information found for the given Csp %s "
                                    + "and site %s.",
                            csp.toValue(), site));
        }
        Optional<AbstractCredentialInfo> credentialWithAllVariables =
                credentialInfos.stream()
                        .filter(
                                credentialInfo ->
                                        allMandatoryCredentialVariableNotBlank(
                                                (CredentialVariables) credentialInfo))
                        .findFirst();
        if (credentialWithAllVariables.isEmpty()) {
            throw new CredentialVariablesNotComplete(
                    Set.of(
                            String.format(
                                    "All mandatory variables for credential of type %s for Csp:%s"
                                            + " and user %s is not available",
                                    csp, credentialType, userId)));
        }
        return decodeSensitiveVariables(credentialWithAllVariables.get());
    }

    private void encodeSensitiveVariables(CreateCredential createCredential) {
        List<CredentialVariable> variables = createCredential.getVariables();
        variables.forEach(
                variable -> {
                    if (!Objects.isNull(variable) && variable.getIsSensitive()) {
                        if (StringUtils.isNotBlank(variable.getValue())) {
                            variable.setValue(aesUtil.encode(variable.getValue()));
                        }
                    }
                });
    }

    private CredentialVariables decodeSensitiveVariables(
            AbstractCredentialInfo abstractCredentialInfo) {
        CredentialVariables credentialVariables = (CredentialVariables) abstractCredentialInfo;
        List<CredentialVariable> variables = credentialVariables.getVariables();
        variables.forEach(
                variable -> {
                    if (!Objects.isNull(variable) && variable.getIsSensitive()) {
                        if (StringUtils.isNotBlank(variable.getValue())) {
                            variable.setValue(aesUtil.decode(variable.getValue()));
                        }
                    }
                });
        return credentialVariables;
    }

    /**
     * Check the input credential whether is valid.
     *
     * @param inputCredential The credential to create or update.
     */
    public void checkInputCredentialIsValid(CreateCredential inputCredential) {

        // filter defined credential abilities by csp,type and name.
        List<AbstractCredentialInfo> credentialAbilities =
                listCredentialCapabilities(
                        inputCredential.getCsp(),
                        inputCredential.getType(),
                        inputCredential.getName());

        if (CollectionUtils.isEmpty(credentialAbilities)) {
            throw new CredentialCapabilityNotFound(
                    String.format(
                            "Defined credentials with type %s and name %s provided by "
                                    + "csp %s not found.",
                            inputCredential.getType(),
                            inputCredential.getName(),
                            inputCredential.getCsp()));
        }
        // check the site of the input credential is valid.
        OrchestratorPlugin plugin = pluginManager.getOrchestratorPlugin(inputCredential.getCsp());
        if (!plugin.getSites().contains(inputCredential.getSite())) {
            throw new CredentialCapabilityNotFound(
                    String.format(
                            "Site %s is not supported by csp %s.",
                            inputCredential.getSite(), inputCredential.getCsp()));
        }
        // check all fields in the input credential are valid based on the defined credentials.
        for (AbstractCredentialInfo credentialAbility : credentialAbilities) {
            if (CredentialType.VARIABLES.equals(credentialAbility.getType())) {
                Set<String> errorReasons = new HashSet<>();
                CredentialVariables inputVariables = new CredentialVariables(inputCredential);
                CredentialVariables definedVariables = (CredentialVariables) credentialAbility;
                Set<String> definedVariableNameSet =
                        definedVariables.getVariables().stream()
                                .map(CredentialVariable::getName)
                                .collect(Collectors.toSet());
                Set<String> inputVariableNameSet =
                        inputVariables.getVariables().stream()
                                .map(CredentialVariable::getName)
                                .collect(Collectors.toSet());
                if (!inputVariableNameSet.containsAll(definedVariableNameSet)) {
                    definedVariableNameSet.removeAll(inputVariableNameSet);
                    errorReasons.add(
                            String.format(
                                    "Missing variables with names %s.", definedVariableNameSet));
                }
                Set<String> definedMandatoryVariableNameSet =
                        definedVariables.getVariables().stream()
                                .filter(CredentialVariable::getIsMandatory)
                                .map(CredentialVariable::getName)
                                .collect(Collectors.toSet());
                for (CredentialVariable inputVariable : inputCredential.getVariables()) {
                    if (definedMandatoryVariableNameSet.contains(inputVariable.getName())
                            && StringUtils.isBlank(inputVariable.getValue())) {
                        errorReasons.add(
                                String.format(
                                        "The value of mandatory variable with name %s"
                                                + " could not be empty.",
                                        inputVariable.getName()));
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
     * Get credentialInfo from the environment using @Csp.
     *
     * @param csp The cloud service provider.
     * @return Returns credentialInfo.
     */
    private AbstractCredentialInfo getCompleteCredentialDefinitionFromEnv(Csp csp) {
        List<AbstractCredentialInfo> credentialAbilities =
                listCredentialCapabilities(csp, null, null);
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
                if (allMandatoryCredentialVariableNotBlank(credentialVariables)) {
                    return credentialAbility;
                }
            }
        }
        return null;
    }

    private boolean allMandatoryCredentialVariableNotBlank(
            CredentialVariables credentialVariables) {
        return credentialVariables.getVariables().stream()
                .filter(CredentialVariable::getIsMandatory)
                .allMatch(
                        credentialVariable ->
                                StringUtils.isNotBlank(credentialVariable.getValue()));
    }

    private void checkNullParamAndFillValueFromEnv(AbstractCredentialInfo abstractCredentialInfo) {
        if (abstractCredentialInfo.getType().equals(CredentialType.VARIABLES)) {
            CredentialVariables credentialVariables = (CredentialVariables) abstractCredentialInfo;
            for (CredentialVariable variable : credentialVariables.getVariables()) {
                if (Objects.isNull(variable.getValue())
                        || Objects.equals(variable.getValue(), "null")) {
                    variable.setValue(System.getenv(variable.getName()));
                }
            }
        }
    }

    private void addCredentialInfoFromEnv(
            Csp csp, String site, List<AbstractCredentialInfo> abstractCredentialInfos) {
        AbstractCredentialInfo credentialInfoFromEnv = getCompleteCredentialDefinitionFromEnv(csp);
        if (Objects.nonNull(credentialInfoFromEnv)) {
            credentialInfoFromEnv.setSite(site);
            abstractCredentialInfos.add(credentialInfoFromEnv);
        }
    }

    private List<AbstractCredentialInfo> maskSensitiveValues(
            List<AbstractCredentialInfo> abstractCredentialInfos) {
        if (CollectionUtils.isEmpty(abstractCredentialInfos)) {
            return abstractCredentialInfos;
        }
        List<AbstractCredentialInfo> maskedCredentialInfos = new ArrayList<>();
        for (AbstractCredentialInfo abstractCredentialInfo : abstractCredentialInfos) {
            CredentialVariables credentialVariables = (CredentialVariables) abstractCredentialInfo;
            List<CredentialVariable> maskedCredentialVariableList = new ArrayList<>();
            for (CredentialVariable variable : credentialVariables.getVariables()) {
                CredentialVariable maskedCredentialVariable =
                        new CredentialVariable(
                                variable.getName(),
                                variable.getDescription(),
                                variable.getIsMandatory(),
                                variable.getIsSensitive(),
                                variable.getValue());
                if (maskedCredentialVariable.getIsSensitive()) {
                    maskedCredentialVariable.setValue("*********");
                }
                maskedCredentialVariableList.add(maskedCredentialVariable);
            }
            AbstractCredentialInfo maskedAbstractCredentialInfo =
                    new CredentialVariables(
                            credentialVariables.getCsp(),
                            credentialVariables.getSite(),
                            credentialVariables.getType(),
                            credentialVariables.getName(),
                            credentialVariables.getDescription(),
                            credentialVariables.getUserId(),
                            maskedCredentialVariableList);
            maskedAbstractCredentialInfo.setTimeToLive(credentialVariables.getTimeToLive());
            maskedCredentialInfos.add(maskedAbstractCredentialInfo);
        }
        return maskedCredentialInfos;
    }

    private List<AbstractCredentialInfo> joinCredentialsFromAllSources(
            Csp csp, String site, CredentialType requestedCredentialType, String userId) {
        List<AbstractCredentialInfo> joinCredentials = new ArrayList<>();
        List<AbstractCredentialInfo> userCredentials =
                listUserCredentials(csp, site, requestedCredentialType, userId);
        if (!CollectionUtils.isEmpty(userCredentials)) {
            for (AbstractCredentialInfo userCredential : userCredentials) {
                if (Objects.nonNull(userCredential)) {
                    checkNullParamAndFillValueFromEnv(userCredential);
                    joinCredentials.add(userCredential);
                }
            }
        } else {
            addCredentialInfoFromEnv(csp, site, joinCredentials);
        }
        return joinCredentials;
    }

    /**
     * Traverse the AbstractCredentialInfo collection and set the value for CredentialVariable.
     *
     * @param abstractCredentialInfoList A collection of instances of any class that implements
     *     AbstractCredentialInfo.
     */
    public void getCredentialCapabilitiesValue(
            List<AbstractCredentialInfo> abstractCredentialInfoList) {

        for (AbstractCredentialInfo abstractCredentialInfo : abstractCredentialInfoList) {
            if (abstractCredentialInfo.getType() == CredentialType.VARIABLES) {
                CredentialVariables credentialVariables =
                        (CredentialVariables) abstractCredentialInfo;
                for (CredentialVariable variable : credentialVariables.getVariables()) {
                    variable.setValue(
                            CredentialTypeMessage.getMessageByType(CredentialType.VARIABLES));
                }
            }
        }
    }
}
