package org.eclipse.xpanse.modules.orchestrator.deployment;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.eclipse.xpanse.modules.models.service.deploy.DeployRequest;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeployTaskTest {

    final UUID id = UUID.fromString("5956ede6-d19b-4f43-ad99-4a187eafefad");
    @Mock
    private DeployRequest mockDeployRequest;
    @Mock
    private Ocl mockOcl;
    @Mock
    private DestroyScenario mockDestroyScenario;
    private DeployTask deployTaskUnderTest;

    @BeforeEach
    void setUp() {
        deployTaskUnderTest = new DeployTask();
        deployTaskUnderTest.setId(id);
        deployTaskUnderTest.setDeployRequest(mockDeployRequest);
        deployTaskUnderTest.setOcl(mockOcl);
        deployTaskUnderTest.setDestroyScenario(mockDestroyScenario);
    }

    @Test
    void testGetterAndSetter() {
        deployTaskUnderTest.setId(id);
        assertThat(deployTaskUnderTest.getId()).isEqualTo(id);
        final String namespace = "namespace";
        deployTaskUnderTest.setNamespace(namespace);
        assertThat(deployTaskUnderTest.getNamespace()).isEqualTo(namespace);
        assertThat(deployTaskUnderTest.getDeployRequest()).isEqualTo(mockDeployRequest);
        assertThat(deployTaskUnderTest.getOcl()).isEqualTo(mockOcl);
        final UUID serviceTemplateId = UUID.fromString("42185b7f-c328-4d2c-b53e-18fb7a9db3b7");
        deployTaskUnderTest.setServiceTemplateId(serviceTemplateId);
        assertThat(deployTaskUnderTest.getServiceTemplateId()).isEqualTo(serviceTemplateId);
        assertThat(deployTaskUnderTest.getDestroyScenario()).isEqualTo(mockDestroyScenario);
    }

    @Test
    void testEquals() {
        assertThat(deployTaskUnderTest.equals("o")).isFalse();
    }

    @Test
    void testCanEqual() {
        assertThat(deployTaskUnderTest.canEqual("other")).isFalse();
    }

    @Test
    void testHashCode() {
        assertThat(deployTaskUnderTest.hashCode()).isNotEqualTo(0);
    }

    @Test
    void testToString() {
        String exceptedString = String.format("DeployTask(id=%s, namespace=null, "
                        + "deployRequest=mockDeployRequest, ocl=mockOcl, serviceTemplateId=null,"
                        + " destroyScenario=mockDestroyScenario)", id);
        assertThat(deployTaskUnderTest.toString()).isEqualTo(exceptedString);
    }
}
