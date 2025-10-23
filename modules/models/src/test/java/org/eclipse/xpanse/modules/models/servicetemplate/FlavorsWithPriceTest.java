package org.eclipse.xpanse.modules.models.servicetemplate;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.BeanUtils;

class FlavorsWithPriceTest {

    private final boolean isDowngradeAllowed = false;
    @Mock private List<ServiceFlavorWithPrice> mockFlavorWithPriceList;
    @Mock private ModificationImpact mockModificationImpact;

    private FlavorsWithPrice test;

    @BeforeEach
    void setUp() throws Exception {
        test = new FlavorsWithPrice();
        test.setServiceFlavors(mockFlavorWithPriceList);
        test.setModificationImpact(mockModificationImpact);
        test.setDowngradeAllowed(isDowngradeAllowed);
    }

    @Test
    void testGetters() {
        assertThat(test.getServiceFlavors()).isEqualTo(mockFlavorWithPriceList);
        assertThat(test.getModificationImpact()).isEqualTo(mockModificationImpact);
        assertThat(test.isDowngradeAllowed()).isEqualTo(isDowngradeAllowed);
    }

    @Test
    void testEqualsAndHashCode() {
        Object o = new Object();
        assertThat(test.canEqual(o)).isFalse();
        assertThat(test).isNotEqualTo(o);
        assertThat(test.hashCode()).isNotEqualTo(o.hashCode());

        FlavorsWithPrice flavors = new FlavorsWithPrice();
        assertThat(flavors.canEqual(test)).isTrue();
        assertThat(flavors).isNotEqualTo(test);
        assertThat(flavors.hashCode()).isNotEqualTo(test.hashCode());

        BeanUtils.copyProperties(test, flavors);
        assertThat(flavors).isEqualTo(test);
        assertThat(flavors.hashCode()).isEqualTo(test.hashCode());
    }

    @Test
    void testToString() throws Exception {
        String result =
                "FlavorsWithPrice(serviceFlavors="
                        + mockFlavorWithPriceList
                        + ", modificationImpact="
                        + mockModificationImpact
                        + ", isDowngradeAllowed="
                        + isDowngradeAllowed
                        + ", controllerApiMethods="
                        + null
                        + ")";
        assertThat(test.toString()).isEqualTo(result);
    }
}
