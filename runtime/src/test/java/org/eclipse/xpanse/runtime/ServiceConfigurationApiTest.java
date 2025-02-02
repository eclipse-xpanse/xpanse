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
import static org.mockito.Mockito.doNothing;
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
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.database.resource.ServiceResourceEntity;
import org.eclipse.xpanse.modules.deployment.PolicyValidator;
import org.eclipse.xpanse.modules.models.response.ErrorResponse;
import org.eclipse.xpanse.modules.models.service.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.models.service.order.ServiceOrder;
import org.eclipse.xpanse.modules.models.serviceconfiguration.ServiceChangeOrderDetails;
import org.eclipse.xpanse.modules.models.serviceconfiguration.ServiceConfigurationUpdate;
import org.eclipse.xpanse.modules.models.serviceconfiguration.enums.ServiceChangeStatus;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
import org.eclipse.xpanse.modules.models.servicetemplate.view.ServiceTemplateDetailVo;
import org.eclipse.xpanse.runtime.util.ApisTestCommon;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/** Test for ServiceConfigurationApi. */
@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {"spring.profiles.active=oauth,zitadel,zitadel-testbed,test,dev"})
@AutoConfigureMockMvc
class ServiceConfigurationApiTest extends ApisTestCommon {

    public static final String KAFKA_CFG_MESSAGE_MAX_BYTES = "kafka_cfg_message_max_bytes";
    public static final Integer KAFKA_CFG_MESSAGE_MAX_BYTES_VALUE = 2048;
    public static final String KAFKA_CFG_LOG_DIRS = "kafka_cfg_log_dirs";
    public static final String KAFKA_CFG_LOG_DIRS_VALUE = "/var/lib/kafka/logs2";
    public static final String KAFKA_CFG_NUM_IO_THREADS = "kafka_cfg_num_io_threads";
    public static final Integer KAFKA_CFG_NUM_IO_THREADS_VALUE = 2;
    public static final String KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR =
            "kafka_offsets_topic_replication_factor";
    public static final Integer KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR_VALUE = 2;
    public static final String KAFKA_LOG_FLUSH_INTERVAL_MESSAGES =
            "kafka_log_flush_interval_messages";
    public static final Integer KAFKA_LOG_FLUSH_INTERVAL_MESSAGES_VALUE = 10000;
    public static final String ZOOKEEPER_SNAP_COUNT = "zookeeper_snap_count";
    public static final Integer ZOOKEEPER_SNAP_COUNT_VALUE = 1;
    public static final String ZOOKEEPER_GLOBAL_OUTSTANDING_LIMIT =
            "zookeeper_global_outstanding_limit";
    public static final Integer ZOOKEEPER_GLOBAL_OUTSTANDING_LIMIT_VALUE = 10000;
    public static final String HUAWEI_CLOUD_COMPUTE_INSTANCE = "huaweicloud_compute_instance";
    public static final String ZOOKEEPER = "zookeeper";
    public static final String KAFKA_BROKER = "kafka-broker";
    public static final String USER_ID = "userId";
    @MockitoBean private PolicyValidator mockPolicyValidator;

    void mockDeploymentWitPolicies() {
        doNothing().when(mockPolicyValidator).validateDeploymentWithPolicies(any());
    }

    @Test
    @WithJwt(file = "jwt_all_roles-no-policies.json")
    void test_change_service_configuration_success() throws Exception {
        ServiceOrder deployOrder = test_register_and_deploy_service();
        if (Objects.nonNull(deployOrder)
                && waitServiceDeploymentIsCompleted(deployOrder.getServiceId())) {
            test_change_service_configuration_well(deployOrder.getServiceId());
        }
    }

    private void test_change_service_configuration_well(UUID serviceId) throws Exception {
        ServiceOrder order = changeServiceConfiguration(serviceId);
        if (Objects.nonNull(order) && waitServiceOrderIsCompleted(order.getOrderId())) {
            List<ServiceChangeOrderDetails> requests =
                    listServiceChangeDetails(order.getOrderId(), order.getServiceId());
            assertFalse(requests.isEmpty());
            assertEquals(1, requests.size());
            requests.forEach(request -> assertEquals(request.getOrderId(), order.getOrderId()));
            requests.forEach(
                    request ->
                            request.getChangeRequests()
                                    .forEach(
                                            requestDetails ->
                                                    assertEquals(
                                                            ServiceChangeStatus.PENDING,
                                                            requestDetails.getStatus())));
            requests.forEach(
                    request ->
                            request.getChangeRequests()
                                    .forEach(
                                            requestDetails -> {
                                                if (ZOOKEEPER.equals(
                                                        requestDetails.getConfigManager())) {
                                                    assertEquals(
                                                            requestDetails.getProperties(),
                                                            getZookeeperConfig());
                                                }
                                                if (KAFKA_BROKER.equals(
                                                        requestDetails.getConfigManager())) {
                                                    assertEquals(
                                                            requestDetails.getProperties(),
                                                            getKafkaBrokerConfig());
                                                }
                                            }));
        }
    }

