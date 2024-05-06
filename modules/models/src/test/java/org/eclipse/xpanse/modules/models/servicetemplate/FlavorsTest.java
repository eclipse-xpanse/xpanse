package org.eclipse.xpanse.modules.models.servicetemplate;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;

@ExtendWith(MockitoExtension.class)
class FlavorsTest {
    private final boolean isDowngradeAllowed = false;

    @Mock
    private List<ServiceFlavor> mockServiceFlavors;
    @Mock
    private ModificationImpact mockModificationImpact;

    private Flavors flavorsUnderTest;

    @BeforeEach
    void setUp() throws Exception {
        flavorsUnderTest = new Flavors();
        flavorsUnderTest.setServiceFlavors(mockServiceFlavors);
        flavorsUnderTest.setModificationImpact(mockModificationImpact);
        flavorsUnderTest.setDowngradeAllowed(isDowngradeAllowed);
    }

    @Test
    void testGetters() {
        assertThat(flavorsUnderTest.getServiceFlavors()).isEqualTo(mockServiceFlavors);
        assertThat(flavorsUnderTest.getModificationImpact()).isEqualTo(mockModificationImpact);
        assertThat(flavorsUnderTest.isDowngradeAllowed()).isEqualTo(isDowngradeAllowed);
    }


    @Test
    void testEqualsAndHashCode() {
        Object o = new Object();
        assertThat(flavorsUnderTest.canEqual(o)).isFalse();
        assertThat(flavorsUnderTest).isNotEqualTo(o);
        assertThat(flavorsUnderTest.hashCode()).isNotEqualTo(o.hashCode());

        Flavors flavors = new Flavors();
        assertThat(flavors.canEqual(flavorsUnderTest)).isTrue();
        assertThat(flavors).isNotEqualTo(flavorsUnderTest);
        assertThat(flavors.hashCode()).isNotEqualTo(flavorsUnderTest.hashCode());

        BeanUtils.copyProperties(flavorsUnderTest, flavors);
        assertThat(flavors).isEqualTo(flavorsUnderTest);
        assertThat(flavors.hashCode()).isEqualTo(flavorsUnderTest.hashCode());
    }

    @Test
    void testToString() throws Exception {
        String result = "Flavors(serviceFlavors=" + mockServiceFlavors + ", modificationImpact="
                + mockModificationImpact + ", isDowngradeAllowed=" + isDowngradeAllowed + ")";
        assertThat(flavorsUnderTest.toString()).isEqualTo(result);
    }
}
