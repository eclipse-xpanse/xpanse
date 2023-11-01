/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.policy;

import java.util.List;
import java.util.UUID;
import org.eclipse.xpanse.modules.models.policy.PolicyQueryRequest;

/**
 * Interface for persist of PoliciesManageService.
 */
public interface PolicyStorage {

    /**
     * Add or update valid data to database.
     *
     * @param policyEntity the model of deployed service.
     * @return Returns stored entity.
     */
    PolicyEntity store(PolicyEntity policyEntity);

    /**
     * Method to get all stored database entries.
     *
     * @return Returns all rows from the database table.
     */
    List<PolicyEntity> policies();

    /**
     * Method to get stored database entries by query model.
     *
     * @param query query model.
     * @return Returns all rows matched the query info from the database table.
     */
    List<PolicyEntity> listPolicies(PolicyQueryRequest query);

    /**
     * Get policy using ID.
     *
     * @param id the id of the policy.
     * @return policyEntity.
     */
    PolicyEntity findPolicyById(UUID id);

    /**
     * Delete stored policy entities using policy entity.
     *
     * @param policyEntity the entity of policy.
     */
    void deletePolicies(PolicyEntity policyEntity);

    /**
     * Delete stored policy entity using the id of policy.
     *
     * @param id the id  of the policy.
     */
    void deletePolicyById(UUID id);
}
