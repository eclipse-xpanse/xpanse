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

    private final UUID orderId = UUID.fromString("5956ede6-d19b-4f43-ad99-4a187eafefad");

    private final UUID serviceId = UUID.fromString("5956ede6-d19b-4f43-ad99-4a187eafefad");

    private final UUID serviceTemplateId = UUID.fromString("5956ede6-d19b-4f43-ad99-4a187eafefad");

    private final ServiceOrderType taskType = ServiceOrderType.DEPLOY;

    private final String userId = "userId";

    private final String namespace = "namespace";
    @Mock
    private DeployRequest mockDeployRequest;
    @Mock
    private Ocl mockOcl;
    @Mock
    private DeploymentScenario mockDeploymentScenario;
    private DeployTask test;

    @BeforeEach
    void setUp() {
        test = new DeployTask();
        test.setOrderId(orderId);
        test.setServiceId(serviceId);
        test.setTaskType(taskType);
        test.setUserId(userId);
        test.setNamespace(namespace);
        test.setDeployRequest(mockDeployRequest);
        test.setOcl(mockOcl);
        test.setServiceTemplateId(serviceTemplateId);
        test.setDeploymentScenario(mockDeploymentScenario);
    }

    @Test
    void testGetters() {
        assertThat(test.getOrderId()).isEqualTo(orderId);
        assertThat(test.getServiceId()).isEqualTo(serviceId);
        assertThat(test.getTaskType()).isEqualTo(taskType);
        assertThat(test.getUserId()).isEqualTo(userId);
        assertThat(test.getNamespace()).isEqualTo(namespace);
        assertThat(test.getDeployRequest()).isEqualTo(mockDeployRequest);
        assertThat(test.getOcl()).isEqualTo(mockOcl);
        assertThat(test.getServiceTemplateId()).isEqualTo(serviceTemplateId);
        assertThat(test.getDeploymentScenario()).isEqualTo(mockDeploymentScenario);
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
        String exceptedString = "DeployTask(orderId=" + orderId + ", "
                + "taskType=" + taskType + ", "
                + "userId=" + userId + ", "
                + "serviceId=" + serviceId + ", "
                + "namespace=" + namespace + ", "
                + "deployRequest=" + mockDeployRequest + ", "
                + "ocl=" + mockOcl + ", "
                + "serviceTemplateId=" + serviceTemplateId + ", "
                + "deploymentScenario=" + mockDeploymentScenario + ")";
        assertEquals(exceptedString, test.toString());
    }
}
