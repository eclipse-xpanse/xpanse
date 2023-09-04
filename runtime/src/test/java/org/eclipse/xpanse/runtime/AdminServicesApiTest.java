/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.api.TerraformApi;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.model.TerraformBootSystemStatus;
import org.eclipse.xpanse.modules.models.system.BackendSystemStatus;
import org.eclipse.xpanse.modules.models.system.SystemStatus;
import org.eclipse.xpanse.modules.models.system.enums.BackendSystemType;
import org.eclipse.xpanse.modules.models.system.enums.DatabaseType;
import org.eclipse.xpanse.modules.models.system.enums.HealthStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Test for AdminServicesApi.
 */
@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {"spring.profiles.active=default,terraform-boot"})
@AutoConfigureMockMvc
class AdminServicesApiTest {

    @Resource
    private MockMvc mockMvc;

    @Resource
    private TerraformApi terraformApi;

    @Test
    void testHealthCheck() throws Exception {
        // SetUp
        SystemStatus systemStatus = new SystemStatus();
        systemStatus.setHealthStatus(HealthStatus.OK);
        BackendSystemStatus dataBaseSystemStatus = new BackendSystemStatus();
        dataBaseSystemStatus.setBackendSystemType(BackendSystemType.DATABASE);
        dataBaseSystemStatus.setHealthStatus(HealthStatus.OK);
        dataBaseSystemStatus.setName(DatabaseType.H2DB.toValue());

        if (isTerraformBootApiAccessible()) {
            BackendSystemStatus terraformBootStatus = new BackendSystemStatus();
            terraformBootStatus.setBackendSystemType(BackendSystemType.TERRAFORM_BOOT);
            terraformBootStatus.setHealthStatus(HealthStatus.OK);
            terraformBootStatus.setName(BackendSystemType.TERRAFORM_BOOT.toValue());
            systemStatus.setBackendSystemStatuses(
                    List.of(dataBaseSystemStatus, terraformBootStatus));
        } else {
            systemStatus.setBackendSystemStatuses(List.of(dataBaseSystemStatus));
        }

        String resBody = new ObjectMapper().writeValueAsString(systemStatus);

        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(get("/xpanse/health")
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // Verify the results
        assertEquals(response.getStatus(), HttpStatus.OK.value());
        assertTrue(StringUtils.isNotEmpty(response.getContentAsString()));
        assertEquals(resBody, response.getContentAsString());

    }

    private boolean isTerraformBootApiAccessible() {
        try {
            TerraformBootSystemStatus terraformBootSystemStatus = terraformApi.healthCheck();
            return terraformBootSystemStatus.getHealthStatus()
                    .equals(TerraformBootSystemStatus.HealthStatusEnum.OK);
        } catch (Exception e) {
            return false;
        }
    }

}
