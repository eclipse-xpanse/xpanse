package org.eclipse.xpanse.modules.deployment.deployers.opentofu;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OpenTofuExecutorResultTest {

    @Mock
    private Map<String, String> mockImportantFileContentMap;

    private OpenTofuExecutorResult openTofuExecutorResultUnderTest;

    @BeforeEach
    void setUp() {
        openTofuExecutorResultUnderTest = new OpenTofuExecutorResult();
        openTofuExecutorResultUnderTest.setImportantFileContentMap(mockImportantFileContentMap);
    }

    @Test
    void testTerraformStateGetterAndSetter() {
        final String terraformState = "terraformState";
        openTofuExecutorResultUnderTest.setTerraformState(terraformState);
        assertThat(openTofuExecutorResultUnderTest.getTerraformState()).isEqualTo(terraformState);
    }

    @Test
    void testGetImportantFileContentMap() {
        assertThat(openTofuExecutorResultUnderTest.getImportantFileContentMap())
                .isEqualTo(mockImportantFileContentMap);
    }

    @Test
    void testToString() {
        String result = "OpenTofuExecutorResult(terraformState=null, "
                + "importantFileContentMap=mockImportantFileContentMap)";
        assertThat(openTofuExecutorResultUnderTest.toString()).isEqualTo(result);
    }

    @Test
    void testEquals() {
        assertThat(openTofuExecutorResultUnderTest.equals("o")).isFalse();
    }

    @Test
    void testCanEqual() {
        assertThat(openTofuExecutorResultUnderTest.canEqual("other")).isFalse();
    }

    @Test
    void testHashCode() {
        assertThat(openTofuExecutorResultUnderTest.hashCode()).isNotEqualTo(0);
    }
}
