/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.policy;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.database.servicepolicy.ServicePolicyEntity;
import org.eclipse.xpanse.modules.database.servicepolicy.ServicePolicyStorage;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateStorage;
import org.eclipse.xpanse.modules.models.common.enums.UserOperation;
import org.eclipse.xpanse.modules.models.policy.exceptions.PolicyDuplicateException;
import org.eclipse.xpanse.modules.models.policy.exceptions.PolicyNotFoundException;
import org.eclipse.xpanse.modules.models.policy.servicepolicy.ServicePolicy;
import org.eclipse.xpanse.modules.models.policy.servicepolicy.ServicePolicyCreateRequest;
import org.eclipse.xpanse.modules.models.policy.servicepolicy.ServicePolicyUpdateRequest;
import org.eclipse.xpanse.modules.models.service.deployment.exceptions.FlavorInvalidException;
import org.eclipse.xpanse.modules.security.auth.UserServiceHelper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/** The service for managing policies belongs to the registered service template. */
@Slf4j
@Component
public class ServicePolicyManager {
    private static final String SEPARATOR = ",";

    private final PolicyManager policyManager;
    private final UserServiceHelper userServiceHelper;
    private final ServicePolicyStorage servicePolicyStorage;
    private final ServiceTemplateStorage serviceTemplateStorage;

    /** Constructor method. */
    @Autowired
    public ServicePolicyManager(
            PolicyManager policyManager,
            UserServiceHelper userServiceHelper,
            ServicePolicyStorage servicePolicyStorage,
            ServiceTemplateStorage serviceTemplateStorage) {
        this.policyManager = policyManager;
        this.userServiceHelper = userServiceHelper;
        this.servicePolicyStorage = servicePolicyStorage;
        this.serviceTemplateStorage = serviceTemplateStorage;
    }

    /**
     * List policies owned by the registered service template.
     *
     * @param serviceTemplateId id of the registered service template.
     * @return list of service's policies.
     */
    public List<ServicePolicy> listServicePolicies(UUID serviceTemplateId) {
        ServiceTemplateEntity existingServiceTemplate =
                getServiceTemplateEntity(
                        serviceTemplateId, UserOperation.VIEW_POLICIES_OF_SERVICE_TEMPLATE);
        if (CollectionUtils.isEmpty(existingServiceTemplate.getServicePolicyList())) {
            return Collections.emptyList();
        }
        return existingServiceTemplate.getServicePolicyList().stream()
                .map(this::conventToServicePolicy)
                .toList();
    }

    /**
     * Create new policy for the registered service template.
     *
     * @param createRequest create policy request.
     * @return Returns created policy view object.
     */
    public ServicePolicy addServicePolicy(ServicePolicyCreateRequest createRequest) {
        ServiceTemplateEntity existingServiceTemplate =
                getServiceTemplateEntity(
                        createRequest.getServiceTemplateId(),
                        UserOperation.CREATE_POLICY_OF_SERVICE_TEMPLATE);

        ServicePolicyEntity newPolicy =
                getServicePolicyToCreate(createRequest, existingServiceTemplate);

        ServicePolicyEntity storedPolicy = servicePolicyStorage.storeAndFlush(newPolicy);
        return conventToServicePolicy(storedPolicy);
    }

    private ServiceTemplateEntity getServiceTemplateEntity(
            UUID serviceTemplateId, UserOperation operation) {
        ServiceTemplateEntity existedServiceTemplate =
                serviceTemplateStorage.getServiceTemplateById(serviceTemplateId);
        checkPermission(existedServiceTemplate, operation);
        return existedServiceTemplate;
    }

    private void checkPermission(ServiceTemplateEntity serviceTemplate, UserOperation operation) {
        boolean hasManagePermission =
                userServiceHelper.currentUserCanManageIsv(serviceTemplate.getServiceVendor());
        if (!hasManagePermission) {
            String errorMsg =
                    String.format(
                            "No permission to %s owned by other service vendors.",
                            operation.toValue());
            log.error(errorMsg);
            throw new AccessDeniedException(errorMsg);
        }
    }

    /**
     * Update policy owned by the registered service template.
     *
     * @param updateRequest update policy request.
     * @return Returns updated policy view object.
     */
    public ServicePolicy updateServicePolicy(
            UUID servicePolicyId, ServicePolicyUpdateRequest updateRequest) {
        ServicePolicyEntity existingPolicy =
                getServicePolicyEntity(
                        servicePolicyId, UserOperation.UPDATE_POLICY_OF_SERVICE_TEMPLATE);
        ServicePolicyEntity policyToUpdate =
                getServicePolicyToUpdate(updateRequest, existingPolicy);
        ServicePolicyEntity updatedPolicy = servicePolicyStorage.storeAndFlush(policyToUpdate);
        return conventToServicePolicy(updatedPolicy);
    }

    private void validFlavorNames(
            List<String> flavorNameList, ServiceTemplateEntity existingServiceTemplate) {
        for (String flavorName : flavorNameList) {
            boolean flavorExists =
                    existingServiceTemplate.getOcl().getFlavors().getServiceFlavors().stream()
                            .anyMatch(flavor -> flavor.getName().equals(flavorName));
            if (!flavorExists) {
                String errMsg =
                        String.format(
                                "Flavor name %s is not valid for service template with id %s.",
                                flavorName, existingServiceTemplate.getId());
                throw new FlavorInvalidException(errMsg);
            }
        }
    }

