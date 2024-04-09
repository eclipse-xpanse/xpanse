/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.userpolicy;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.eclipse.xpanse.modules.models.policy.userpolicy.UserPolicyQueryRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the UserPolicyStorage.
 */
@Component
@Transactional
public class DatabaseUserPolicyStorage implements UserPolicyStorage {

    private final UserPolicyRepository userPolicyRepository;

    @Autowired
    public DatabaseUserPolicyStorage(UserPolicyRepository userPolicyRepository) {
        this.userPolicyRepository = userPolicyRepository;
    }

    @Override
    public UserPolicyEntity store(UserPolicyEntity userPolicyEntity) {
        return userPolicyRepository.save(userPolicyEntity);
    }

    @Override
    public List<UserPolicyEntity> policies() {
        return userPolicyRepository.findAll();
    }

    @Override
    public List<UserPolicyEntity> listPolicies(UserPolicyQueryRequest queryModel) {
        Specification<UserPolicyEntity> specification =
                (root, query, criteriaBuilder) -> {
                    List<Predicate> predicateList = new ArrayList<>();
                    if (Objects.nonNull(queryModel.getCsp())) {
                        predicateList.add(criteriaBuilder.equal(root.get("csp"),
                                queryModel.getCsp()));
                    }
                    if (Objects.nonNull(queryModel.getEnabled())) {
                        predicateList.add(criteriaBuilder.equal(root.get("enabled"),
                                queryModel.getEnabled()));

                    }
                    if (Objects.nonNull(queryModel.getPolicy())) {
                        predicateList.add(criteriaBuilder.equal(root.get("policy"),
                                queryModel.getPolicy()));

                    }

                    if (Objects.nonNull(queryModel.getUserId())) {
                        predicateList.add(criteriaBuilder.equal(root.get("userId"),
                                queryModel.getUserId()));
                    }

                    return query.where(criteriaBuilder.and(predicateList.toArray(new Predicate[0])))
                            .getRestriction();
                };

        return userPolicyRepository.findAll(specification);
    }

    @Override
    public UserPolicyEntity findPolicyById(UUID id) {
        Optional<UserPolicyEntity> optional = userPolicyRepository.findById(id);
        return optional.orElse(null);
    }

    @Override
    public void deletePolicies(UserPolicyEntity userPolicyEntity) {
        userPolicyRepository.delete(userPolicyEntity);
    }

    @Override
    public void deletePolicyById(UUID id) {
        userPolicyRepository.deleteById(id);
    }
}
