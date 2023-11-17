/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.policy.policyman;

import jakarta.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.database.policy.DatabasePolicyStorage;
import org.eclipse.xpanse.modules.database.policy.PolicyEntity;
import org.eclipse.xpanse.modules.models.policy.PolicyCreateRequest;
import org.eclipse.xpanse.modules.models.policy.PolicyQueryRequest;
import org.eclipse.xpanse.modules.models.policy.PolicyUpdateRequest;
import org.eclipse.xpanse.modules.models.policy.PolicyVo;
import org.eclipse.xpanse.modules.models.policy.exceptions.PoliciesEvaluationFailedException;
import org.eclipse.xpanse.modules.models.policy.exceptions.PoliciesValidationFailedException;
import org.eclipse.xpanse.modules.models.policy.exceptions.PolicyDuplicateException;
import org.eclipse.xpanse.modules.models.policy.exceptions.PolicyNotFoundException;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
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
import org.eclipse.xpanse.modules.security.IdentityProviderManager;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClientException;

/**
 * The service of policy-man.
 */
@Slf4j
@Component
public class PolicyManager {


    @Resource
    private DatabasePolicyStorage policyStorage;

    @Resource
    private AdminApi adminApi;

    @Resource
    private PoliciesValidateApi policiesValidateApi;

    @Resource
    private PoliciesEvaluationApi policiesEvaluationApi;

