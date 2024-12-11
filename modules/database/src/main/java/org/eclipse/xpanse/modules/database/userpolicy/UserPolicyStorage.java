/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.userpolicy;

import java.util.List;
import java.util.UUID;
import org.eclipse.xpanse.modules.models.policy.userpolicy.UserPolicyQueryRequest;

/** Interface for persist of UserPolicyManager. */
public interface UserPolicyStorage {

    /**
     * Add or update valid data to database.
     *
     * @param userPolicyEntity the model of deployed service.
     * @return Returns stored entity.
     */
    UserPolicyEntity storeAndFlush(UserPolicyEntity userPolicyEntity);

    /**
     * Method to get stored database entries by query model.
     *
     * @param query query model.
     * @return Returns all rows matched the query info from the database table.
     */
    List<UserPolicyEntity> listPolicies(UserPolicyQueryRequest query);

    /**
     * Get policy using ID.
     *
     * @param id the id of the policy.
     * @return policyEntity.
     */
    UserPolicyEntity findPolicyById(UUID id);

    /**
     * Delete stored policy entities using policy entity.
     *
     * @param userPolicyEntity the entity of policy.
     */
    void deletePolicies(UserPolicyEntity userPolicyEntity);

    /**
     * Delete stored policy entity using the id of policy.
     *
     * @param id the id of the policy.
     */
    void deletePolicyById(UUID id);
}
