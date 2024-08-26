package org.eclipse.xpanse.modules.models.billing;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.BeanUtils;

class PriceWithRegionTest {

    private final String regionName = "any";

    private final String siteName = "default";

    @Mock
    private Price mockPrice;

    private PriceWithRegion test;

    @BeforeEach
    void setUp() {
        test = new PriceWithRegion();
        test.setPrice(mockPrice);
        test.setRegionName(regionName);
        test.setSiteName(siteName);
    }

    @Test
    void testGetters() {
        assertThat(test.getRegionName()).isEqualTo(regionName);
        assertThat(test.getSiteName()).isEqualTo(siteName);
        assertThat(test.getPrice()).isEqualTo(mockPrice);
    }


    @Test
    void testEqualsAndHashCode() {
        Object o = new Object();
        assertThat(test.equals(o)).isFalse();
        assertThat(test.canEqual(o)).isFalse();
        assertThat(test.hashCode()).isNotEqualTo(o.hashCode());

        PriceWithRegion test1 = new PriceWithRegion();
        assertThat(test.equals(test1)).isFalse();
        assertThat(test.canEqual(test1)).isTrue();
        assertThat(test.hashCode()).isNotEqualTo(test1.hashCode());

        BeanUtils.copyProperties(test, test1);
        assertThat(test.equals(test1)).isTrue();
        assertThat(test.canEqual(test1)).isTrue();
        assertThat(test.hashCode()).isEqualTo(test1.hashCode());
    }


    @Test
    void testToString() {
        String result = "PriceWithRegion(regionName=" + regionName + ", siteName=" + siteName
                + ", price=" + mockPrice + ")";
        assertThat(test.toString()).isEqualTo(result);
    }
}