    @Resource
    private IdentityProviderManager identityProviderManager;

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
            org.eclipse.xpanse.modules.policy.policyman.generated.model.HealthStatus
                    healthStatus = policyManSystemStatus.getHealthStatus();
            policyManStatus.setHealthStatus(HealthStatus.valueOf(healthStatus.getValue()));
        } catch (RestClientException e) {
            log.error("Get status of policy-man error:{}", e.getMessage());
            policyManStatus.setHealthStatus(HealthStatus.NOK);
            policyManStatus.setDetails(e.getMessage());
        }

        return policyManStatus;
    }


    /**
     * List policies by the query model.
     *
     * @param queryModel The query model.
     * @return Returns all policies matched the query model.
     */
    public List<PolicyVo> listPolicies(PolicyQueryRequest queryModel) {
        List<PolicyEntity> policyEntities = policyStorage.listPolicies(queryModel);
        return policyEntities.stream().sorted(Comparator.comparing(PolicyEntity::getCsp))
                .map(this::conventToPolicyVo).toList();
    }

    /**
     * Create new policy.
     *
     * @param createRequest create policy request.
     * @return Returns created policy view object.
     */
    public PolicyVo addPolicy(PolicyCreateRequest createRequest) {
        validatePolicy(createRequest.getPolicy());
        checkIfPolicyIsDuplicate(createRequest.getCsp(), createRequest.getPolicy());
        PolicyEntity newPolicy = conventToPolicyEntity(createRequest);
        PolicyEntity policyEntity = policyStorage.store(newPolicy);
        return conventToPolicyVo(policyEntity);
    }

    /**
     * Update policy.
     *
     * @param updateRequest update policy request.
     * @return Returns updated policy view object.
     */
    public PolicyVo updatePolicy(PolicyUpdateRequest updateRequest) {
        PolicyEntity existingEntity = policyStorage.findPolicyById(updateRequest.getId());
        if (Objects.isNull(existingEntity)) {
            String errorMsg = String.format("The policy with id %s not found.",
                    updateRequest.getId());
            throw new PolicyNotFoundException(errorMsg);
        }

        Optional<String> userIdOptional = identityProviderManager.getCurrentLoginUserId();
        if (!StringUtils.equals(userIdOptional.orElse(null), existingEntity.getUserId())) {
            throw new AccessDeniedException(
                    "No permissions to update policy belonging to other users.");
        }

        PolicyEntity policyToUpdate = getPolicyToUpdate(updateRequest, existingEntity);

        PolicyEntity updatedPolicy = policyStorage.store(policyToUpdate);
        return conventToPolicyVo(updatedPolicy);
    }

    /**
     * Get details of the policy.
     *
     * @param id the id of the policy.
     * @return Returns the policy view object.
     */
    public PolicyVo getPolicyDetails(UUID id) {
        PolicyEntity existingEntity = policyStorage.findPolicyById(id);
        if (Objects.isNull(existingEntity)) {
            String errorMsg = String.format("The policy with id %s not found.", id);
            throw new PolicyNotFoundException(errorMsg);
        }
        Optional<String> userIdOptional = identityProviderManager.getCurrentLoginUserId();
        if (!StringUtils.equals(userIdOptional.orElse(null), existingEntity.getUserId())) {
            throw new AccessDeniedException(
                    "No permissions to view policy belonging to other users.");
        }
        return conventToPolicyVo(existingEntity);
    }


    /**
     * Delete the policy.
     *
     * @param id the id of policy.
     */
    public void deletePolicy(UUID id) {
        PolicyEntity existingEntity = policyStorage.findPolicyById(id);
        if (Objects.isNull(existingEntity)) {
            String errorMsg = String.format("The policy with id %s not found.", id);
            throw new PolicyNotFoundException(errorMsg);
        }
        Optional<String> userIdOptional = identityProviderManager.getCurrentLoginUserId();
        if (!StringUtils.equals(userIdOptional.orElse(null), existingEntity.getUserId())) {
            throw new AccessDeniedException(
                    "No permissions to delete policy belonging to other users.");
        }
        policyStorage.deletePolicyById(id);
    }

    private void checkIfPolicyIsDuplicate(Csp csp, String policy) {

        PolicyQueryRequest queryModel = new PolicyQueryRequest();
        Optional<String> userIdOptional = identityProviderManager.getCurrentLoginUserId();
        queryModel.setUserId(userIdOptional.orElse(null));
        queryModel.setCsp(csp);
        queryModel.setPolicy(policy);
        List<PolicyEntity> policyEntityList = policyStorage.listPolicies(queryModel);
        if (!CollectionUtils.isEmpty(policyEntityList)) {
            String policyKey = policyEntityList.get(0).getId().toString();
            String errMsg = String.format("The same policy already exists for Csp: %s."
                    + " with id: %s", csp, policyKey);
            throw new PolicyDuplicateException(errMsg);
        }

    }


    private PolicyVo conventToPolicyVo(PolicyEntity policyEntity) {
        if (Objects.nonNull(policyEntity)) {
            PolicyVo policyVo = new PolicyVo();
            BeanUtils.copyProperties(policyEntity, policyVo);
            return policyVo;
        }
        return null;
    }

    private PolicyEntity conventToPolicyEntity(PolicyCreateRequest policyCreateRequest) {
        PolicyEntity policyEntity = new PolicyEntity();
        BeanUtils.copyProperties(policyCreateRequest, policyEntity);
        Optional<String> userIdOptional = identityProviderManager.getCurrentLoginUserId();
        policyEntity.setUserId(userIdOptional.orElse(null));
        return policyEntity;
    }


    private void validatePolicy(String policy) {
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

    private PolicyEntity getPolicyToUpdate(PolicyUpdateRequest updateRequest,
                                           PolicyEntity existingEntity) {
        PolicyEntity policyToUpdate = new PolicyEntity();
        BeanUtils.copyProperties(existingEntity, policyToUpdate);
        boolean updatePolicy = StringUtils.isNotBlank(updateRequest.getPolicy())
                && !StringUtils.equals(updateRequest.getPolicy(), existingEntity.getPolicy());
        if (updatePolicy) {
            validatePolicy(updateRequest.getPolicy());
            policyToUpdate.setPolicy(updateRequest.getPolicy());
        }

        boolean updateCsp = Objects.nonNull(updateRequest.getCsp())
                && !Objects.equals(updateRequest.getCsp(), existingEntity.getCsp());
        if (updateCsp) {
            policyToUpdate.setCsp(updateRequest.getCsp());
        }

        if (Objects.nonNull(updateRequest.getEnabled())) {
            policyToUpdate.setEnabled(updateRequest.getEnabled());
        }

        if (updateCsp || updatePolicy) {
            checkIfPolicyIsDuplicate(policyToUpdate.getCsp(), policyToUpdate.getPolicy());
        }
        return policyToUpdate;
    }

    /**
     * Evaluate input by polices.
     *
     * @param policies list of policies.
     * @param input    input
     */
    public void evaluatePolicies(List<String> policies, String input) {
        boolean valid = true;
        String errorMsg = "";
        EvalCmdList cmdList = new EvalCmdList();
        cmdList.setPolicyList(policies);
        cmdList.setInput(input);
        try {
            EvalResult evalResult = policiesEvaluationApi.evaluatePoliciesPost(cmdList);
            log.info("Evaluate policies response:{}", evalResult.toString());
            if (!evalResult.getIsSuccessful()) {
                valid = false;
                errorMsg = String.format("Evaluate policies failed. policies: %s, input: %s",
                        evalResult.getPolicy(), evalResult.getInput());
            }
        } catch (RestClientException e) {
            log.error("Evaluate policies error:{}", e.getMessage());
            valid = false;
            errorMsg = e.getMessage();
        }
        if (!valid) {
            throw new PoliciesEvaluationFailedException(errorMsg);
        }

    }


}
