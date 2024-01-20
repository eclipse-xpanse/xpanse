/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment;

import jakarta.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.database.servicetemplate.DatabaseServiceTemplateStorage;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.policy.exceptions.PoliciesEvaluationFailedException;
import org.eclipse.xpanse.modules.models.policy.servicepolicy.ServicePolicy;
import org.eclipse.xpanse.modules.models.policy.userpolicy.UserPolicy;
import org.eclipse.xpanse.modules.models.policy.userpolicy.UserPolicyQueryRequest;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.eclipse.xpanse.modules.policy.PolicyManager;
import org.eclipse.xpanse.modules.policy.UserPolicyManager;
import org.eclipse.xpanse.modules.policy.policyman.generated.model.EvalResult;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * Bean to wrap all logic to validate policies during service deployment.
 */
@Component
@Slf4j
public class PolicyValidator {

    @Resource
    private PolicyManager policyManager;
    @Resource
    private UserPolicyManager userPolicyManager;
    @Resource
    private DatabaseServiceTemplateStorage serviceTemplateStorage;
    @Resource
    private DeployerKindManager deployerKindManager;

    private List<ServicePolicy> getServicePolicies(UUID serviceTemplateId) {
        ServiceTemplateEntity existedServiceTemplate =
                serviceTemplateStorage.getServiceTemplateById(serviceTemplateId);
        if (Objects.nonNull(existedServiceTemplate)
                && Objects.nonNull(existedServiceTemplate.getServicePolicyList())) {
            return existedServiceTemplate.getServicePolicyList().stream()
                    .filter(servicePolicyEntity -> servicePolicyEntity.getEnabled()
                            && StringUtils.isNotBlank(servicePolicyEntity.getPolicy()))
                    .map(servicePolicyEntity -> {
                        ServicePolicy servicePolicy = new ServicePolicy();
                        BeanUtils.copyProperties(servicePolicyEntity, servicePolicy);
                        servicePolicy.setServiceTemplateId(
                                servicePolicyEntity.getServiceTemplate().getId());
                        return servicePolicy;
                    }).toList();
        }
        return Collections.emptyList();
    }

    private List<UserPolicy> getUserPolicies(DeployTask deployTask) {
        if (Objects.nonNull(deployTask.getDeployRequest())) {
            String userId = deployTask.getDeployRequest().getUserId();
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
            return;
        }
        String planJson = deployerKindManager.getDeployment(
                deployTask.getOcl().getDeployment().getKind()).getDeploymentPlanAsJson(deployTask);
        if (StringUtils.isEmpty(planJson)) {
            return;
        }
        evaluateDeploymentPlanWithServicePolicies(servicePolicies, planJson);
        evaluateDeploymentPlanWithUserPolicies(userPolicies, planJson);
    }

    private void evaluateDeploymentPlanWithServicePolicies(List<ServicePolicy> servicePolicies,
                                                           String planJson) {
        if (!CollectionUtils.isEmpty(servicePolicies)) {
            List<String> servicePolicyList = servicePolicies.stream()
                    .map(ServicePolicy::getPolicy).toList();
            String errMsg = "Evaluate deployment plan with service policies failed.";
            EvalResult evalResult = policyManager.evaluatePolicies(servicePolicyList, planJson);
            if (!evalResult.getIsSuccessful()) {
                ServicePolicy failedServicePolicy = servicePolicies.stream()
                        .filter(servicePolicy -> servicePolicy.getPolicy()
                                .equals(evalResult.getPolicy()))
                        .findFirst().orElse(null);
                if (Objects.nonNull(failedServicePolicy)) {
                    errMsg = String.format(errMsg + "\n Failed by the service policy with id: %s."
                                    + "\n Deployment plan: %s",
                            failedServicePolicy.getId(), planJson);
                } else {
                    errMsg = String.format(errMsg + "\n Failed by the service policy with context"
                                    + ": %s.\"\nDeployment plan: %s",
                            evalResult.getPolicy(), planJson);
                }
                log.error(errMsg);
                throw new PoliciesEvaluationFailedException(errMsg);
            } else {
                log.info("Evaluate deployment plan with service policies successful.");
            }
        }
    }


    private void evaluateDeploymentPlanWithUserPolicies(List<UserPolicy> userPolicies,
                                                        String planJson) {
        if (!CollectionUtils.isEmpty(userPolicies)) {
            String errMsg = "Evaluate deployment plan with user policies failed.";
            List<String> userPolicyList = userPolicies.stream()
                    .map(UserPolicy::getPolicy).toList();
            EvalResult evalResult = policyManager.evaluatePolicies(userPolicyList, planJson);
            if (!evalResult.getIsSuccessful()) {
                UserPolicy failedUserPolicy = userPolicies.stream()
                        .filter(userPolicy -> userPolicy.getPolicy()
                                .equals(evalResult.getPolicy()))
                        .findFirst().orElse(null);
                if (Objects.nonNull(failedUserPolicy)) {
                    errMsg = String.format(errMsg + "\n Failed by the user policy with id: %s."
                                    + "\n Deployment plan: %s",
                            failedUserPolicy.getId(), planJson);
                } else {
                    errMsg = String.format(errMsg + "\n Failed by the user policy with context: %s."
                                    + "\nDeployment plan: %s",
                            evalResult.getPolicy(), planJson);
                }
                log.error(errMsg);
                throw new PoliciesEvaluationFailedException(errMsg);
            } else {
                log.info("Evaluate deployment plan with user policies successful.");
            }
        }
    }
}
