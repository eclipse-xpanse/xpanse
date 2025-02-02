/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.c4_soft.springaddons.security.oauth2.test.annotations.WithJwt;
import com.fasterxml.jackson.core.type.TypeReference;
import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.models.billing.FlavorPriceResult;
import org.eclipse.xpanse.modules.models.billing.Price;
import org.eclipse.xpanse.modules.models.billing.PriceWithRegion;
import org.eclipse.xpanse.modules.models.billing.RatingMode;
import org.eclipse.xpanse.modules.models.billing.enums.BillingMode;
import org.eclipse.xpanse.modules.models.billing.enums.Currency;
import org.eclipse.xpanse.modules.models.billing.enums.PricingPeriod;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.Region;
import org.eclipse.xpanse.modules.models.servicetemplate.request.ServiceTemplateRequestInfo;
import org.eclipse.xpanse.modules.models.servicetemplate.view.ServiceTemplateDetailVo;
import org.eclipse.xpanse.plugins.huaweicloud.common.HuaweiCloudConstants;
import org.eclipse.xpanse.runtime.util.ApisTestCommon;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/** Test for ServicePricingApi. */
@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {"spring.profiles.active=oauth,zitadel,zitadel-testbed,test,dev"})
@AutoConfigureMockMvc
class ServicePricingApiTest extends ApisTestCommon {

    @Test
    @WithJwt(file = "jwt_all_roles.json")
    void testServicePricingApi() throws Exception {
        testServicePricingApiThrowExceptions();

        testServicePricingApiWithHuaweiCloud();
        testServicePricingApiWithFlexibleEngine();
        testServicePricingApiWithOpenstackTestlab();
        testServicePricingApiWithPlusServer();
        testServicePricingApiWithRegioCloud();
    }

