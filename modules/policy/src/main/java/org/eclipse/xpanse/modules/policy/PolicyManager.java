/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.policy;

import jakarta.annotation.Resource;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.policy.exceptions.PoliciesValidationFailedException;
import org.eclipse.xpanse.modules.models.system.BackendSystemStatus;
import org.eclipse.xpanse.modules.models.system.enums.BackendSystemType;
import org.eclipse.xpanse.modules.models.system.enums.HealthStatus;
import org.eclipse.xpanse.modules.policy.policyman.generated.api.AdminApi;
import org.eclipse.xpanse.modules.policy.policyman.generated.api.PoliciesEvaluationApi;
import org.eclipse.xpanse.modules.policy.policyman.generated.api.PoliciesValidateApi;
import org.eclipse.xpanse.modules.policy.policyman.generated.model.EvalCmdList;
import org.eclipse.xpanse.modules.policy.policyman.generated.model.EvalResult;
import org.eclipse.xpanse.modules.policy.policyman.generated.model.ValidatePolicyList;
import org.eclipse.xpanse.modules.policy.policyman.generated.model.ValidateResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

/** The service implements policy common methods. */
@Slf4j
@Component
public class PolicyManager {

    @Resource private AdminApi adminApi;

    @Resource private PoliciesValidateApi policiesValidateApi;

    @Resource private PoliciesEvaluationApi policiesEvaluationApi;

    @Value("${policy.man.endpoint:http://localhost:8090}")
    private String policyManBaseUrl;

    /**
     * Get system status of the policyMan.
     *
     * @return Returns BackendSystemStatus.
     */
    public BackendSystemStatus getPolicyManStatus() {
        BackendSystemStatus policyManStatus = new BackendSystemStatus();
        policyManStatus.setBackendSystemType(BackendSystemType.POLICY_MAN);
        policyManStatus.setName(BackendSystemType.POLICY_MAN.toValue());
        policyManStatus.setEndpoint(policyManBaseUrl);
        try {
            org.eclipse.xpanse.modules.policy.policyman.generated.model.SystemStatus
                    policyManSystemStatus = adminApi.healthGet();
            org.eclipse.xpanse.modules.policy.policyman.generated.model.HealthStatus healthStatus =
                    policyManSystemStatus.getHealthStatus();
            policyManStatus.setHealthStatus(HealthStatus.valueOf(healthStatus.getValue()));
        } catch (RestClientException e) {
            log.error("Get status of policy-man error:{}", e.getMessage());
            policyManStatus.setHealthStatus(HealthStatus.NOK);
            policyManStatus.setDetails(e.getMessage());
        }

        return policyManStatus;
    }

    /**
     * Valid policy.
     *
     * @param policy policy.
     */
    public void validatePolicy(String policy) {
        boolean valid = true;
        String errorMsg = "";
        ValidatePolicyList validatePolicyList = new ValidatePolicyList();
        validatePolicyList.setPolicyList(List.of(policy));
        try {
            ValidateResponse validateResponse =
                    policiesValidateApi.validatePoliciesPost(validatePolicyList);
            log.info("Validate policies response:{}", validateResponse.toString());
            if (!validateResponse.getIsSuccessful()) {
                valid = false;
                errorMsg = validateResponse.getErrMsg();
            }
        } catch (RestClientException e) {
            log.error("Validate policies error:{}", e.getMessage());
            valid = false;
            errorMsg = e.getMessage();
        }
        if (!valid) {
            throw new PoliciesValidationFailedException(errorMsg);
        }
    }

    /**
     * Evaluate input by polices.
     *
     * @param policies list of policies.
     * @param input input
     */
    public EvalResult evaluatePolicies(List<String> policies, String input) {
        try {
            EvalCmdList cmdList = new EvalCmdList();
            cmdList.setPolicyList(policies);
            cmdList.setInput(input);
            EvalResult evalResult = policiesEvaluationApi.evaluatePoliciesPost(cmdList);
            log.info("Evaluate input with policies response:{}", evalResult.toString());
            return evalResult;
        } catch (RestClientException e) {
            String errorMsg = "Evaluate input with policies failed.\nError:" + e.getMessage();
            log.error(errorMsg);
            throw new RestClientException(errorMsg);
        }
    }
}
