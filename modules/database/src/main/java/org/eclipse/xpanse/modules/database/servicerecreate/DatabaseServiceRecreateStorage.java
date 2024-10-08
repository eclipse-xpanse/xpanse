/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.servicerecreate;

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
 * Implementation of the ServiceRecreateStorage.
 */
@Component
@Transactional
public class DatabaseServiceRecreateStorage implements ServiceRecreateStorage {

    private final ServiceRecreateRepository serviceRecreateRepository;

    @Autowired
    public DatabaseServiceRecreateStorage(
            ServiceRecreateRepository serviceRecreateRepository) {
        this.serviceRecreateRepository = serviceRecreateRepository;
    }

    /**
     * Store the entity to the database and flush the data immediately.
     *
     * @param serviceRecreateEntity the entity of serviceRecreate.
     * @return ServiceRecreateEntity the entity of serviceRecreate.
     */
    @Override
    public ServiceRecreateEntity storeAndFlush(ServiceRecreateEntity serviceRecreateEntity) {
        return serviceRecreateRepository.saveAndFlush(serviceRecreateEntity);
    }

    /**
     * Method to list database entries based ServiceRecreateEntity.
     *
     * @param serviceRecreateQuery query model for search Service Recreate entity.
     * @return Returns the database entry for the provided arguments.
     */
    @Override
    public List<ServiceRecreateEntity> listServiceRecreates(
            ServiceRecreateQueryModel serviceRecreateQuery) {

        Specification<ServiceRecreateEntity> specification =
                (root, query, criteriaBuilder) -> {
                    List<Predicate> predicateList = new ArrayList<>();
                    if (Objects.nonNull(serviceRecreateQuery.getRecreateId())) {
                        predicateList.add(criteriaBuilder.equal(root.get("recreateId"),
                                serviceRecreateQuery.getRecreateId()));
                    }
                    if (Objects.nonNull(serviceRecreateQuery.getServiceId())) {
                        predicateList.add(criteriaBuilder.equal(root.get("serviceId"),
                                serviceRecreateQuery.getServiceId()));
                    }
                    if (Objects.nonNull(serviceRecreateQuery.getRecreateStatus())) {
                        predicateList.add(criteriaBuilder.equal(root.get("recreateStatus"),
                                serviceRecreateQuery.getRecreateStatus()));
                    }
                    if (StringUtils.isNotBlank(serviceRecreateQuery.getUserId())) {
                        predicateList.add(criteriaBuilder.equal(root.get("userId"),
                                StringUtils.lowerCase(serviceRecreateQuery.getUserId())));
                    }

                    return query.where(criteriaBuilder.and(predicateList.toArray(new Predicate[0])))
                            .getRestriction();
                };

        return serviceRecreateRepository.findAll(specification);
    }

    /**
     * Get detail of Service Recreate using ID.
     *
     * @param id the ID of Service Recreate.
     * @return serviceRecreateEntity
     */
    @Override
    public ServiceRecreateEntity findServiceRecreateById(UUID id) {
        Optional<ServiceRecreateEntity> optional = serviceRecreateRepository.findById(id);
        return optional.orElse(null);
    }
}
