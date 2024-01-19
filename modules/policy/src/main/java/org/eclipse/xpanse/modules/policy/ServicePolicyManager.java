/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.policy;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.database.servicepolicy.DatabaseServicePolicyStorage;
import org.eclipse.xpanse.modules.database.servicepolicy.ServicePolicyEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.DatabaseServiceTemplateStorage;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.models.policy.exceptions.PolicyDuplicateException;
import org.eclipse.xpanse.modules.models.policy.exceptions.PolicyNotFoundException;
import org.eclipse.xpanse.modules.models.policy.servicepolicy.ServicePolicy;
import org.eclipse.xpanse.modules.models.policy.servicepolicy.ServicePolicyCreateRequest;
import org.eclipse.xpanse.modules.models.policy.servicepolicy.ServicePolicyUpdateRequest;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateNotRegistered;
import org.eclipse.xpanse.modules.security.IdentityProviderManager;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * The service for managing policies belongs to the registered service template.
 */
@Slf4j
@Component
public class ServicePolicyManager {

    @Resource
    private PolicyManager policyManager;

    @Resource
    private IdentityProviderManager identityProviderManager;

    @Resource
    private DatabaseServicePolicyStorage servicePolicyStorage;

    @Resource
    private DatabaseServiceTemplateStorage serviceTemplateStorage;


    /**
     * List policies belonging to the registered service template.
     *
     * @param serviceTemplateId id of the registered service template.
     * @return list of service's policies.
     */
    public List<ServicePolicy> listServicePolicies(String serviceTemplateId) {
        ServiceTemplateEntity existingServiceTemplate =
                getServiceTemplateEntity(UUID.fromString(serviceTemplateId));
        return existingServiceTemplate.getServicePolicyList().stream()
                .map(this::conventToServicePolicy).toList();
    }

    /**
     * Create new policy for the registered service template.
     *
     * @param createRequest create policy request.
     * @return Returns created policy view object.
     */
    public ServicePolicy addServicePolicy(ServicePolicyCreateRequest createRequest) {
        ServiceTemplateEntity existingServiceTemplate =
                getServiceTemplateEntity(createRequest.getServiceTemplateId());
        policyManager.validatePolicy(createRequest.getPolicy());
        ServicePolicyEntity newPolicy = conventToServicePolicyEntity(createRequest);
        newPolicy.setServiceTemplate(existingServiceTemplate);
        checkIfServicePolicyIsDuplicate(newPolicy, existingServiceTemplate);
        ServicePolicyEntity storedPolicy = servicePolicyStorage.storeAndFlush(newPolicy);
        return conventToServicePolicy(storedPolicy);
    }

    private ServiceTemplateEntity getServiceTemplateEntity(UUID serviceTemplateId) {
        ServiceTemplateEntity existedServiceTemplate =
                serviceTemplateStorage.getServiceTemplateById(serviceTemplateId);
        if (Objects.isNull(existedServiceTemplate)) {
            String errMsg =
                    String.format("Service template with id %s not found.", serviceTemplateId);
            throw new ServiceTemplateNotRegistered(errMsg);
        }
        Optional<String> namespace = identityProviderManager.getUserNamespace();
        if (namespace.isEmpty() || !StringUtils.equals(namespace.get(),
                existedServiceTemplate.getNamespace())) {
            throw new AccessDeniedException("No permissions to add policy belonging to the "
                    + "service template belonging to other namespaces.");
        }
        return existedServiceTemplate;
    }

    /**
     * Update policy belonging to the registered service template.
     *
     * @param updateRequest update policy request.
     * @return Returns updated policy view object.
     */
    public ServicePolicy updateServicePolicy(ServicePolicyUpdateRequest updateRequest) {
        ServicePolicyEntity existingPolicy =
                getServicePolicyEntity(updateRequest.getId(), "update");

        ServicePolicyEntity policyToUpdate =
                getServicePolicyToUpdate(updateRequest, existingPolicy);

        ServicePolicyEntity updatedPolicy = servicePolicyStorage.storeAndFlush(policyToUpdate);
        return conventToServicePolicy(updatedPolicy);
    }

