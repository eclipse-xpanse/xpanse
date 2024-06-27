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
 * Implementation of the DeployResourceStorage.
 */
@Component
@Transactional
public class DatabaseDeployResourceStorage implements DeployResourceStorage {

    private final DeployResourceRepository deployResourceRepository;

    @Autowired
    public DatabaseDeployResourceStorage(DeployResourceRepository deployResourceRepository) {
        this.deployResourceRepository = deployResourceRepository;
    }

    @Override
    public void deleteByDeployServiceId(UUID serviceId) {
        Specification<DeployResourceEntity> specification =
                (root, query, criteriaBuilder) -> {
                    List<Predicate> predicateList = new ArrayList<>();
                    predicateList.add(criteriaBuilder.equal(root.get("serviceId"), serviceId));
                    return query.where(criteriaBuilder.and(predicateList.toArray(new Predicate[0])))
                            .getRestriction();
                };
        deployResourceRepository.delete(specification);
    }

    /**
     * Get detail of deployed resource using ID.
     *
     * @param id the ID of deployed resource.
     * @return DeployResourceEntity
     */
    @Override
    public DeployResourceEntity findDeployResourceById(UUID id) {
        return deployResourceRepository.findById(id).orElse(null);
    }

    /**
     * Get detail of deployed resource using ID.
     *
     * @param resourceId the RESOURCE_ID of deployed resource.
     * @return DeployResourceEntity
     */
    @Override
    public DeployResourceEntity findDeployResourceByResourceId(String resourceId) {
        Specification<DeployResourceEntity> specification =
                (root, query, criteriaBuilder) -> {
                    List<Predicate> predicateList = new ArrayList<>();
                    predicateList.add(criteriaBuilder.equal(root.get("resourceId"),
                            resourceId));
                    return query.where(criteriaBuilder.and(predicateList.toArray(new Predicate[0])))
                            .getRestriction();
                };

        List<DeployResourceEntity> deployResources =
                deployResourceRepository.findAll(specification);
        if (!CollectionUtils.isEmpty(deployResources)) {
            return deployResources.getFirst();
        }
        return null;
    }


}
