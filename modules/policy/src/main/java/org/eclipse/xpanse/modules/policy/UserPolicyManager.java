/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.policy;

import jakarta.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.database.userpolicy.DatabaseUserPolicyStorage;
import org.eclipse.xpanse.modules.database.userpolicy.UserPolicyEntity;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.policy.exceptions.PolicyDuplicateException;
import org.eclipse.xpanse.modules.models.policy.exceptions.PolicyNotFoundException;
import org.eclipse.xpanse.modules.models.policy.userpolicy.UserPolicy;
import org.eclipse.xpanse.modules.models.policy.userpolicy.UserPolicyCreateRequest;
import org.eclipse.xpanse.modules.models.policy.userpolicy.UserPolicyQueryRequest;
import org.eclipse.xpanse.modules.models.policy.userpolicy.UserPolicyUpdateRequest;
import org.eclipse.xpanse.modules.security.UserServiceHelper;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/** The service for managing policies created by the end user. */
@Slf4j
@Component
public class UserPolicyManager {

    @Resource private PolicyManager policyManager;

    @Resource private UserServiceHelper userServiceHelper;
    @Resource private DatabaseUserPolicyStorage userPolicyStorage;

    /**
     * Get the query model for listing policies.
     *
     * @param csp The csp.
     * @param enabled The enabled.
     * @return Returns the query model.
     */
    public UserPolicyQueryRequest getUserPolicyQueryModel(Csp csp, Boolean enabled) {
        UserPolicyQueryRequest userPolicyQueryRequest = new UserPolicyQueryRequest();
        if (Objects.nonNull(csp)) {
            userPolicyQueryRequest.setCsp(csp);
        }
        if (Objects.nonNull(enabled)) {
            userPolicyQueryRequest.setEnabled(enabled);
        }
        String currentUserId = userServiceHelper.getCurrentUserId();
        userPolicyQueryRequest.setUserId(currentUserId);
        return userPolicyQueryRequest;
    }

    /**
     * List policies by the query model.
     *
     * @param queryModel The query model.
     * @return Returns all policies matched the query model.
     */
    public List<UserPolicy> listUserPolicies(UserPolicyQueryRequest queryModel) {
        List<UserPolicyEntity> policyEntities = userPolicyStorage.listPolicies(queryModel);
        return policyEntities.stream()
                .sorted(Comparator.comparing(UserPolicyEntity::getCsp))
                .map(this::conventToUserPolicy)
                .toList();
    }

    /**
     * Create new policy by user.
     *
     * @param createRequest create policy request.
     * @return Returns created policy view object.
     */
    public UserPolicy addUserPolicy(UserPolicyCreateRequest createRequest) {
        policyManager.validatePolicy(createRequest.getPolicy());
        checkIfUserPolicyIsDuplicate(createRequest.getCsp(), createRequest.getPolicy());
        UserPolicyEntity newPolicy = conventToUserPolicyEntity(createRequest);
        UserPolicyEntity userPolicyEntity = userPolicyStorage.storeAndFlush(newPolicy);
        return conventToUserPolicy(userPolicyEntity);
    }

    /**
     * Update policy.
     *
     * @param updateRequest update policy request.
     * @return Returns updated policy view object.
     */
    public UserPolicy updateUserPolicy(UUID id, UserPolicyUpdateRequest updateRequest) {
        UserPolicyEntity existingEntity = getUserPolicyEntity(id);
        UserPolicyEntity policyToUpdate = getUserPolicyToUpdate(updateRequest, existingEntity);
        UserPolicyEntity updatedPolicy = userPolicyStorage.storeAndFlush(policyToUpdate);
        return conventToUserPolicy(updatedPolicy);
    }

    /**
     * Get details of the policy.
     *
     * @param policyId the id of the policy.
     * @return Returns the policy view object.
     */
    public UserPolicy getUserPolicyDetails(UUID policyId) {
        UserPolicyEntity existingEntity = getUserPolicyEntity(policyId);
        return conventToUserPolicy(existingEntity);
    }

