/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.servicepolicy;

import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Implementation of the ServicePolicyStorage. */
@Component
@Transactional
public class DatabaseServicePolicyStorage implements ServicePolicyStorage {

    private final ServicePolicyRepository servicePolicyRepository;

    @Autowired
    public DatabaseServicePolicyStorage(ServicePolicyRepository servicePolicyRepository) {
        this.servicePolicyRepository = servicePolicyRepository;
    }

    @Override
    public ServicePolicyEntity storeAndFlush(ServicePolicyEntity servicePolicyEntity) {
        return servicePolicyRepository.saveAndFlush(servicePolicyEntity);
    }

    @Override
    public ServicePolicyEntity findPolicyById(UUID id) {
        Optional<ServicePolicyEntity> optional = servicePolicyRepository.findById(id);
        return optional.orElse(null);
    }

    @Override
    public void deletePolicies(ServicePolicyEntity servicePolicyEntity) {
        servicePolicyRepository.delete(servicePolicyEntity);
    }

    @Override
    public void deletePolicyById(UUID id) {
        servicePolicyRepository.deleteById(id);
    }
}
