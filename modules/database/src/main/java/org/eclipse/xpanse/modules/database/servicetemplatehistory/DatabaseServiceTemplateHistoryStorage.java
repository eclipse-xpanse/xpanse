/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.servicetemplatehistory;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implements methods of ServiceTemplateHistoryStorage interface.
 */
@Slf4j
@Component
@Transactional
public class DatabaseServiceTemplateHistoryStorage implements ServiceTemplateHistoryStorage {

    private final ServiceTemplateHistoryRepository repository;

    @Autowired
    public DatabaseServiceTemplateHistoryStorage(
            ServiceTemplateHistoryRepository serviceTemplateHistoryRepository) {
        this.repository = serviceTemplateHistoryRepository;
    }

    @Override
    public ServiceTemplateHistoryEntity storeAndFlush(
            ServiceTemplateHistoryEntity serviceTemplateHistoryEntity) {
        return repository.saveAndFlush(serviceTemplateHistoryEntity);
    }

    @Override
    public List<ServiceTemplateHistoryEntity> listServiceTemplateHistory(
            ServiceTemplateHistoryEntity queryEntity) {
        Specification<ServiceTemplateHistoryEntity> specification =
                (root, query, criteriaBuilder) -> {
                    List<Predicate> predicateList = new ArrayList<>();
                    predicateList.add(criteriaBuilder.equal(root.get("serviceTemplate").get("id"),
                            queryEntity.getServiceTemplate().getId()));
                    predicateList.add(criteriaBuilder.equal(root.get("requestType"),
                            queryEntity.getRequestType()));
                    predicateList.add(criteriaBuilder.equal(root.get("status"),
                            queryEntity.getStatus()));
                    return query.where(criteriaBuilder.and(predicateList.toArray(new Predicate[0])))
                            .getRestriction();
                };
        return repository.findAll(specification);
    }

}
