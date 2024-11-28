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

    @Override
    public List<ServiceTemplateEntity> listServiceTemplates(ServiceTemplateQueryModel queryModel) {

        Specification<ServiceTemplateEntity> specification =
                (root, query, criteriaBuilder) -> {
                    List<Predicate> predicateList = new ArrayList<>();
                    if (Objects.nonNull(queryModel.getCategory())) {
                        predicateList.add(criteriaBuilder.equal(root.get("category"),
                                queryModel.getCategory()));
                    }
                    if (Objects.nonNull(queryModel.getCsp())) {
                        predicateList.add(criteriaBuilder.equal(root.get("csp"),
                                queryModel.getCsp()));
                    }
                    if (StringUtils.isNotBlank(queryModel.getServiceName())) {
                        predicateList.add(criteriaBuilder.equal(root.get("name"),
                                StringUtils.lowerCase(queryModel.getServiceName())));

                    }
                    if (StringUtils.isNotBlank(queryModel.getServiceVersion())) {
                        predicateList.add(criteriaBuilder.equal(root.get("version"),
                                StringUtils.lowerCase(queryModel.getServiceVersion())));
                    }
                    if (StringUtils.isNotBlank(queryModel.getNamespace())) {
                        predicateList.add(criteriaBuilder.equal(root.get("namespace"),
                                queryModel.getNamespace()));
                    }
                    if (Objects.nonNull(queryModel.getServiceHostingType())) {
                        predicateList.add(criteriaBuilder.equal(root.get("serviceHostingType"),
                                queryModel.getServiceHostingType()));
                    }
                    if (Objects.nonNull(queryModel.getServiceTemplateRegistrationState())) {
                        predicateList.add(criteriaBuilder.equal(
                                root.get("serviceTemplateRegistrationState"),
                                queryModel.getServiceTemplateRegistrationState()));
                    }
                    if (Objects.nonNull(queryModel.getAvailableInCatalog())) {
                        predicateList.add(criteriaBuilder.equal(root.get("availableInCatalog"),
                                queryModel.getAvailableInCatalog()));
                    }
                    if (Objects.nonNull(queryModel.getIsUpdatePending())) {
                        predicateList.add(criteriaBuilder.equal(root.get("isUpdatePending"),
                                queryModel.getIsUpdatePending()));
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
