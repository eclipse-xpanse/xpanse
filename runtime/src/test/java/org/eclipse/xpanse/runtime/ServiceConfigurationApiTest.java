/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.runtime;

import static org.eclipse.xpanse.modules.logging.LoggingKeyConstant.HEADER_TRACKING_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.c4_soft.springaddons.security.oauth2.test.annotations.WithJwt;
import com.fasterxml.jackson.core.type.TypeReference;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.eclipse.xpanse.modules.database.resource.DeployResourceEntity;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.database.serviceconfiguration.update.ServiceConfigurationUpdateRequest;
import org.eclipse.xpanse.modules.deployment.DeployServiceEntityHandler;
import org.eclipse.xpanse.modules.models.response.Response;
import org.eclipse.xpanse.modules.models.service.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.models.service.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.models.service.order.ServiceOrder;
import org.eclipse.xpanse.modules.models.service.statemanagement.enums.ServiceState;
import org.eclipse.xpanse.modules.models.serviceconfiguration.ServiceConfigurationUpdate;
import org.eclipse.xpanse.modules.models.serviceconfiguration.enums.ServiceConfigurationStatus;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
import org.eclipse.xpanse.modules.models.servicetemplate.view.ServiceTemplateDetailVo;
import org.eclipse.xpanse.runtime.util.ApisTestCommon;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Test for ServiceConfigurationApi.
 */
@SuppressWarnings("unchecked")
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {"spring.profiles.active=oauth,zitadel,zitadel-testbed"})
@AutoConfigureMockMvc
class ServiceConfigurationApiTest extends ApisTestCommon {

    private static final Logger log = LoggerFactory.getLogger(ServiceConfigurationApiTest.class);

    public static final String KAFKA_CFG_MESSAGE_MAX_BYTES = "kafka_cfg_message_max_bytes";
    public static final Integer KAFKA_CFG_MESSAGE_MAX_BYTES_VALUE = 2048;

    public static final String KAFKA_CFG_LOG_DIRS = "kafka_cfg_log_dirs";
    public static final String KAFKA_CFG_LOG_DIRS_VALUE = "/var/lib/kafka/logs2";

    public static final String KAFKA_CFG_NUM_IO_THREADS = "kafka_cfg_num_io_threads";
    public static final Integer KAFKA_CFG_NUM_IO_THREADS_VALUE = 2;

    public static final String KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR = "kafka_offsets_topic_replication_factor";
    public static final Integer KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR_VALUE = 2;

    public static final String KAFKA_LOG_FLUSH_INTERVAL_MESSAGES = "kafka_log_flush_interval_messages";
    public static final Integer KAFKA_LOG_FLUSH_INTERVAL_MESSAGES_VALUE = 10000;

    public static final String ZOOKEEPER_SNAP_COUNT = "zookeeper_snap_count";
    public static final Integer ZOOKEEPER_SNAP_COUNT_VALUE = 1;

    public static final String ZOOKEEPER_GLOBAL_OUTSTANDING_LIMIT = "zookeeper_global_outstanding_limit";
    public static final Integer ZOOKEEPER_GLOBAL_OUTSTANDING_LIMIT_VALUE = 10000;

    public static final String HUAWEI_CLOUD_COMPUTE_INSTANCE = "huaweicloud_compute_instance";

    public static final String ZOOKEEPER = "zookeeper";

    public static final String KAFKA_BROKER = "kafka-broker";
    public static final String USER_ID = "userId";

    @MockBean
    DeployServiceEntityHandler deployServiceEntityHandler;

    @Test
    @WithJwt(file = "jwt_all_roles.json")
    void test_change_service_configuration_success() throws Exception {
        DeployServiceEntity deployServiceEntity = test_register_and_deploy_service_well();
        test_change_service_configuration_well(deployServiceEntity);
    }

    private void test_change_service_configuration_well(DeployServiceEntity deployServiceEntity) throws Exception {
        when(deployServiceEntityHandler.getDeployServiceEntity(any())).thenReturn(deployServiceEntity);
        ServiceOrder order  = changeServiceConfiguration(deployServiceEntity.getId());
        assertEquals(deployServiceEntity.getId(), order.getServiceId());
        List<ServiceConfigurationUpdateRequest> requests =
                listServiceConfigurationUpdateRequests(order.getOrderId(), order.getServiceId());
        assertFalse(requests.isEmpty());
        assertEquals(requests.size(), 4);
        requests.forEach(request -> assertEquals(request.getServiceOrderEntity().getOrderId(), order.getOrderId()));
        requests.forEach(request -> assertEquals(request.getDeployServiceEntity().getId(), order.getServiceId()));
        requests.forEach(request -> assertEquals(request.getStatus(), ServiceConfigurationStatus.PENDING));
        requests.forEach(request -> {
            if(ZOOKEEPER.equals(request.getConfigManager())){
                assertEquals(request.getProperties(),getZookeeperConfig());
            }
            if(KAFKA_BROKER.equals(request.getConfigManager())){
                assertEquals(request.getProperties(),getKafkaBrokerConfig());
            }
        });
    }

    DeployServiceEntity test_register_and_deploy_service_well() throws Exception {
        // Setup
        Ocl ocl = new OclLoader().getOcl(
                URI.create("file:src/test/resources/ocl_terraform_kafka_test.yml").toURL());
        ServiceTemplateDetailVo serviceTemplate = registerServiceTemplate(ocl);
        if (Objects.isNull(serviceTemplate)) {
            log.error("Register service template failed.");
            return null;
        }
        approveServiceTemplateRegistration(serviceTemplate.getServiceTemplateId());

        return saveDeployServiceEntity(ocl,serviceTemplate.getServiceTemplateId());
    }