    private UserPolicyEntity getUserPolicyEntity(UUID policyId) {
        UserPolicyEntity existingEntity = userPolicyStorage.findPolicyById(policyId);
        if (Objects.isNull(existingEntity)) {
            String errorMsg = String.format("The user policy with id %s not found.", policyId);
            throw new PolicyNotFoundException(errorMsg);
        }

        boolean currentUserIsOwner =
                userServiceHelper.currentUserIsOwner(existingEntity.getUserId());
        if (!currentUserIsOwner) {
            throw new AccessDeniedException(
                    "No permissions to view or manage policy belonging to other users.");
        }
        return existingEntity;
    }

    /**
     * Delete the policy by user.
     *
     * @param id the id of policy.
     */
    public void deleteUserPolicy(UUID id) {
        getUserPolicyEntity(id);
        userPolicyStorage.deletePolicyById(id);
    }

    private void checkIfUserPolicyIsDuplicate(Csp csp, String policy) {

        UserPolicyQueryRequest queryModel = new UserPolicyQueryRequest();
        String currentUserId = userServiceHelper.getCurrentUserId();
        queryModel.setUserId(currentUserId);
        queryModel.setCsp(csp);
        queryModel.setPolicy(policy);
        List<UserPolicyEntity> userPolicyEntityList = userPolicyStorage.listPolicies(queryModel);
        if (!CollectionUtils.isEmpty(userPolicyEntityList)) {
            String policyKey = userPolicyEntityList.getFirst().getId().toString();
            String errMsg =
                    String.format(
                            "The same policy already exists for Csp: %s." + " with id: %s",
                            csp, policyKey);
            throw new PolicyDuplicateException(errMsg);
        }
    }

    private UserPolicy conventToUserPolicy(UserPolicyEntity userPolicyEntity) {
        if (Objects.nonNull(userPolicyEntity)) {
            UserPolicy userPolicy = new UserPolicy();
            BeanUtils.copyProperties(userPolicyEntity, userPolicy);
            userPolicy.setUserPolicyId(userPolicyEntity.getId());
            return userPolicy;
        }
        return null;
    }

    private UserPolicyEntity conventToUserPolicyEntity(
            UserPolicyCreateRequest userPolicyCreateRequest) {
        UserPolicyEntity userPolicyEntity = new UserPolicyEntity();
        BeanUtils.copyProperties(userPolicyCreateRequest, userPolicyEntity);
        String currentUserId = userServiceHelper.getCurrentUserId();
        userPolicyEntity.setUserId(currentUserId);
        return userPolicyEntity;
    }

    private UserPolicyEntity getUserPolicyToUpdate(
            UserPolicyUpdateRequest updateRequest, UserPolicyEntity existingEntity) {
        UserPolicyEntity policyToUpdate = new UserPolicyEntity();
        BeanUtils.copyProperties(existingEntity, policyToUpdate);
        boolean updatePolicy =
                StringUtils.isNotBlank(updateRequest.getPolicy())
                        && !StringUtils.equals(
                                updateRequest.getPolicy(), existingEntity.getPolicy());
        if (updatePolicy) {
            policyManager.validatePolicy(updateRequest.getPolicy());
            policyToUpdate.setPolicy(updateRequest.getPolicy());
        }

        boolean updateCsp =
                Objects.nonNull(updateRequest.getCsp())
                        && !Objects.equals(updateRequest.getCsp(), existingEntity.getCsp());
        if (updateCsp) {
            policyToUpdate.setCsp(updateRequest.getCsp());
        }

        if (Objects.nonNull(updateRequest.getEnabled())) {
            policyToUpdate.setEnabled(updateRequest.getEnabled());
        }

        if (updateCsp || updatePolicy) {
            checkIfUserPolicyIsDuplicate(policyToUpdate.getCsp(), policyToUpdate.getPolicy());
        }
        return policyToUpdate;
    }
}
