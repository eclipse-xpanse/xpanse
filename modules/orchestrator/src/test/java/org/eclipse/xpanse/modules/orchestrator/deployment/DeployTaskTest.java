package org.eclipse.xpanse.modules.orchestrator.deployment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.UUID;
import org.eclipse.xpanse.modules.models.service.deploy.DeployRequest;
import org.eclipse.xpanse.modules.models.service.order.enums.ServiceOrderType;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;

@ExtendWith(MockitoExtension.class)
class DeployTaskTest {

    private final UUID uuid = UUID.fromString("5956ede6-d19b-4f43-ad99-4a187eafefad");
    private final ServiceOrderType taskType = ServiceOrderType.DEPLOY;
    private final String userId = "userId";
    private final String namespace = "namespace";
    @Mock
    private DeployRequest mockDeployRequest;
    @Mock
    private Object mockRequest;
    @Mock
    private Ocl mockOcl;
    private DeployTask test;

    @BeforeEach
    void setUp() {
        test = new DeployTask();
        test.setOrderId(uuid);
        test.setParentOrderId(uuid);
        test.setServiceId(uuid);
        test.setOriginalServiceId(uuid);
        test.setWorkflowId(uuid.toString());
        test.setTaskType(taskType);
        test.setUserId(userId);
        test.setNamespace(namespace);
        test.setDeployRequest(mockDeployRequest);
        test.setRequest(mockRequest);
        test.setOcl(mockOcl);
        test.setServiceTemplateId(uuid);
    }

    @Test
    void testGetters() {
        assertThat(test.getOrderId()).isEqualTo(uuid);
        assertThat(test.getParentOrderId()).isEqualTo(uuid);
        assertThat(test.getServiceId()).isEqualTo(uuid);
        assertThat(test.getOriginalServiceId()).isEqualTo(uuid);
        assertThat(test.getWorkflowId()).isEqualTo(uuid.toString());
        assertThat(test.getTaskType()).isEqualTo(taskType);
        assertThat(test.getUserId()).isEqualTo(userId);
        assertThat(test.getNamespace()).isEqualTo(namespace);
        assertThat(test.getOcl()).isEqualTo(mockOcl);
        assertThat(test.getServiceTemplateId()).isEqualTo(uuid);
        assertThat(test.getDeployRequest()).isEqualTo(mockDeployRequest);
        assertThat(test.getRequest()).isEqualTo(mockRequest);
    }

    @Test
    void testHashCodeAndEquals() {
        Object obj = new Object();
        assertNotEquals(test, obj);
        assertNotEquals(test.hashCode(), obj.hashCode());

        DeployTask test1 = new DeployTask();
        assertNotEquals(test, test1);
        assertNotEquals(test.hashCode(), test1.hashCode());

        BeanUtils.copyProperties(test, test1);
        assertEquals(test, test1);
        assertEquals(test.hashCode(), test1.hashCode());
    }

    @Test
    void testToString() {
        String exceptedString = "DeployTask(orderId=" + uuid
                + ", parentOrderId=" + uuid
                + ", taskType=" + taskType
                + ", userId=" + userId
                + ", serviceId=" + uuid
                + ", originalServiceId=" + uuid
                + ", workflowId=" + uuid
                + ", namespace=" + namespace
                + ", deployRequest=" + mockDeployRequest
                + ", request=" + mockRequest
                + ", ocl=" + mockOcl
                + ", serviceTemplateId=" + uuid + ")";
        assertEquals(exceptedString, test.toString());
    }
}
