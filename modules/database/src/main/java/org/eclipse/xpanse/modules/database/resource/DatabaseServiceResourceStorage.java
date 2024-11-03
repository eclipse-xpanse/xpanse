/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.resource;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

/**
 * Implementation of the ServiceResourceStorage.
 */
@Component
@Transactional
public class DatabaseServiceResourceStorage implements ServiceResourceStorage {

    private final ServiceResourceRepository serviceResourceRepository;

    @Autowired
    public DatabaseServiceResourceStorage(ServiceResourceRepository serviceResourceRepository) {
        this.serviceResourceRepository = serviceResourceRepository;
    }

    @Override
    public void deleteByDeployServiceId(UUID serviceId) {
        Specification<ServiceResourceEntity> specification =
                (root, query, criteriaBuilder) -> {
                    List<Predicate> predicateList = new ArrayList<>();
                    predicateList.add(criteriaBuilder.equal(root.get("serviceId"), serviceId));
                    return query.where(criteriaBuilder.and(predicateList.toArray(new Predicate[0])))
                            .getRestriction();
                };
        serviceResourceRepository.delete(specification);
    }

    /**
     * Get detail of service resource using ID.
     *
     * @param id the ID of deployed resource.
     * @return ServiceResourceEntity
     */
    @Override
    public ServiceResourceEntity findServiceResourceById(UUID id) {
        return serviceResourceRepository.findById(id).orElse(null);
    }

    /**
     * Get detail of service resource using ID.
     *
     * @param resourceId the RESOURCE_ID of deployed resource.
     * @return ServiceResourceEntity
     */
    @Override
    public ServiceResourceEntity findServiceResourceByResourceId(String resourceId) {
        Specification<ServiceResourceEntity> specification =
                (root, query, criteriaBuilder) -> {
                    List<Predicate> predicateList = new ArrayList<>();
                    predicateList.add(criteriaBuilder.equal(root.get("resourceId"),
                            resourceId));
                    return query.where(criteriaBuilder.and(predicateList.toArray(new Predicate[0])))
                            .getRestriction();
                };

        List<ServiceResourceEntity> deployResources =
                serviceResourceRepository.findAll(specification);
        if (!CollectionUtils.isEmpty(deployResources)) {
            return deployResources.getFirst();
        }
        return null;
    }


}
