/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.serviceconfiguration.update;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


/**
 * Implementation of the ServiceConfigurationUpdateStorage.
 */
@Component
@Transactional
public class DatabaseServiceConfigurationChangeDetailsStorage
        implements ServiceConfigurationChangeDetailsStorage {

    private final ServiceConfigurationChangeDetailsRepository repository;

    @Autowired
    public DatabaseServiceConfigurationChangeDetailsStorage(
            ServiceConfigurationChangeDetailsRepository repository) {
        this.repository = repository;
    }

    @Override
    public ServiceConfigurationChangeDetailsEntity storeAndFlush(
            ServiceConfigurationChangeDetailsEntity serviceConfigurationChangeDetailsEntity) {
        return repository.saveAndFlush(serviceConfigurationChangeDetailsEntity);
    }

    @Override
    public <S extends ServiceConfigurationChangeDetailsEntity>
            List<S> saveAll(Iterable<S> entities) {
        return repository.saveAll(entities);
    }

    @Override
    public List<ServiceConfigurationChangeDetailsEntity> listServiceConfigurationChangeDetails(
            ServiceConfigurationChangeDetailsQueryModel requestQuery) {
        Specification<ServiceConfigurationChangeDetailsEntity> specification =
                (root, query, criteriaBuilder) -> {
                    List<Predicate> predicateList = new ArrayList<>();
                    if (Objects.nonNull(requestQuery.getOrderId())) {
                        predicateList.add(criteriaBuilder.equal(
                                root.get("serviceOrderEntity").get("orderId"),
                                requestQuery.getOrderId()));
                    }

                    if (Objects.nonNull(requestQuery.getServiceId())) {
                        predicateList.add(criteriaBuilder.equal(
                                root.get("deployServiceEntity").get("id"),
                                requestQuery.getServiceId()));
                    }
                    if (StringUtils.isNotBlank(requestQuery.getResourceName())) {
                        predicateList.add(criteriaBuilder.equal(root.get("resourceName"),
                                StringUtils.lowerCase(requestQuery.getResourceName())));

                    }
                    if (StringUtils.isNotBlank(requestQuery.getConfigManager())) {
                        predicateList.add(criteriaBuilder.equal(root.get("configManager"),
                                StringUtils.lowerCase(requestQuery.getConfigManager())));
                    }
                    if (Objects.nonNull(requestQuery.getStatus())) {
                        predicateList.add(criteriaBuilder.equal(root.get("status"),
                                requestQuery.getStatus()));
                    }
                    return query.where(criteriaBuilder.and(predicateList.toArray(new Predicate[0])))
                            .getRestriction();
                };
        return repository.findAll(specification);
    }

    @Override
    public ServiceConfigurationChangeDetailsEntity findById(UUID changeId) {
        return repository.findById(changeId).orElse(null);
    }
}