    /**
     * Get details of the policy owned by the registered service template.
     *
     * @param policyId the id of the policy.
     * @return Returns the policy view object.
     */
    public ServicePolicy getServicePolicyDetails(UUID policyId) {
        ServicePolicyEntity existingEntity =
                getServicePolicyEntity(policyId, UserOperation.VIEW_DETAILS_OF_SERVICE_TEMPLATE);
        return conventToServicePolicy(existingEntity);
    }

    /**
     * Delete the policy owned by the registered service template.
     *
     * @param policyId the id of policy.
     */
    public void deleteServicePolicy(UUID policyId) {
        getServicePolicyEntity(policyId, UserOperation.DELETE_POLICY_OF_SERVICE_TEMPLATE);
        servicePolicyStorage.deletePolicyById(policyId);
    }

    private ServicePolicyEntity getServicePolicyEntity(UUID policyId, UserOperation operation) {
        ServicePolicyEntity existingPolicy = servicePolicyStorage.findPolicyById(policyId);
        if (Objects.isNull(existingPolicy)) {
            String errorMsg = String.format("The service policy with id %s not found.", policyId);
            throw new PolicyNotFoundException(errorMsg);
        }
        checkPermission(existingPolicy.getServiceTemplate(), operation);
        return existingPolicy;
    }

    private void checkIfServicePolicyIsDuplicate(
            ServicePolicyEntity newPolicy, ServiceTemplateEntity existingService) {
        if (!CollectionUtils.isEmpty(existingService.getServicePolicyList())) {
            String newPolicyUniqueKey = getPolicyUniqueKey(newPolicy);
            for (ServicePolicyEntity servicePolicyEntity : existingService.getServicePolicyList()) {
                if (StringUtils.equals(
                        newPolicyUniqueKey, getPolicyUniqueKey(servicePolicyEntity))) {
                    String errMsg =
                            String.format(
                                    "The same policy already exists with id: %s for "
                                            + "the registered service template with id: %s.",
                                    servicePolicyEntity.getId(), existingService.getId());
                    throw new PolicyDuplicateException(errMsg);
                }
            }
        }
    }

    private String getPolicyUniqueKey(ServicePolicyEntity servicePolicyEntity) {
        return servicePolicyEntity.getServiceTemplate().getId() + servicePolicyEntity.getPolicy();
    }

    /**
     * Convert service policy entity to service policy view object.
     *
     * @param servicePolicyEntity service policy entity.
     * @return Returns service policy view object.
     */
    public ServicePolicy conventToServicePolicy(ServicePolicyEntity servicePolicyEntity) {
        if (Objects.nonNull(servicePolicyEntity)
                && Objects.nonNull(servicePolicyEntity.getServiceTemplate())) {
            ServicePolicy servicePolicy = new ServicePolicy();
            BeanUtils.copyProperties(servicePolicyEntity, servicePolicy);
            servicePolicy.setServicePolicyId(servicePolicyEntity.getId());
            if (StringUtils.isNotBlank(servicePolicyEntity.getFlavorNames())) {
                List<String> flavorNames =
                        Arrays.asList(
                                StringUtils.split(servicePolicyEntity.getFlavorNames(), SEPARATOR));
                servicePolicy.setFlavorNameList(flavorNames);
            }
            if (Objects.nonNull(servicePolicyEntity.getServiceTemplate().getId())) {
                servicePolicy.setServiceTemplateId(
                        servicePolicyEntity.getServiceTemplate().getId());
            }
            return servicePolicy;
        }
        return null;
    }

    private ServicePolicyEntity getServicePolicyToCreate(
            ServicePolicyCreateRequest createRequest, ServiceTemplateEntity existingService) {
        ServicePolicyEntity policyToCreate = new ServicePolicyEntity();
        BeanUtils.copyProperties(createRequest, policyToCreate);
        policyToCreate.setServiceTemplate(existingService);

        if (!CollectionUtils.isEmpty(createRequest.getFlavorNameList())) {
            validFlavorNames(createRequest.getFlavorNameList(), existingService);
            String flavorNames =
                    StringUtils.join(new HashSet<>(createRequest.getFlavorNameList()), SEPARATOR);
            policyToCreate.setFlavorNames(flavorNames);
        }

        policyManager.validatePolicy(createRequest.getPolicy());
        checkIfServicePolicyIsDuplicate(policyToCreate, existingService);
        return policyToCreate;
    }

    private ServicePolicyEntity getServicePolicyToUpdate(
            ServicePolicyUpdateRequest updateRequest, ServicePolicyEntity existingPolicy) {
        ServicePolicyEntity policyToUpdate = new ServicePolicyEntity();
        BeanUtils.copyProperties(existingPolicy, policyToUpdate);
        if (Objects.nonNull(updateRequest.getFlavorNames())) {
            if (CollectionUtils.isEmpty(updateRequest.getFlavorNames())) {
                policyToUpdate.setFlavorNames(null);
            } else {
                validFlavorNames(
                        updateRequest.getFlavorNames(), existingPolicy.getServiceTemplate());
                String flavorNames =
                        StringUtils.join(new HashSet<>(updateRequest.getFlavorNames()), SEPARATOR);
                policyToUpdate.setFlavorNames(flavorNames);
            }
        }

        boolean updatePolicy =
                StringUtils.isNotBlank(updateRequest.getPolicy())
                        && !StringUtils.equals(
                                updateRequest.getPolicy(), existingPolicy.getPolicy());
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
