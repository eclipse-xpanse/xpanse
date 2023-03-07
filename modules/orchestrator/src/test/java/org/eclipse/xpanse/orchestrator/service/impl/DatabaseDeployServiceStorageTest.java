/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator.service.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.eclipse.xpanse.modules.database.common.DeployResourceKind;
import org.eclipse.xpanse.modules.database.service.DeployResourceEntity;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.ocl.loader.OclLoader;
import org.eclipse.xpanse.modules.ocl.loader.data.models.Ocl;
import org.eclipse.xpanse.modules.ocl.loader.data.models.enums.Csp;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

/**
 * Test for DatabaseDeployServiceStorage.
 */
@Transactional
public class DatabaseDeployServiceStorageTest {

    @PersistenceContext
    EntityManager entityManager;

    @Disabled
    @Test
    public void basicTest() throws Exception {

        OclLoader oclLoader = new OclLoader();
        Ocl ocl = oclLoader.getOcl(new URL("file:./target/test-classes/ocl_test.yaml"));

        Map<String, String> property = new HashMap<>();
        property.put("secgroup_id", "1234567890");

        List<DeployResourceEntity> deployResourceEntityList = new ArrayList<>();
        DeployResourceEntity deployResourceEntity = new DeployResourceEntity();
        deployResourceEntity.setResourceId("11111122222222333333333");
        deployResourceEntity.setName("kafka-instance");
        deployResourceEntity.setKind(DeployResourceKind.Vm);
        deployResourceEntity.setProperty(property);
        deployResourceEntityList.add(deployResourceEntity);

        DeployServiceEntity deployServiceEntity = new DeployServiceEntity();
        deployServiceEntity.setVersion("V1.3");
        deployServiceEntity.setName("kafka");
        deployServiceEntity.setId(UUID.randomUUID());
        deployServiceEntity.setCsp(Csp.HUAWEI);
        deployServiceEntity.setFlavor("simple-slave");
        deployServiceEntity.setProperty(property);
        deployServiceEntity.setOcl(ocl);
        deployServiceEntity.setDeployResourceEntity(deployResourceEntityList);
        this.entityManager.persist(deployServiceEntity);
    }
}
