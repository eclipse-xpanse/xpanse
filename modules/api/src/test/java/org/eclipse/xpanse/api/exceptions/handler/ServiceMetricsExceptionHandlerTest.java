/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.api.exceptions.handler;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.eclipse.xpanse.api.config.CspPluginValidator;
import org.eclipse.xpanse.api.controllers.ServiceMetricsApi;
import org.eclipse.xpanse.modules.models.monitor.exceptions.MetricsDataNotYetAvailableException;
import org.eclipse.xpanse.modules.models.monitor.exceptions.ResourceNotFoundException;
import org.eclipse.xpanse.modules.models.monitor.exceptions.ResourceNotSupportedForMonitoringException;
import org.eclipse.xpanse.modules.monitor.ServiceMetricsAdapter;
import org.eclipse.xpanse.modules.orchestrator.OrchestratorPlugin;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.eclipse.xpanse.modules.security.IdentityProviderManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ServiceMetricsApi.class, ServiceMetricsAdapter.class,
        ServiceMetricsExceptionHandler.class, IdentityProviderManager.class, PluginManager.class,
        CspPluginValidator.class})
@WebMvcTest
class ServiceMetricsExceptionHandlerTest {

    private final String serviceId = "e034af0c-be03-453e-92cd-fd69acbfe526";
    private final String resourceId = "a034af0c-be03-453e-92cd-fd69acbfe526";
    @MockBean
    private PluginManager pluginManager;
    @MockBean
    private CspPluginValidator cspPluginValidator;
    @MockBean
    private OrchestratorPlugin orchestratorPlugin;
    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @MockBean
    private ServiceMetricsAdapter serviceMetricsAdapter;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void testResourceNotSupportedForMonitoringException() throws Exception {
        when(serviceMetricsAdapter
                .getMetricsByResourceId(resourceId, null, null, null, null, true))
                .thenThrow(new ResourceNotSupportedForMonitoringException("test error"));

        this.mockMvc.perform(get("/xpanse/metrics")
                        .param("serviceId", serviceId)
                        .param("resourceId", resourceId)
                        .param("onlyLastKnownMetric", "true"))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.errorType").value("Resource Invalid For Monitoring"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }

    @Test
    void testResourceNotFoundException() throws Exception {
        when(serviceMetricsAdapter
                .getMetricsByResourceId(resourceId, null, null, null, null, true))
                .thenThrow(new ResourceNotFoundException("test error"));

        this.mockMvc.perform(get("/xpanse/metrics")
                        .param("serviceId", serviceId)
                        .param("resourceId", resourceId)
                        .param("onlyLastKnownMetric", "true"))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.errorType").value("Resource Not Found"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }

    @Test
    void testMetricsDataNotYetAvailableException() throws Exception {
        when(serviceMetricsAdapter
                .getMetricsByResourceId(resourceId, null, null, null, null, true))
                .thenThrow(new MetricsDataNotYetAvailableException("test error"));

        this.mockMvc.perform(get("/xpanse/metrics")
                        .param("serviceId", serviceId)
                        .param("resourceId", resourceId)
                        .param("onlyLastKnownMetric", "true"))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.errorType").value("Metrics Data Not Ready"))
                .andExpect(jsonPath("$.details[0]").value("test error"));
    }
}
