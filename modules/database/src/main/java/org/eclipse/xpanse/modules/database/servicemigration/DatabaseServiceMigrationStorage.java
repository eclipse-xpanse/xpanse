/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.servicemigration;

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
 * Implementation of the ServiceMigrationStorage.
 */
@Component
@Transactional
public class DatabaseServiceMigrationStorage implements ServiceMigrationStorage {

    private final ServiceMigrationRepository serviceMigrationRepository;

    @Autowired
    public DatabaseServiceMigrationStorage(
            ServiceMigrationRepository serviceMigrationRepository) {
        this.serviceMigrationRepository = serviceMigrationRepository;
    }

    /**
     * Store the entity to the database and flush the data immediately.
     *
     * @param serviceMigrationEntity the entity of serviceMigration.
     * @return ServiceMigrationEntity the entity of serviceMigration.
     */
    @Override
    public ServiceMigrationEntity storeAndFlush(ServiceMigrationEntity serviceMigrationEntity) {
        return serviceMigrationRepository.saveAndFlush(serviceMigrationEntity);
    }

    /**
     * Method to list database entries based ServiceMigrationEntity.
     *
     * @param serviceMigrationQuery query model for search Service Migration entity.
     * @return Returns the database entry for the provided arguments.
     */
    @Override
    public List<ServiceMigrationEntity> listServiceMigrations(
            ServiceMigrationQueryModel serviceMigrationQuery) {

        Specification<ServiceMigrationEntity> specification =
                (root, query, criteriaBuilder) -> {
                    List<Predicate> predicateList = new ArrayList<>();
                    if (Objects.nonNull(serviceMigrationQuery.getMigrationId())) {
                        predicateList.add(criteriaBuilder.equal(root.get("migrationId"),
                                serviceMigrationQuery.getMigrationId()));
                    }
                    if (Objects.nonNull(serviceMigrationQuery.getOldServiceId())) {
                        predicateList.add(criteriaBuilder.equal(root.get("oldServiceId"),
                                serviceMigrationQuery.getOldServiceId()));
                    }
                    if (Objects.nonNull(serviceMigrationQuery.getNewServiceId())) {
                        predicateList.add(criteriaBuilder.equal(root.get("newServiceId"),
                                serviceMigrationQuery.getNewServiceId()));

                    }
                    if (Objects.nonNull(serviceMigrationQuery.getMigrationStatus())) {
                        predicateList.add(criteriaBuilder.equal(root.get("migrationStatus"),
                                serviceMigrationQuery.getMigrationStatus()));
                    }
                    if (StringUtils.isNotBlank(serviceMigrationQuery.getUserId())) {
                        predicateList.add(criteriaBuilder.equal(root.get("userId"),
                                StringUtils.lowerCase(serviceMigrationQuery.getUserId())));
                    }

                    return query.where(criteriaBuilder.and(predicateList.toArray(new Predicate[0])))
                            .getRestriction();
                };

        return serviceMigrationRepository.findAll(specification);
    }

    /**
     * Get detail of Service Migration using ID.
     *
     * @param id the ID of Service Migration.
     * @return serviceMigrationEntity
     */
    @Override
    public ServiceMigrationEntity findServiceMigrationById(UUID id) {
        Optional<ServiceMigrationEntity> optional = serviceMigrationRepository.findById(id);
        return optional.orElse(null);
    }
}
