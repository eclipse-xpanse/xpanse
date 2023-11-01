/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.policy;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.eclipse.xpanse.modules.models.policy.PolicyQueryRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the PoliciesStorage.
 */
@Component
@Transactional
public class DatabasePolicyStorage implements PolicyStorage {

    private final PolicyRepository policyRepository;

    @Autowired
    public DatabasePolicyStorage(PolicyRepository policyRepository) {
        this.policyRepository = policyRepository;
    }

    @Override
    public PolicyEntity store(PolicyEntity policyEntity) {
        return policyRepository.save(policyEntity);
    }

    @Override
    public List<PolicyEntity> policies() {
        return policyRepository.findAll();
    }

    @Override
    public List<PolicyEntity> listPolicies(PolicyQueryRequest queryModel) {
        Specification<PolicyEntity> specification =
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
                    predicateList.add(criteriaBuilder.equal(root.get("userId"),
                            queryModel.getUserId()));

                    return query.where(criteriaBuilder.and(predicateList.toArray(new Predicate[0])))
                            .getRestriction();
                };

        return policyRepository.findAll(specification);
    }

    @Override
    public PolicyEntity findPolicyById(UUID id) {
        Optional<PolicyEntity> optional = policyRepository.findById(id);
        return optional.orElse(null);
    }

    @Override
    public void deletePolicies(PolicyEntity policyEntity) {
        policyRepository.delete(policyEntity);
    }

    @Override
    public void deletePolicyById(UUID id) {
        policyRepository.deleteById(id);
    }
}
