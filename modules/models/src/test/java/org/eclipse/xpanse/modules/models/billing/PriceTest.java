package org.eclipse.xpanse.modules.models.billing;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.xpanse.modules.models.billing.enums.Currency;
import org.eclipse.xpanse.modules.models.billing.enums.PricingPeriod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;

class PriceTest {

    private final long cost = 10;
    private final Currency currency = Currency.USD;
    private final PricingPeriod period = PricingPeriod.MONTHLY;

    private Price priceUnderTest;

    @BeforeEach
    void setUp() {
        priceUnderTest = new Price();
        priceUnderTest.setCost(cost);
        priceUnderTest.setCurrency(currency);
        priceUnderTest.setPeriod(period);
    }

    @Test
    void testGetters() {
        assertThat(priceUnderTest.getCost()).isEqualTo(cost);
        assertThat(priceUnderTest.getCurrency()).isEqualTo(currency);
        assertThat(priceUnderTest.getPeriod()).isEqualTo(period);
    }

    @Test
    void testEqualsAndHashCode() {
        Object o = new Object();
        assertThat(priceUnderTest.canEqual(o)).isFalse();
        assertThat(priceUnderTest.canEqual(new Price())).isTrue();
        assertThat(priceUnderTest.equals(o)).isFalse();
        assertThat(priceUnderTest.hashCode()).isNotEqualTo(o.hashCode());

        Price price1 = new Price();
        assertThat(priceUnderTest.equals(price1)).isFalse();
        assertThat(priceUnderTest.hashCode()).isNotEqualTo(price1.hashCode());

        BeanUtils.copyProperties(priceUnderTest, price1);
        assertThat(priceUnderTest.equals(price1)).isTrue();
        assertThat(priceUnderTest.hashCode()).isEqualTo(price1.hashCode());
    }

    @Test
    void testToString() {
        String result =
                "Price(cost=" + cost + ", currency=" + currency + ", period=" + period + ")";
        assertThat(priceUnderTest.toString()).isEqualTo(result);
    }
}
