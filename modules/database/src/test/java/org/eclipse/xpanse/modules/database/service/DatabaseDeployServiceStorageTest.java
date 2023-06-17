/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.deploy.CreateRequest;
import org.eclipse.xpanse.modules.models.service.deploy.DeployResource;
import org.eclipse.xpanse.modules.models.service.deploy.DeployResult;
import org.eclipse.xpanse.modules.models.service.deploy.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.models.service.register.Ocl;
import org.eclipse.xpanse.modules.models.service.utils.OclLoader;
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

        DeployResource deployResource = new DeployResource();
        deployResource.setResourceId("11111122222222333333333");
        deployResource.setName("kafka-instance");
        deployResource.setKind(DeployResourceKind.VM);
        deployResource.setProperties(property);

        DeployServiceEntity deployServiceEntity = new DeployServiceEntity();
        deployServiceEntity.setVersion("V1.3");
        deployServiceEntity.setName("kafka");
        deployServiceEntity.setId(UUID.randomUUID());
        deployServiceEntity.setCsp(Csp.HUAWEI);
        deployServiceEntity.setFlavor("simple-slave");

        List<DeployResource> deployResources = new ArrayList<>();
        deployResources.add(deployResource);
        DeployResult deployResult = new DeployResult();
        deployResult.setResources(deployResources);
        CreateRequest request = new CreateRequest();
        request.setOcl(ocl);
        this.entityManager.persist(deployServiceEntity);
    }
}
