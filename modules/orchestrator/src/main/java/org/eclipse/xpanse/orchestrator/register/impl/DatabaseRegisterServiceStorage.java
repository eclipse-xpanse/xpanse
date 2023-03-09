/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator.register.impl;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.database.register.RegisterServiceEntity;
import org.eclipse.xpanse.modules.database.register.RegisterServiceRepository;
import org.eclipse.xpanse.modules.models.query.RegisteredServiceQuery;
import org.eclipse.xpanse.orchestrator.register.RegisterServiceStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

/**
 * Bean to manage all service task to database.
 */
@Component
public class DatabaseRegisterServiceStorage implements RegisterServiceStorage {

    private final RegisterServiceRepository repository;

    @Autowired
    public DatabaseRegisterServiceStorage(RegisterServiceRepository registerServiceRepository) {
        this.repository = registerServiceRepository;
    }

    /**
     * Add or update managed service data to database.
     *
     * @param registerServiceEntity the model of registered service.
     */
    @Override
    public void store(RegisterServiceEntity registerServiceEntity) {
        repository.save(registerServiceEntity);
    }

    /**
     * Method to list database entry based registerServiceEntity by query model.
     *
     * @param registerServiceEntity registerServiceEntity.
     * @return Returns the database entry for the provided arguments.
     */
    @Override
    public RegisterServiceEntity findRegisteredService(
            RegisterServiceEntity registerServiceEntity) {
        Specification<RegisterServiceEntity> specification =
                ((root, query, criteriaBuilder) -> {
                    List<Predicate> predicateList = new ArrayList<>();
                    predicateList.add(criteriaBuilder.equal(root.get("csp"),
                            registerServiceEntity.getCsp()));
                    predicateList.add(criteriaBuilder.equal(root.get("name"),
                            StringUtils.lowerCase(registerServiceEntity.getName())));
                    predicateList.add(criteriaBuilder.equal(root.get("version"),
                            StringUtils.lowerCase(registerServiceEntity.getVersion())));
                    predicateList.add(criteriaBuilder.equal(root.get("category"),
                            registerServiceEntity.getCategory()));
                    return query.where(criteriaBuilder.and(predicateList.toArray(new Predicate[0])))
                            .getRestriction();
                });
        Optional<RegisterServiceEntity> optional = repository.findOne(specification);
        return optional.orElse(null);

    }

    /**
     * Method to list database entry based registerServiceEntity.
     *
     * @param serviceQuery query model for search register service entity.
     * @return Returns the database entry for the provided arguments.
     */
    @Override
    public List<RegisterServiceEntity> queryRegisteredServices(
            RegisteredServiceQuery serviceQuery) {

        Specification<RegisterServiceEntity> specification =
                ((root, query, criteriaBuilder) -> {
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

                    return query.where(criteriaBuilder.and(predicateList.toArray(new Predicate[0])))
                            .getRestriction();
                });

        return repository.findAll(specification);
    }

    /**
     * Method to get database entry based registerServiceEntity.
     *
     * @param uuid uuid of registerServiceEntity.
     * @return Returns the database entry for the provided arguments.
     */
    @Override
    public RegisterServiceEntity getRegisterServiceById(UUID uuid) {
        Optional<RegisterServiceEntity> optional = repository.findById(uuid);
        return optional.orElse(null);
    }

    /**
     * Method to get all stored database entries.
     *
     * @return Returns all rows from the service status database table.
     */
    @Override
    public List<RegisterServiceEntity> services() {
        return repository.findAll();
    }

    /**
     * Remove register service entity from database by uuid.
     *
     * @param uuid uuid of register service entity
     */
    @Override
    public void removeById(UUID uuid) {
        if (repository.existsById(uuid)) {
            repository.deleteById(uuid);
        } else {
            throw new IllegalStateException(
                    String.format("Registered service %s not existed.", uuid));
        }

    }

    /**
     * Remove register service entity from database by entity.
     *
     * @param registerServiceEntity register service entity
     */
    @Override
    public void remove(RegisterServiceEntity registerServiceEntity) {
        repository.delete(registerServiceEntity);
    }
}
