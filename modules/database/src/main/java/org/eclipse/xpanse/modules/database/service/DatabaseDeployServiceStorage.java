/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.service;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.service.query.ServiceQueryModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the DeployServiceStorage.
 */
@Component
@Transactional
public class DatabaseDeployServiceStorage implements DeployServiceStorage {

    private final DeployServiceRepository deployServiceRepository;

    @Autowired
    public DatabaseDeployServiceStorage(DeployServiceRepository deployServiceRepository) {
        this.deployServiceRepository = deployServiceRepository;
    }

    /**
     * Store the entity to the database and flush the data immediately.
     *
     * @param deployServiceEntity the entity of service.
     * @return deployServiceEntity the entity of service.
     */
    @Override
    public DeployServiceEntity storeAndFlush(DeployServiceEntity deployServiceEntity) {
        return this.deployServiceRepository.saveAndFlush(deployServiceEntity);
    }

    @Override
    public List<DeployServiceEntity> services() {
        return this.deployServiceRepository.findAll();
    }

    /**
     * Method to list database entries based DeployServiceEntity.
     *
     * @param serviceQuery query model for search deploy service entity.
     * @return Returns the database entry for the provided arguments.
     */
    @Override
    public List<DeployServiceEntity> listServices(
            ServiceQueryModel serviceQuery) {

        Specification<DeployServiceEntity> specification =
                (root, query, criteriaBuilder) -> {
                    List<Predicate> predicateList = new ArrayList<>();
                    if (Objects.nonNull(serviceQuery.getCategory())) {
                        predicateList.add(criteriaBuilder.equal(root.get("category"),
                                serviceQuery.getCategory()));
                    }
                    if (Objects.nonNull(serviceQuery.getCsp())) {
                        predicateList.add(criteriaBuilder.equal(root.get("csp"),
                                serviceQuery.getCsp()));
                    }
                    if (StringUtils.isNotBlank(serviceQuery.getServiceName())) {
                        predicateList.add(criteriaBuilder.equal(root.get("name"),
                                StringUtils.lowerCase(serviceQuery.getServiceName())));

                    }
                    if (StringUtils.isNotBlank(serviceQuery.getServiceVersion())) {
                        predicateList.add(criteriaBuilder.equal(root.get("version"),
                                StringUtils.lowerCase(serviceQuery.getServiceVersion())));
                    }

                    if (Objects.nonNull(serviceQuery.getServiceState())) {
                        predicateList.add(criteriaBuilder.equal(root.get("serviceDeploymentState"),
                                serviceQuery.getServiceState()));
                    }
                    predicateList.add(
                            criteriaBuilder.isNotNull(root.get("serviceDeploymentState")));

                    predicateList.add(criteriaBuilder.equal(root.get("userId"),
                            serviceQuery.getUserId()));

                    return query.where(criteriaBuilder.and(predicateList.toArray(new Predicate[0])))
                            .getRestriction();
                };

        return deployServiceRepository.findAll(specification);
    }

    /**
     * Get detail of deployed service using ID.
     *
     * @param id the ID of deployed service.
     * @return registerServiceEntity
     */
    @Override
    public DeployServiceEntity findDeployServiceById(UUID id) {
        Optional<DeployServiceEntity> optional =
                this.deployServiceRepository.findById(id);
        return optional.orElse(null);
    }

    @Override
    public void deleteDeployService(DeployServiceEntity deployServiceEntity) {
        this.deployServiceRepository.delete(deployServiceEntity);
    }
}
