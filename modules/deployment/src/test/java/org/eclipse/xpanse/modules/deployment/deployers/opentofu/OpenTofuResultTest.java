package org.eclipse.xpanse.modules.deployment.deployers.opentofu;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.eclipse.xpanse.modules.orchestrator.deployment.DestroyScenario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OpenTofuResultTest {

    @Mock
    private Map<String, String> mockImportantFileContentMap;

    private OpenTofuResult openTofuResultUnderTest;

    @BeforeEach
    void setUp() {
        openTofuResultUnderTest = new OpenTofuResult();
        openTofuResultUnderTest.setImportantFileContentMap(mockImportantFileContentMap);
    }

    @Test
    void testGetterAndSetter() {
        final DestroyScenario destroyScenario = DestroyScenario.DESTROY;
        openTofuResultUnderTest.setDestroyScenario(destroyScenario);
        assertThat(openTofuResultUnderTest.getDestroyScenario()).isEqualTo(destroyScenario);

        final String terraformState = "terraformState";
        openTofuResultUnderTest.setTerraformState(terraformState);
        assertThat(openTofuResultUnderTest.getTerraformState()).isEqualTo(terraformState);

        assertThat(openTofuResultUnderTest.getImportantFileContentMap())
                .isEqualTo(mockImportantFileContentMap);
    }

    @Test
    void testToString() {
        String result = "OpenTofuResult(destroyScenario=null, terraformState=null, "
                + "importantFileContentMap=mockImportantFileContentMap)";
        assertThat(openTofuResultUnderTest.toString()).isEqualTo(result);
    }

    @Test
    void testEquals() {
        assertThat(openTofuResultUnderTest.equals("o")).isFalse();
    }

    @Test
    void testCanEqual() {
        assertThat(openTofuResultUnderTest.canEqual("other")).isFalse();
    }

    @Test
    void testHashCode() {
        assertThat(openTofuResultUnderTest.hashCode()).isNotEqualTo(0);
    }
}