    DeployServiceEntity saveDeployServiceEntity(Ocl ocl, UUID serviceTemplateId){
        DeployServiceEntity entity = new DeployServiceEntity();
        entity.setId(UUID.randomUUID());
        entity.setCsp(ocl.getCloudServiceProvider().getName());
        entity.setUserId(USER_ID);
        entity.setCategory(ocl.getCategory());
        entity.setName(ocl.getName());
        entity.setCustomerServiceName(ocl.getName());
        entity.setVersion(ocl.getVersion());
        entity.setServiceTemplateId(serviceTemplateId);
        entity.setServiceDeploymentState(ServiceDeploymentState.DEPLOY_SUCCESS);
        entity.setServiceState(ServiceState.RUNNING);
        entity.setDeployResourceList(getDeployResources());
        deployServiceStorage.storeAndFlush(entity);
        return entity;
    }

    Map<String, Object> getZookeeperConfig() {
        Map<String, Object> configuration = new HashMap<>();
        configuration.put(ZOOKEEPER_SNAP_COUNT, ZOOKEEPER_SNAP_COUNT_VALUE);
        configuration.put(ZOOKEEPER_GLOBAL_OUTSTANDING_LIMIT, ZOOKEEPER_GLOBAL_OUTSTANDING_LIMIT_VALUE);
        return configuration;
    }

    Map<String, Object> getKafkaBrokerConfig() {
        Map<String, Object> configuration = new HashMap<>();
        configuration.put(KAFKA_CFG_MESSAGE_MAX_BYTES, KAFKA_CFG_MESSAGE_MAX_BYTES_VALUE);
        configuration.put(KAFKA_CFG_LOG_DIRS, KAFKA_CFG_LOG_DIRS_VALUE);
        configuration.put(KAFKA_CFG_NUM_IO_THREADS, KAFKA_CFG_NUM_IO_THREADS_VALUE);
        configuration.put(KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR, KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR_VALUE);
        configuration.put(KAFKA_LOG_FLUSH_INTERVAL_MESSAGES, KAFKA_LOG_FLUSH_INTERVAL_MESSAGES_VALUE);
        return configuration;
    }

    ServiceConfigurationUpdate getServiceConfigurationUpdate() {
        ServiceConfigurationUpdate request = new ServiceConfigurationUpdate();
        Map<String, Object> configuration = getKafkaBrokerConfig();
        configuration.putAll(getZookeeperConfig());
        request.setConfiguration(configuration);
        return request;
    }

    List<DeployResourceEntity> getDeployResources() {
        List<DeployResourceEntity> deployResources = new ArrayList<>();
        DeployResourceEntity zookeeperResource = new DeployResourceEntity();
        zookeeperResource.setId(UUID.randomUUID());
        zookeeperResource.setGroupType(HUAWEI_CLOUD_COMPUTE_INSTANCE);
        zookeeperResource.setGroupName(ZOOKEEPER);
        zookeeperResource.setResourceId(UUID.randomUUID().toString());
        zookeeperResource.setResourceName("kafka-zookeeper"+UUID.randomUUID());
        zookeeperResource.setResourceKind(DeployResourceKind.VM);
        deployResources.add(zookeeperResource);
        for(int i=0; i<3; i++){
            DeployResourceEntity kafkaResource = new DeployResourceEntity();
            kafkaResource.setId(UUID.randomUUID());
            kafkaResource.setGroupType(HUAWEI_CLOUD_COMPUTE_INSTANCE);
            kafkaResource.setGroupName(KAFKA_BROKER);
            kafkaResource.setResourceId(UUID.randomUUID().toString());
            kafkaResource.setResourceName(KAFKA_BROKER+"-"+UUID.randomUUID());
            kafkaResource.setResourceKind(DeployResourceKind.VM);
            deployResources.add(kafkaResource);
        }
        return deployResources;
    }


    ServiceOrder changeServiceConfiguration(UUID serviceId) throws Exception {
        MockHttpServletResponse response = mockMvc.perform(
                        put("/xpanse/services/config/{serviceId}", serviceId)
                                .content(objectMapper.writeValueAsString(getServiceConfigurationUpdate()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertNotNull(response.getHeader(HEADER_TRACKING_ID));
        if (response.getStatus() == HttpStatus.OK.value()) {
            ServiceOrder order = objectMapper.readValue(response.getContentAsString(), ServiceOrder.class);
            assertNotNull(order);
            assertEquals(order.getServiceId(), serviceId);
            return order;
        } else {
            Response responseError = objectMapper.readValue(response.getContentAsString(), Response.class);
            log.error("Change service configuration failed. Error: " + responseError.getDetails());
            return null;
        }
    }

    List<ServiceConfigurationUpdateRequest> listServiceConfigurationUpdateRequests(UUID orderId,UUID serviceId)
            throws Exception {

        final MockHttpServletResponse listResponse = mockMvc.perform(
                        get("/xpanse/services/config/list")
                                .param("orderId", orderId.toString())
                                .param("serviceId", serviceId.toString())
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        assertEquals(HttpStatus.OK.value(), listResponse.getStatus());
        assertNotNull(listResponse.getHeader(HEADER_TRACKING_ID));
        return objectMapper.readValue(listResponse.getContentAsString(),
                new TypeReference<List<ServiceConfigurationUpdateRequest>>() {});
    }
}
