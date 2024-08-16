package org.eclipse.xpanse.modules.models.billing;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.BeanUtils;

class PriceWithRegionTest {

    private final String region = "any";

    @Mock
    private Price mockPrice;

    private PriceWithRegion test;

    @BeforeEach
    void setUp() {
        test = new PriceWithRegion();
        test.setPrice(mockPrice);
        test.setRegion(region);
    }

    @Test
    void testGetters() {
        assertThat(test.getPrice()).isEqualTo(mockPrice);
        assertThat(test.getRegion()).isEqualTo(region);
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
        String result = "PriceWithRegion(region=" + region + ", price=" + mockPrice + ")";
        assertThat(test.toString()).isEqualTo(result);
    }
}
