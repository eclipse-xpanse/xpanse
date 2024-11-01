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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the ServiceDeploymentStorage.
 */
@Component
@Transactional
public class DatabaseServiceDeploymentStorage implements ServiceDeploymentStorage {

    private final ServiceDeploymentRepository serviceDeploymentRepository;

    @Autowired
    public DatabaseServiceDeploymentStorage(ServiceDeploymentRepository repository) {
        this.serviceDeploymentRepository = repository;
    }

    /**
     * Store the entity to the database and flush the data immediately.
     *
     * @param serviceDeploymentEntity the entity of service.
     * @return serviceDeploymentEntity the entity of service.
     */
    @Override
    public ServiceDeploymentEntity storeAndFlush(ServiceDeploymentEntity serviceDeploymentEntity) {
        return this.serviceDeploymentRepository.saveAndFlush(serviceDeploymentEntity);
    }

    /**
     * Method to list database entries based ServiceDeploymentEntity.
     *
     * @param serviceQuery query model for search service deployment entity.
     * @return Returns the database entry for the provided arguments.
     */
    @Override
    public List<ServiceDeploymentEntity> listServices(
            ServiceQueryModel serviceQuery) {

        Specification<ServiceDeploymentEntity> specification =
                (root, query, criteriaBuilder) -> {
                    List<Predicate> predicateList = new ArrayList<>();
                    if (Objects.nonNull(serviceQuery.getServiceTemplateId())) {
                        predicateList.add(criteriaBuilder.equal(root.get("serviceTemplateId"),
                                serviceQuery.getServiceTemplateId()));
                    }
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

                    if (Objects.nonNull(serviceQuery.getUserId())) {
                        predicateList.add(criteriaBuilder.equal(root.get("userId"),
                                serviceQuery.getUserId()));
                    }

                    if (Objects.nonNull(serviceQuery.getNamespace())) {
                        predicateList.add(criteriaBuilder.equal(root.get("namespace"),
                                serviceQuery.getNamespace()));
                    }

                    query.where(criteriaBuilder.and(predicateList.toArray(new Predicate[0])))
                            .getRestriction();

                    query.orderBy(criteriaBuilder.desc(root.get("createTime")));

                    return query.getRestriction();
                };

        return serviceDeploymentRepository.findAll(specification);
    }

    /**
     * Get detail of deployed service using ID.
     *
     * @param id the ID of deployed service.
     * @return serviceDeploymentEntity
     */
    @Override
    public ServiceDeploymentEntity findServiceDeploymentById(UUID id) {
        Optional<ServiceDeploymentEntity> optional =
                this.serviceDeploymentRepository.findById(id);
        return optional.orElse(null);
    }

    @Override
    public void deleteServiceDeployment(ServiceDeploymentEntity serviceDeploymentEntity) {
        this.serviceDeploymentRepository.delete(serviceDeploymentEntity);
    }
}