    ServiceOrder test_register_and_deploy_service() throws Exception {
        // Setup
        addCredentialForHuaweiCloud();
        Ocl ocl =
                new OclLoader()
                        .getOcl(
                                URI.create("file:src/test/resources/ocl_terraform_kafka_test.yml")
                                        .toURL());
        ServiceTemplateDetailVo serviceTemplate =
                registerServiceTemplateAndApproveRegistration(ocl);
        if (Objects.isNull(serviceTemplate)) {
            log.error("Register service template failed.");
            return null;
        }
        mockDeploymentWitPolicies();
        return deployService(serviceTemplate);
    }

    Map<String, Object> getZookeeperConfig() {
        Map<String, Object> configuration = new HashMap<>();
        configuration.put(ZOOKEEPER_SNAP_COUNT, ZOOKEEPER_SNAP_COUNT_VALUE);
        configuration.put(
                ZOOKEEPER_GLOBAL_OUTSTANDING_LIMIT, ZOOKEEPER_GLOBAL_OUTSTANDING_LIMIT_VALUE);
        return configuration;
    }

    Map<String, Object> getKafkaBrokerConfig() {
        Map<String, Object> configuration = new HashMap<>();
        configuration.put(KAFKA_CFG_MESSAGE_MAX_BYTES, KAFKA_CFG_MESSAGE_MAX_BYTES_VALUE);
        configuration.put(KAFKA_CFG_LOG_DIRS, KAFKA_CFG_LOG_DIRS_VALUE);
        configuration.put(KAFKA_CFG_NUM_IO_THREADS, KAFKA_CFG_NUM_IO_THREADS_VALUE);
        configuration.put(
                KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR,
                KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR_VALUE);
        configuration.put(
                KAFKA_LOG_FLUSH_INTERVAL_MESSAGES, KAFKA_LOG_FLUSH_INTERVAL_MESSAGES_VALUE);
        return configuration;
    }

    ServiceConfigurationUpdate getServiceConfigurationUpdate() {
        ServiceConfigurationUpdate request = new ServiceConfigurationUpdate();
        Map<String, Object> configuration = getKafkaBrokerConfig();
        configuration.putAll(getZookeeperConfig());
        request.setConfiguration(configuration);
        return request;
    }

    List<ServiceResourceEntity> getDeployResources() {
        List<ServiceResourceEntity> deployResources = new ArrayList<>();
        ServiceResourceEntity zookeeperResource = new ServiceResourceEntity();
        zookeeperResource.setId(UUID.randomUUID());
        zookeeperResource.setGroupType(HUAWEI_CLOUD_COMPUTE_INSTANCE);
        zookeeperResource.setGroupName(ZOOKEEPER);
        zookeeperResource.setResourceId(UUID.randomUUID().toString());
        zookeeperResource.setResourceName("kafka-zookeeper" + UUID.randomUUID());
        zookeeperResource.setResourceKind(DeployResourceKind.VM);
        deployResources.add(zookeeperResource);
        for (int i = 0; i < 3; i++) {
            ServiceResourceEntity kafkaResource = new ServiceResourceEntity();
            kafkaResource.setId(UUID.randomUUID());
            kafkaResource.setGroupType(HUAWEI_CLOUD_COMPUTE_INSTANCE);
            kafkaResource.setGroupName(KAFKA_BROKER);
            kafkaResource.setResourceId(UUID.randomUUID().toString());
            kafkaResource.setResourceName(KAFKA_BROKER + "-" + UUID.randomUUID());
            kafkaResource.setResourceKind(DeployResourceKind.VM);
            deployResources.add(kafkaResource);
        }
        return deployResources;
    }

    ServiceOrder changeServiceConfiguration(UUID serviceId) throws Exception {
        MockHttpServletResponse response =
                mockMvc.perform(
                                put("/xpanse/services/config/{serviceId}", serviceId)
                                        .content(
                                                objectMapper.writeValueAsString(
                                                        getServiceConfigurationUpdate()))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .accept(MediaType.APPLICATION_JSON))
                        .andReturn()
                        .getResponse();
        assertNotNull(response.getHeader(HEADER_TRACKING_ID));
        if (response.getStatus() == HttpStatus.OK.value()) {
            ServiceOrder order =
                    objectMapper.readValue(response.getContentAsString(), ServiceOrder.class);
            assertNotNull(order);
            assertEquals(order.getServiceId(), serviceId);
            return order;
        } else {
            ErrorResponse errorResponseError =
                    objectMapper.readValue(response.getContentAsString(), ErrorResponse.class);
            log.error(
                    "Change service configuration failed. Error: {}",
                    errorResponseError.getDetails());
            return null;
        }
    }

    List<ServiceChangeOrderDetails> listServiceChangeDetails(UUID orderId, UUID serviceId)
            throws Exception {

        final MockHttpServletResponse listResponse =
                mockMvc.perform(
                                get("/xpanse/services/config/requests")
                                        .param("orderId", orderId.toString())
                                        .param("serviceId", serviceId.toString())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .accept(MediaType.APPLICATION_JSON))
                        .andReturn()
                        .getResponse();
        assertEquals(HttpStatus.OK.value(), listResponse.getStatus());
        assertNotNull(listResponse.getHeader(HEADER_TRACKING_ID));
        return objectMapper.readValue(
                listResponse.getContentAsString(),
                new TypeReference<List<ServiceChangeOrderDetails>>() {});
    }
}
