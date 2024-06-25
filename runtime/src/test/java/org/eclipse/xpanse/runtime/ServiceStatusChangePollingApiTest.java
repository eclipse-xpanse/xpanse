/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.runtime;

import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.api.config.ServiceDeploymentStateConverter;
import org.eclipse.xpanse.api.controllers.ServiceStatusChangePollingApi;
import org.eclipse.xpanse.modules.async.TaskConfiguration;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.database.service.DeployServiceStorage;
import org.eclipse.xpanse.modules.deployment.polling.StatusChangePolling;
import org.eclipse.xpanse.modules.models.service.enums.ServiceDeploymentState;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@Slf4j
@ContextConfiguration(classes = {ServiceStatusChangePollingApi.class, StatusChangePolling.class, TaskConfiguration.class, ServiceDeploymentStateConverter.class})
@WebMvcTest(ServiceStatusChangePollingApi.class)
@TestPropertySource(properties = {
        "service.status.long.polling.wait.time.in.seconds=2", "service.status.long.polling.interval.in.seconds=1",
})
public class ServiceStatusChangePollingApiTest {

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    GenericConversionService genericConversionService;

    @MockBean
    DeployServiceStorage deployServiceStorage;

    MockMvc mockMvc;

    @BeforeEach
    void setup() {
        FormattingConversionService formattingConversionService=new FormattingConversionService();
        formattingConversionService.addConverter(applicationContext.getAutowireCapableBeanFactory().getBean(ServiceDeploymentStateConverter.class));
        this.mockMvc = MockMvcBuilders.standaloneSetup(applicationContext.getAutowireCapableBeanFactory().getBean(ServiceStatusChangePollingApi.class))
                .setConversionService(formattingConversionService)
                .build();
    }

    @Test
    void testPollingIsSkippedIfFinalStatus() throws Exception {
        UUID serviceId = UUID.randomUUID();
        DeployServiceEntity  deployServiceEntity = Instancio.of(DeployServiceEntity.class).set(
                Select.field(DeployServiceEntity::getServiceDeploymentState),
                ServiceDeploymentState.DEPLOY_SUCCESS).create();
        doReturn(deployServiceEntity).when(deployServiceStorage).findDeployServiceById(serviceId);
        MvcResult mvcResult = mockMvc.perform(get("/xpanse/service/deployment/status")
                        .queryParam("id",serviceId.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(request().asyncStarted())
                .andReturn();
        this.mockMvc.perform(MockMvcRequestBuilders.asyncDispatch(mvcResult))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(content().string("{\"serviceDeploymentState\":\"deployment successful\",\"isOrderCompleted\":true}"));
        verify(deployServiceStorage, times(1)).findDeployServiceById(serviceId);
    }

    @Test
    void testPollingIsSkippedIfNotFinalStatus() throws Exception {
        UUID serviceId = UUID.randomUUID();
        DeployServiceEntity  deployServiceEntity = Instancio.of(DeployServiceEntity.class).set(
                Select.field(DeployServiceEntity::getServiceDeploymentState),
                ServiceDeploymentState.DEPLOYING).create();
        doReturn(deployServiceEntity).when(deployServiceStorage).findDeployServiceById(serviceId);
        MvcResult mvcResult = mockMvc.perform(get("/xpanse/service/deployment/status")
                        .param("id",serviceId.toString())
                        .param("lastKnownServiceDeploymentState", ServiceDeploymentState.DEPLOYING.toValue())
                        .accept(MediaType.APPLICATION_JSON))
                        .andExpect(request().asyncStarted())
                .andReturn();
        this.mockMvc.perform(MockMvcRequestBuilders.asyncDispatch(mvcResult))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(content().string("{\"serviceDeploymentState\":\"deploying\",\"isOrderCompleted\":false}"));

        verify(deployServiceStorage, atLeast(2)).findDeployServiceById(serviceId);

    }
}
