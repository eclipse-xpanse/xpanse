package org.eclipse.xpanse.modules.models.billing;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.eclipse.xpanse.modules.models.service.enums.DeployResourceKind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;

@ExtendWith(MockitoExtension.class)
class ResourceTest {

    private final int count = 1;
    @Mock private DeployResourceKind mockDeployResourceKind;
    @Mock private Map<String, String> mockProperties;

    private Resource resourceUnderTest;

    @BeforeEach
    void setUp() {
        resourceUnderTest = new Resource();
        resourceUnderTest.setCount(count);
        resourceUnderTest.setDeployResourceKind(mockDeployResourceKind);
        resourceUnderTest.setProperties(mockProperties);
    }

    @Test
    void testGetters() {
        assertThat(resourceUnderTest.getCount()).isEqualTo(count);
        assertThat(resourceUnderTest.getDeployResourceKind()).isEqualTo(mockDeployResourceKind);
        assertThat(resourceUnderTest.getProperties()).isEqualTo(mockProperties);
    }

    @Test
    void testEqualsAndHashCode() {
        Object o = new Object();
        assertThat(resourceUnderTest.equals(o)).isFalse();
        assertThat(resourceUnderTest.canEqual(o)).isFalse();
        assertThat(resourceUnderTest.hashCode()).isNotEqualTo(o.hashCode());

        Resource resource = new Resource();
        assertThat(resourceUnderTest.equals(resource)).isFalse();
        assertThat(resourceUnderTest.canEqual(resource)).isTrue();
        assertThat(resourceUnderTest.hashCode()).isNotEqualTo(resource.hashCode());

        BeanUtils.copyProperties(resourceUnderTest, resource);
        assertThat(resourceUnderTest.equals(resource)).isTrue();
        assertThat(resourceUnderTest.canEqual(resource)).isTrue();
        assertThat(resourceUnderTest.hashCode()).isEqualTo(resource.hashCode());
    }

    @Test
    void testToString() {
        String result =
                "Resource(count="
                        + count
                        + ", deployResourceKind="
                        + mockDeployResourceKind
                        + ", properties="
                        + mockProperties
                        + ")";
        assertThat(resourceUnderTest.toString()).isEqualTo(result);
    }
}
