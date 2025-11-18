/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateStorage;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.policy.exceptions.PoliciesEvaluationFailedException;
import org.eclipse.xpanse.modules.models.policy.servicepolicy.ServicePolicy;
import org.eclipse.xpanse.modules.models.policy.userpolicy.UserPolicy;
import org.eclipse.xpanse.modules.models.policy.userpolicy.UserPolicyQueryRequest;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.eclipse.xpanse.modules.policy.PolicyManager;
import org.eclipse.xpanse.modules.policy.ServicePolicyManager;
import org.eclipse.xpanse.modules.policy.UserPolicyManager;
import org.eclipse.xpanse.modules.policy.policyman.generated.model.EvalResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/** Bean to wrap all logic to validate policies during service deployment. */
@Component
@Slf4j
public class PolicyValidator {

    private final PolicyManager policyManager;
    private final UserPolicyManager userPolicyManager;
    private final ServicePolicyManager servicePolicyManager;
    private final ServiceTemplateStorage serviceTemplateStorage;
    private final DeployerKindManager deployerKindManager;

    /** Constructor method. */
    @Autowired
    public PolicyValidator(
            PolicyManager policyManager,
            UserPolicyManager userPolicyManager,
            ServicePolicyManager servicePolicyManager,
            ServiceTemplateStorage serviceTemplateStorage,
            DeployerKindManager deployerKindManager) {
        this.policyManager = policyManager;
        this.userPolicyManager = userPolicyManager;
        this.servicePolicyManager = servicePolicyManager;
        this.serviceTemplateStorage = serviceTemplateStorage;
        this.deployerKindManager = deployerKindManager;
    }

    private List<ServicePolicy> getServicePolicies(UUID serviceTemplateId) {
        ServiceTemplateEntity existedServiceTemplate =
                serviceTemplateStorage.getServiceTemplateById(serviceTemplateId);
        if (Objects.nonNull(existedServiceTemplate)
                && Objects.nonNull(existedServiceTemplate.getServicePolicyList())) {
            return existedServiceTemplate.getServicePolicyList().stream()
                    .filter(
                            servicePolicyEntity ->
                                    servicePolicyEntity.getEnabled()
                                            && StringUtils.isNotBlank(
                                                    servicePolicyEntity.getPolicy()))
                    .map(servicePolicyManager::conventToServicePolicy)
                    .toList();
        }
        return Collections.emptyList();
    }

    private List<UserPolicy> getUserPolicies(DeployTask deployTask) {
        if (Objects.nonNull(deployTask.getDeployRequest())) {
            String userId = deployTask.getUserId();
            Csp csp = deployTask.getDeployRequest().getCsp();
            UserPolicyQueryRequest queryRequest = new UserPolicyQueryRequest();
            queryRequest.setUserId(userId);
            queryRequest.setCsp(csp);
            queryRequest.setEnabled(true);
            return userPolicyManager.listUserPolicies(queryRequest);
        }
        return Collections.emptyList();
    }

    /**
     * Validate deployment with policies.
     *
     * @param deployTask deploy task.
     */
    public void validateDeploymentWithPolicies(DeployTask deployTask) {

        List<ServicePolicy> servicePolicies = getServicePolicies(deployTask.getServiceTemplateId());
        List<UserPolicy> userPolicies = getUserPolicies(deployTask);
        if (CollectionUtils.isEmpty(userPolicies) && CollectionUtils.isEmpty(servicePolicies)) {
            log.info("No service or user policies found.");
            return;
        }
        String planJson =
                deployerKindManager
                        .getDeployment(
                                deployTask.getOcl().getDeployment().getDeployerTool().getKind())
                        .getDeploymentPlanAsJson(deployTask);
        if (StringUtils.isEmpty(planJson)) {
            return;
        }
        evaluateDeploymentPlanWithServicePolicies(
                servicePolicies, planJson, deployTask.getDeployRequest().getFlavor());
        evaluateDeploymentPlanWithUserPolicies(userPolicies, planJson);
    }

    private void evaluateDeploymentPlanWithServicePolicies(
            List<ServicePolicy> servicePolicies, String planJson, String flavorName) {
        if (!CollectionUtils.isEmpty(servicePolicies)) {
            List<String> policyList = new ArrayList<>();
            for (ServicePolicy servicePolicy : servicePolicies) {
                if (CollectionUtils.isEmpty(servicePolicy.getFlavorNameList())) {
                    policyList.add(servicePolicy.getPolicy());
                } else if (servicePolicy.getFlavorNameList().contains(flavorName)) {
                    policyList.add(servicePolicy.getPolicy());
                }
            }

            String errMsg = "Evaluate deployment plan with service policies failed.";
            EvalResult evalResult = policyManager.evaluatePolicies(policyList, planJson);
            if (!evalResult.getIsSuccessful()) {
                ServicePolicy failedServicePolicy =
                        servicePolicies.stream()
                                .filter(
                                        servicePolicy ->
                                                servicePolicy
                                                        .getPolicy()
                                                        .equals(evalResult.getPolicy()))
                                .findFirst()
                                .orElse(null);
                if (Objects.nonNull(failedServicePolicy)) {
                    errMsg =
                            String.format(
                                    errMsg
                                            + "\n Failed by the service policy with id: %s."
                                            + "\n Deployment plan: %s",
                                    failedServicePolicy.getServicePolicyId(),
                                    planJson);
                } else {
                    errMsg =
                            String.format(
                                    errMsg
                                            + "\n Failed by the service policy with context"
                                            + ": %s.\"\nDeployment plan: %s",
                                    evalResult.getPolicy(),
                                    planJson);
                }
                log.error(errMsg);
                throw new PoliciesEvaluationFailedException(errMsg);
            } else {
                log.info("Evaluate deployment plan with service policies successful.");
            }
        }
    }

    private void evaluateDeploymentPlanWithUserPolicies(
            List<UserPolicy> userPolicies, String planJson) {
        if (!CollectionUtils.isEmpty(userPolicies)) {
            String errMsg = "Evaluate deployment plan with user policies failed.";
            List<String> userPolicyList = userPolicies.stream().map(UserPolicy::getPolicy).toList();
            EvalResult evalResult = policyManager.evaluatePolicies(userPolicyList, planJson);
            if (!evalResult.getIsSuccessful()) {
                UserPolicy failedUserPolicy =
                        userPolicies.stream()
                                .filter(
                                        userPolicy ->
                                                userPolicy
                                                        .getPolicy()
                                                        .equals(evalResult.getPolicy()))
                                .findFirst()
                                .orElse(null);
                if (Objects.nonNull(failedUserPolicy)) {
                    errMsg =
                            String.format(
                                    errMsg
                                            + "\n Failed by the user policy with id: %s."
                                            + "\n Deployment plan: %s",
                                    failedUserPolicy.getUserPolicyId(),
                                    planJson);
                } else {
                    errMsg =
                            String.format(
                                    errMsg
                                            + "\n Failed by the user policy with context: %s."
                                            + "\nDeployment plan: %s",
                                    evalResult.getPolicy(),
                                    planJson);
                }
                log.error(errMsg);
                throw new PoliciesEvaluationFailedException(errMsg);
            } else {
                log.info("Evaluate deployment plan with user policies successful.");
            }
        }
    }
}
