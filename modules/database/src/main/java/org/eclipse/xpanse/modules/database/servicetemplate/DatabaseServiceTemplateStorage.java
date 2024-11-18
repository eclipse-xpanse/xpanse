/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.servicetemplate;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateNotRegistered;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Bean to manage all service task to database.
 */
@Slf4j
@Component
@Transactional
public class DatabaseServiceTemplateStorage implements ServiceTemplateStorage {

    private final ServiceTemplateRepository repository;

    @Autowired
    public DatabaseServiceTemplateStorage(ServiceTemplateRepository serviceTemplateRepository) {
        this.repository = serviceTemplateRepository;
    }

    /**
     * Add or update managed service data to database.
     *
     * @param serviceTemplateEntity the model of registered service.
     */
    @Override
    public ServiceTemplateEntity storeAndFlush(ServiceTemplateEntity serviceTemplateEntity) {
        return repository.saveAndFlush(serviceTemplateEntity);
    }

    /**
     * Method to list database entry based ServiceTemplateEntity by query model.
     *
     * @param serviceTemplateEntity ServiceTemplateEntity.
     * @return Returns the database entry for the provided arguments.
     */
    @Override
    public ServiceTemplateEntity findServiceTemplate(
            ServiceTemplateEntity serviceTemplateEntity) {
        Specification<ServiceTemplateEntity> specification =
                (root, query, criteriaBuilder) -> {
                    List<Predicate> predicateList = new ArrayList<>();
                    predicateList.add(criteriaBuilder.equal(root.get("csp"),
                            serviceTemplateEntity.getCsp()));
                    predicateList.add(criteriaBuilder.equal(root.get("name"),
                            StringUtils.lowerCase(serviceTemplateEntity.getName())));
                    predicateList.add(criteriaBuilder.equal(root.get("version"),
                            StringUtils.lowerCase(serviceTemplateEntity.getVersion())));
                    predicateList.add(criteriaBuilder.equal(root.get("category"),
                            serviceTemplateEntity.getCategory()));
                    predicateList.add(criteriaBuilder.equal(root.get("serviceHostingType"),
                            serviceTemplateEntity.getServiceHostingType()));
                    assert query != null;
                    return query.where(criteriaBuilder.and(predicateList.toArray(new Predicate[0])))
                            .getRestriction();
                };
        Optional<ServiceTemplateEntity> optional = repository.findOne(specification);
        return optional.orElse(null);

    }

    /**
     * Method to list database entry based ServiceTemplateEntity.
     *
     * @param serviceQuery query model for search register service entity.
     * @return Returns the database entry for the provided arguments.
     */
    @Override
    public List<ServiceTemplateEntity> listServiceTemplates(
            ServiceTemplateQueryModel serviceQuery) {

        Specification<ServiceTemplateEntity> specification =
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
                    if (StringUtils.isNotBlank(serviceQuery.getNamespace())) {
                        predicateList.add(criteriaBuilder.equal(root.get("namespace"),
                                serviceQuery.getNamespace()));
                    }
                    if (Objects.nonNull(serviceQuery.getServiceHostingType())) {
                        predicateList.add(criteriaBuilder.equal(root.get("serviceHostingType"),
                                serviceQuery.getServiceHostingType()));
                    }
                    if (Objects.nonNull(serviceQuery.getServiceTemplateRegistrationState())) {
                        predicateList.add(criteriaBuilder.equal(
                                root.get("serviceTemplateRegistrationState"),
                                serviceQuery.getServiceTemplateRegistrationState()));
                    }
                    if (Objects.nonNull(serviceQuery.getAvailableInCatalog())) {
                        predicateList.add(criteriaBuilder.equal(root.get("availableInCatalog"),
                                        serviceQuery.getAvailableInCatalog()));
                    }
                    if (Objects.nonNull(serviceQuery.getIsUpdatePending())) {
                        predicateList.add(criteriaBuilder.equal(root.get("isUpdatePending"),
                                        serviceQuery.getIsUpdatePending()));
                    }
                    assert query != null;
                    return query.where(criteriaBuilder.and(predicateList.toArray(new Predicate[0])))
                            .getRestriction();
                };

        return repository.findAll(specification);
    }

    /**
     * Method to get database entry based ServiceTemplateEntity.
     *
     * @param uuid uuid of ServiceTemplateEntity.
     * @return Returns the database entry for the provided arguments.
     */
    @Override
    public ServiceTemplateEntity getServiceTemplateById(UUID uuid) {
        Optional<ServiceTemplateEntity> optional = repository.findById(uuid);
        return optional.orElseThrow(() -> new ServiceTemplateNotRegistered(
                String.format("Service template with id %s not found.", uuid)
        ));
    }

    /**
     * Remove register service entity from database by entity.
     *
     * @param serviceTemplateEntity register service entity
     */
    @Override
    public void deleteServiceTemplate(ServiceTemplateEntity serviceTemplateEntity) {
        repository.delete(serviceTemplateEntity);
    }
}
