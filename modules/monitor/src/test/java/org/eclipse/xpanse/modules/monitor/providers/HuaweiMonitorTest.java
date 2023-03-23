/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.monitor.providers;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.xpanse.modules.database.service.DeployResourceEntity;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.models.resource.Ocl;
import org.eclipse.xpanse.modules.models.service.CreateRequest;
import org.eclipse.xpanse.modules.models.service.MonitorDataResponse;
import org.eclipse.xpanse.modules.models.service.MonitorResource;
import org.eclipse.xpanse.modules.models.utils.OclLoader;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Test for monitor.
 */
class HuaweiMonitorTest {

    @Disabled
    @Test
    public void monitorTest() throws Exception {
        OclLoader oclLoader = new OclLoader();
        Ocl ocl = oclLoader.getOcl(new URL("file:./target/test-classes/ocl_test.yaml"));
        Map<String, String> request = new HashMap<>();
        request.put("secgroup_id", "db14f9c5-9ac5-4250-9af5-3f5ade225de4");
        Boolean monitorAgentEnabled = false;
        String fromTime = "2023-03-20 20:34:47";
        String toTime = "2023-03-20 20:44:47";
        DeployServiceEntity deployServiceEntity = new DeployServiceEntity();
        CreateRequest createRequest = new CreateRequest();
        List<DeployResourceEntity> deployResourceEntityList = new ArrayList<>();
        DeployResourceEntity deployResourceEntity = new DeployResourceEntity();
        Map<String, String> property = new HashMap<>();
        property.put("region", "cn-north-9");
        deployResourceEntity.setResourceId("a4f35558-4e64-498f-ba98-7cd56e55a2dc");
        deployResourceEntity.setProperty(property);
        deployResourceEntityList.add(deployResourceEntity);
        deployServiceEntity.setDeployResourceList(deployResourceEntityList);
        createRequest.setOcl(ocl);
        createRequest.setProperty(request);
        deployServiceEntity.setCreateRequest(createRequest);

        MonitorResource monitorResource = new MonitorResource();
        HuaweiMonitor monitor = new HuaweiMonitor();
        List<MonitorDataResponse> cpu = monitor.cpuUsage(deployServiceEntity, monitorAgentEnabled,
                fromTime, toTime);
        List<MonitorDataResponse> mem = monitor.memUsage(deployServiceEntity, monitorAgentEnabled,
                fromTime, toTime);
        monitorResource.setCpu(cpu);
        monitorResource.setMem(mem);
        System.out.println(monitorResource);
    }
}