    void testServicePricingApiWithHuaweiCloud() throws Exception {
        // Setup
        Ocl ocl =
                oclLoader.getOcl(
                        URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        String flavorName = ocl.getFlavors().getServiceFlavors().getFirst().getName();
        String regionName = ocl.getCloudServiceProvider().getRegions().getFirst().getName();
        testGetServicePricing(ocl, flavorName, regionName, HuaweiCloudConstants.INTERNATIONAL_SITE);
        testGetServicePricing(
                ocl, flavorName, regionName, HuaweiCloudConstants.CHINESE_MAINLAND_SITE);
        ocl.getFlavors()
                .getServiceFlavors()
                .getFirst()
                .getPricing()
                .getResourceUsage()
                .getMarkUpPrices()
                .forEach(
                        priceWithRegion -> {
                            priceWithRegion.getPrice().setPeriod(PricingPeriod.ONE_TIME);
                        });
        testGetServicePricing(ocl, flavorName, regionName, HuaweiCloudConstants.EUROPE_SITE);
    }

    void testServicePricingApiWithFlexibleEngine() throws Exception {
        // Setup
        Ocl ocl =
                oclLoader.getOcl(
                        URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        ocl.getCloudServiceProvider().setName(Csp.FLEXIBLE_ENGINE);
        Region region = new Region();
        region.setName("eu-west-0");
        region.setSite("default");
        region.setArea("Europe");
        ocl.getCloudServiceProvider().setRegions(List.of(region));
        PriceWithRegion priceWithRegion = new PriceWithRegion();
        priceWithRegion.setRegionName("any");
        priceWithRegion.setSiteName("default");
        Price price = new Price();
        price.setCost(BigDecimal.valueOf(0.10));
        price.setPeriod(PricingPeriod.HOURLY);
        price.setCurrency(Currency.USD);
        priceWithRegion.setPrice(price);
        List<PriceWithRegion> prices = List.of(priceWithRegion);
        ocl.getFlavors()
                .getServiceFlavors()
                .forEach(
                        flavorWithPrice -> {
                            flavorWithPrice.getPricing().setFixedPrices(prices);
                            flavorWithPrice.getPricing().getResourceUsage().setMarkUpPrices(prices);
                            flavorWithPrice
                                    .getPricing()
                                    .getResourceUsage()
                                    .setLicensePrices(prices);
                        });
        String flavorName = ocl.getFlavors().getServiceFlavors().getFirst().getName();
        testGetServicePricing(ocl, flavorName, region.getName(), region.getSite());
    }

    void testServicePricingApiWithOpenstackTestlab() throws Exception {
        Ocl ocl =
                oclLoader.getOcl(
                        URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        ocl.getCloudServiceProvider().setName(Csp.OPENSTACK_TESTLAB);
        Region region = new Region();
        region.setName("RegionOne");
        region.setSite("default");
        region.setArea("Europe");
        ocl.getCloudServiceProvider().setRegions(List.of(region));
        PriceWithRegion priceWithRegion = new PriceWithRegion();
        priceWithRegion.setRegionName("any");
        priceWithRegion.setSiteName("default");
        Price price = new Price();
        price.setCost(BigDecimal.valueOf(0.10 * 24));
        price.setPeriod(PricingPeriod.DAILY);
        price.setCurrency(Currency.USD);
        priceWithRegion.setPrice(price);
        List<PriceWithRegion> prices = List.of(priceWithRegion);
        ocl.getFlavors()
                .getServiceFlavors()
                .forEach(
                        flavorWithPrice -> {
                            flavorWithPrice.getPricing().setFixedPrices(prices);
                            flavorWithPrice.getPricing().getResourceUsage().setMarkUpPrices(prices);
                            flavorWithPrice
                                    .getPricing()
                                    .getResourceUsage()
                                    .setLicensePrices(prices);
                        });

        String flavorName = ocl.getFlavors().getServiceFlavors().getFirst().getName();
        testGetServicePricing(ocl, flavorName, region.getName(), region.getSite());
    }

    void testServicePricingApiWithPlusServer() throws Exception {
        Ocl ocl =
                oclLoader.getOcl(
                        URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        ocl.getCloudServiceProvider().setName(Csp.PLUS_SERVER);
        Region region = new Region();
        region.setName("RegionOne");
        region.setSite("default");
        region.setArea("Europe");
        ocl.getCloudServiceProvider().setRegions(List.of(region));
        PriceWithRegion priceWithRegion = new PriceWithRegion();
        priceWithRegion.setRegionName("any");
        priceWithRegion.setSiteName("default");
        Price price = new Price();
        price.setCost(BigDecimal.valueOf(0.10 * 24 * 30));
        price.setPeriod(PricingPeriod.MONTHLY);
        price.setCurrency(Currency.USD);
        priceWithRegion.setPrice(price);
        List<PriceWithRegion> prices = List.of(priceWithRegion);
        ocl.getFlavors()
                .getServiceFlavors()
                .forEach(
                        flavorWithPrice -> {
                            flavorWithPrice.getPricing().setFixedPrices(prices);
                            flavorWithPrice.getPricing().getResourceUsage().setMarkUpPrices(prices);
                            flavorWithPrice
                                    .getPricing()
                                    .getResourceUsage()
                                    .setLicensePrices(prices);
                        });
        String flavorName = ocl.getFlavors().getServiceFlavors().getFirst().getName();
        testGetServicePricing(ocl, flavorName, region.getName(), region.getSite());
    }

    void testServicePricingApiWithRegioCloud() throws Exception {
        Ocl ocl =
                oclLoader.getOcl(
                        URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        ocl.getCloudServiceProvider().setName(Csp.REGIO_CLOUD);
        Region region = new Region();
        region.setName("RegionOne");
        region.setSite("default");
        region.setArea("Europe");
        ocl.getCloudServiceProvider().setRegions(List.of(region));
        PriceWithRegion priceWithRegion = new PriceWithRegion();
        priceWithRegion.setRegionName("any");
        priceWithRegion.setSiteName("default");
        Price price = new Price();
        price.setCost(BigDecimal.valueOf(0.10 * 24 * 365));
        price.setPeriod(PricingPeriod.YEARLY);
        price.setCurrency(Currency.USD);
        priceWithRegion.setPrice(price);
        List<PriceWithRegion> prices = List.of(priceWithRegion);
        ocl.getFlavors()
                .getServiceFlavors()
                .forEach(
                        flavorWithPrice -> {
                            flavorWithPrice.getPricing().setFixedPrices(prices);
                            flavorWithPrice.getPricing().getResourceUsage().setMarkUpPrices(prices);
                            flavorWithPrice
                                    .getPricing()
                                    .getResourceUsage()
                                    .setLicensePrices(prices);
                        });
        String flavorName = ocl.getFlavors().getServiceFlavors().getFirst().getName();
        testGetServicePricing(ocl, flavorName, region.getName(), region.getSite());
    }

    private void testGetServicePricing(
            Ocl ocl, String flavorName, String regionName, String siteName) throws Exception {
        // Setup
        ServiceTemplateRequestInfo registerInfo = registerServiceTemplate(ocl);
        ServiceTemplateDetailVo serviceTemplate =
                getServiceTemplateDetailsVo(registerInfo.getServiceTemplateId());
        UUID templateId = serviceTemplate.getServiceTemplateId();
        int flavorCount = ocl.getFlavors().getServiceFlavors().size();
        MockHttpServletResponse serviceFixedPricesResponse =
                getPricesByService(templateId, regionName, siteName, BillingMode.FIXED);

        List<FlavorPriceResult> fixedPriceResultList =
                objectMapper.readValue(
                        serviceFixedPricesResponse.getContentAsString(), new TypeReference<>() {});
        assertEquals(HttpStatus.OK.value(), serviceFixedPricesResponse.getStatus());
        Assertions.assertEquals(flavorCount, fixedPriceResultList.size());

        MockHttpServletResponse servicePayPerUsePricesResponse =
                getPricesByService(templateId, regionName, siteName, BillingMode.PAY_PER_USE);

        List<FlavorPriceResult> payPerUsePriceResultList =
                objectMapper.readValue(
                        servicePayPerUsePricesResponse.getContentAsString(),
                        new TypeReference<>() {});
        assertEquals(HttpStatus.OK.value(), servicePayPerUsePricesResponse.getStatus());
        Assertions.assertEquals(flavorCount, payPerUsePriceResultList.size());

        MockHttpServletResponse fixedPriceResponse =
                getServicePriceByFlavor(
                        templateId, regionName, siteName, flavorName, BillingMode.FIXED);
        FlavorPriceResult flavorPriceResult =
                objectMapper.readValue(
                        fixedPriceResponse.getContentAsString(), FlavorPriceResult.class);
        assertEquals(HttpStatus.OK.value(), fixedPriceResponse.getStatus());
        assertNotNull(flavorPriceResult.getRecurringPrice());
        assertNull(flavorPriceResult.getOneTimePaymentPrice());

        // Setup
        MockHttpServletResponse payPerUsePriceResponse =
                getServicePriceByFlavor(
                        templateId, regionName, siteName, flavorName, BillingMode.PAY_PER_USE);
        FlavorPriceResult flavorPriceResult1 =
                objectMapper.readValue(
                        payPerUsePriceResponse.getContentAsString(), FlavorPriceResult.class);
        assertEquals(HttpStatus.OK.value(), payPerUsePriceResponse.getStatus());
        assertNotNull(flavorPriceResult1.getRecurringPrice());
        deleteServiceTemplate(templateId);
    }

    void testServicePricingApiThrowExceptions() throws Exception {
        // Setup
        UUID uuid = UUID.randomUUID();
        String regionName = "region";
        String siteName = "site";
        String flavorName = "flavor-error-test";
        BillingMode billingMode = BillingMode.FIXED;

        FlavorPriceResult expectedResult = new FlavorPriceResult();
        expectedResult.setFlavorName(flavorName);
        expectedResult.setBillingMode(billingMode);
        expectedResult.setSuccessful(false);
        expectedResult.setErrorMessage(
                String.format("Service template with id %s not found.", uuid));
        String result1 = objectMapper.writeValueAsString(expectedResult);
        // Run the test
        final MockHttpServletResponse priceResponse1 =
                getServicePriceByFlavor(uuid, regionName, siteName, flavorName, billingMode);
        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), priceResponse1.getStatus());
        assertEquals(result1, priceResponse1.getContentAsString());

        // Setup
        Ocl ocl =
                oclLoader.getOcl(
                        URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        ServiceTemplateRequestInfo registerRequestInfo = registerServiceTemplate(ocl);
        ServiceTemplateDetailVo serviceTemplateDetails =
                getServiceTemplateDetailsVo(registerRequestInfo.getServiceTemplateId());
        UUID templateId = serviceTemplateDetails.getServiceTemplateId();
        expectedResult.setErrorMessage(
                String.format(
                        "Flavor %s not found in service template with id %s.",
                        flavorName, templateId));
        String result2 = objectMapper.writeValueAsString(expectedResult);
        // Run the test
        final MockHttpServletResponse priceResponse2 =
                getServicePriceByFlavor(templateId, regionName, siteName, flavorName, billingMode);
        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), priceResponse2.getStatus());
        assertEquals(result2, priceResponse2.getContentAsString());

        // Setup
        ServiceTemplateEntity serviceTemplate =
                serviceTemplateStorage.getServiceTemplateById(templateId);
        flavorName = ocl.getFlavors().getServiceFlavors().getFirst().getName();
        RatingMode ratingMode = ocl.getFlavors().getServiceFlavors().getFirst().getPricing();
        ratingMode.setResourceUsage(null);
        ratingMode.setFixedPrices(null);
        ocl.getFlavors().getServiceFlavors().getFirst().setPricing(ratingMode);
        serviceTemplate.setOcl(ocl);
        serviceTemplateStorage.storeAndFlush(serviceTemplate);
        expectedResult.setFlavorName(flavorName);
        expectedResult.setErrorMessage(
                "BillingMode 'Fixed' can not be supported due to the " + "'FixedPrices' is null.");
        String result3 = objectMapper.writeValueAsString(expectedResult);
        // Run the test
        final MockHttpServletResponse priceResponse3 =
                getServicePriceByFlavor(templateId, regionName, siteName, flavorName, billingMode);
        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), priceResponse3.getStatus());
        assertEquals(result3, priceResponse3.getContentAsString());

        billingMode = BillingMode.PAY_PER_USE;
        expectedResult.setBillingMode(billingMode);
        expectedResult.setErrorMessage(
                "BillingMode 'Pay-Per-Use' can not be supported "
                        + "due to the 'ResourceUsage' is null.");
        String result4 = objectMapper.writeValueAsString(expectedResult);
        // Run the test
        final MockHttpServletResponse priceResponse4 =
                getServicePriceByFlavor(templateId, regionName, siteName, flavorName, billingMode);
        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), priceResponse4.getStatus());
        assertEquals(result4, priceResponse4.getContentAsString());

        // Setup
        ocl.getFlavors().getServiceFlavors().getFirst().setPricing(null);
        serviceTemplate.setOcl(ocl);
        serviceTemplateStorage.storeAndFlush(serviceTemplate);

        expectedResult.setErrorMessage(
                String.format(
                        "Flavor %s in service template with id %s " + "has no pricing.",
                        flavorName, templateId));
        String result5 = objectMapper.writeValueAsString(expectedResult);
        // Run the test
        final MockHttpServletResponse priceResponse5 =
                getServicePriceByFlavor(templateId, regionName, siteName, flavorName, billingMode);

        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), priceResponse5.getStatus());
        assertEquals(result5, priceResponse5.getContentAsString());

        deleteServiceTemplate(templateId);
    }

    private MockHttpServletResponse getServicePriceByFlavor(
            UUID templateId,
            String regionName,
            String siteName,
            String flavorName,
            BillingMode billingMode)
            throws Exception {
        return mockMvc.perform(
                        get(
                                        "/xpanse/pricing/{templateId}/{regionName}/{siteName}/{billingMode}/{flavorName}",
                                        templateId,
                                        regionName,
                                        siteName,
                                        billingMode,
                                        flavorName)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();
    }

    private MockHttpServletResponse getPricesByService(
            UUID templateId, String regionName, String siteName, BillingMode billingMode)
            throws Exception {
        return mockMvc.perform(
                        get(
                                        "/xpanse/pricing/service/{templateId}/{regionName}/{siteName}/{billingMode}",
                                        templateId,
                                        regionName,
                                        siteName,
                                        billingMode)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();
    }
}
