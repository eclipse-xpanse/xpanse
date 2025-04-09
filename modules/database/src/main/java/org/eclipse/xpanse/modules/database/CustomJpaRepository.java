/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

/** Custom repository interface for database operations. */
@NoRepositoryBean
public interface CustomJpaRepository<T, ID> extends Repository<T, ID> {
    Optional<T> findById(ID id);

    <S extends T> S saveAndFlush(S entity);

    void deleteAllInBatch(Iterable<T> entities);

    void deleteById(ID id);

    boolean existsById(ID id);

    void delete(T entity);

    long count();

    List<T> findAllById(Iterable<ID> ids);

    <S extends T> List<S> saveAll(Iterable<S> entities);
}