    /**
     * Get details of the policy belonging to the registered service template.
     *
     * @param id the id of the policy.
     * @return Returns the policy view object.
     */
    public ServicePolicy getServicePolicyDetails(UUID id) {
        ServicePolicyEntity existingEntity = getServicePolicyEntity(id, "view");
        return conventToServicePolicy(existingEntity);
    }

    /**
     * Delete the policy belonging to the registered service template.
     *
     * @param id the id of policy.
     */
    public void deleteServicePolicy(UUID id) {
        getServicePolicyEntity(id, "delete");
        servicePolicyStorage.deletePolicyById(id);
    }

    private ServicePolicyEntity getServicePolicyEntity(UUID policyId, String managementType) {
        ServicePolicyEntity existingPolicy = servicePolicyStorage.findPolicyById(policyId);
        if (Objects.isNull(existingPolicy)) {
            String errorMsg = String.format("The policy with id %s not found.", policyId);
            throw new PolicyNotFoundException(errorMsg);
        }
        Optional<String> namespace = identityProviderManager.getUserNamespace();
        if (namespace.isEmpty() || !StringUtils.equals(namespace.get(),
                existingPolicy.getServiceTemplate().getNamespace())) {
            String errorMsg = String.format("No permissions to %s policy belonging to the "
                    + "service templates belonging to other namespaces.", managementType);
            throw new AccessDeniedException(errorMsg);
        }
        return existingPolicy;
    }

    private void checkIfServicePolicyIsDuplicate(ServicePolicyEntity newPolicy,
                                                 ServiceTemplateEntity existingServiceTemplate) {

        if (!CollectionUtils.isEmpty(existingServiceTemplate.getServicePolicyList())) {
            for (ServicePolicyEntity servicePolicyEntity
                    : existingServiceTemplate.getServicePolicyList()) {
                if (StringUtils.equals(servicePolicyEntity.getPolicy(), newPolicy.getPolicy())) {
                    String errMsg = String.format("The same policy already exists with id: %s for "
                                    + "the registered service template with id: %s.",
                            servicePolicyEntity.getId(), existingServiceTemplate.getId());
                    throw new PolicyDuplicateException(errMsg);
                }
            }
        }
    }

    private ServicePolicy conventToServicePolicy(ServicePolicyEntity servicePolicyEntity) {
        if (Objects.nonNull(servicePolicyEntity)
                && Objects.nonNull(servicePolicyEntity.getServiceTemplate())) {
            ServicePolicy servicePolicy = new ServicePolicy();
            BeanUtils.copyProperties(servicePolicyEntity, servicePolicy);
            servicePolicy.setServiceTemplateId(servicePolicyEntity.getServiceTemplate().getId());
            return servicePolicy;
        }
        return null;
    }

    private ServicePolicyEntity conventToServicePolicyEntity(
            ServicePolicyCreateRequest servicePolicyCreateRequest) {
        ServicePolicyEntity servicePolicyEntity = new ServicePolicyEntity();
        BeanUtils.copyProperties(servicePolicyCreateRequest, servicePolicyEntity);
        return servicePolicyEntity;
    }

    private ServicePolicyEntity getServicePolicyToUpdate(ServicePolicyUpdateRequest updateRequest,
                                                         ServicePolicyEntity existingPolicy) {
        ServicePolicyEntity policyToUpdate = new ServicePolicyEntity();
        BeanUtils.copyProperties(existingPolicy, policyToUpdate);
        boolean updatePolicy = StringUtils.isNotBlank(updateRequest.getPolicy())
                && !StringUtils.equals(updateRequest.getPolicy(), existingPolicy.getPolicy());
        if (updatePolicy) {
            policyManager.validatePolicy(updateRequest.getPolicy());
            policyToUpdate.setPolicy(updateRequest.getPolicy());
            checkIfServicePolicyIsDuplicate(policyToUpdate, existingPolicy.getServiceTemplate());
        }
        if (Objects.nonNull(updateRequest.getEnabled())) {
            policyToUpdate.setEnabled(updateRequest.getEnabled());
        }
        return policyToUpdate;
    }

}
