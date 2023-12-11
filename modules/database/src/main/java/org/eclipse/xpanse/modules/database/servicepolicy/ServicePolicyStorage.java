/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.servicepolicy;

import java.util.UUID;

/**
 * Interface for persist of ServicePolicyManager.
 */
public interface ServicePolicyStorage {

    /**
     * Add or update valid data to database.
     *
     * @param servicePolicyEntity the model of deployed service.
     * @return Returns stored entity.
     */
    ServicePolicyEntity storeAndFlush(ServicePolicyEntity servicePolicyEntity);

    /**
     * Get policy using ID.
     *
     * @param id the id of the policy.
     * @return policyEntity.
     */
    ServicePolicyEntity findPolicyById(UUID id);

    /**
     * Delete stored policy entities using policy entity.
     *
     * @param servicePolicyEntity the entity of policy.
     */
    void deletePolicies(ServicePolicyEntity servicePolicyEntity);

    /**
     * Delete stored policy entity using the id of policy.
     *
     * @param id the id  of the policy.
     */
    void deletePolicyById(UUID id);
}
