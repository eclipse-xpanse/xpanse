/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.c4_soft.springaddons.security.oauth2.test.annotations.WithJwt;
import com.fasterxml.jackson.core.type.TypeReference;
import com.huaweicloud.sdk.bss.v2.model.ListOnDemandResourceRatingsRequest;
import com.huaweicloud.sdk.bss.v2.model.ListOnDemandResourceRatingsResponse;
import com.huaweicloud.sdk.core.exception.ClientRequestException;
import com.huaweicloud.sdk.core.invoker.SyncInvoker;
import com.huaweicloud.sdk.iam.v3.model.KeystoneListProjectsRequest;
import com.huaweicloud.sdk.iam.v3.model.KeystoneListProjectsResponse;
import com.huaweicloud.sdk.iam.v3.model.ProjectResult;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.models.billing.FlavorPriceResult;
import org.eclipse.xpanse.modules.models.billing.Price;
import org.eclipse.xpanse.modules.models.billing.RatingMode;
import org.eclipse.xpanse.modules.models.billing.enums.BillingMode;
import org.eclipse.xpanse.modules.models.billing.enums.Currency;
import org.eclipse.xpanse.modules.models.billing.enums.PricingPeriod;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.view.ServiceTemplateDetailVo;
import org.eclipse.xpanse.runtime.util.ApisTestCommon;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openstack4j.openstack.OSFactory;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Test for ServicePricingApi.
 */
@SuppressWarnings("unchecked")
@Transactional
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {"spring.profiles.active=oauth,zitadel,zitadel-testbed"})
@AutoConfigureMockMvc
class ServicePricingApiTest extends ApisTestCommon {

    @BeforeEach
    void setUp() {
        if (mockOsFactory != null) {
            mockOsFactory.close();
        }
        mockOsFactory = mockStatic(OSFactory.class);
    }

    @AfterEach
    void tearDown() {
        mockOsFactory.close();
    }

    @Test
    @WithJwt(file = "jwt_all_roles.json")
    void testServicePricingApi() throws Exception {
        testServicePricingApiThrowExceptions();

        testServicePricingApiWithHuaweiCloud();
        testServicePricingApiWithFlexibleEngine();
        testServicePricingApiWithOpenstack();
        testServicePricingApiWithPlusServer();
    }

    void testServicePricingApiWithHuaweiCloud() throws Exception {
        // Setup
        Ocl ocl = oclLoader.getOcl(
                URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        String flavorName = ocl.getFlavors().getServiceFlavors().getFirst().getName();
        String regionName = ocl.getCloudServiceProvider().getRegions().getFirst().getName();
        ServiceTemplateDetailVo serviceTemplateDetails = registerServiceTemplate(ocl);
        UUID templateId = serviceTemplateDetails.getServiceTemplateId();
        MockHttpServletResponse fixedPriceResponse =
                getServicePriceByFlavor(templateId, regionName, flavorName, BillingMode.FIXED);
        assertEquals(HttpStatus.OK.value(), fixedPriceResponse.getStatus());
        FlavorPriceResult flavorPriceResult =
                objectMapper.readValue(fixedPriceResponse.getContentAsString(),
                        FlavorPriceResult.class);
        assertNotNull(flavorPriceResult.getRecurringPrice());
        assertEquals(Currency.CNY, flavorPriceResult.getRecurringPrice().getCurrency());
        assertEquals(BillingMode.FIXED, flavorPriceResult.getBillingMode());
        assertNull(flavorPriceResult.getOneTimePaymentPrice());

        // Setup
        int flavorCount = ocl.getFlavors().getServiceFlavors().size();
        MockHttpServletResponse serviceFixedPricesResponse =
                getPricesByService(templateId, regionName, BillingMode.FIXED);

        List<FlavorPriceResult> fixedPriceResultList =
                objectMapper.readValue(serviceFixedPricesResponse.getContentAsString(),
                        new TypeReference<>() {
                        });
        assertEquals(HttpStatus.OK.value(), serviceFixedPricesResponse.getStatus());
        assertEquals(flavorCount, fixedPriceResultList.size());

        addCredentialForHuaweiCloud();
        mockSdkClientsForHuaweiCloud();
        // mock ListProjectInvoker OK
        mockListProjectInvoker();
        // mock ListOnDemandResourceRatingsInvoker OK in BssintlClient
        mockListOnDemandResourceRatingsInvokerWithBssintlClient();

        // Test get prices by service with PAY_PER_USE
        MockHttpServletResponse servicePayPerUsePricesResponse =
                getPricesByService(templateId, regionName, BillingMode.PAY_PER_USE);
        assertEquals(HttpStatus.OK.value(), servicePayPerUsePricesResponse.getStatus());

        List<FlavorPriceResult> payPerUsePriceResultList =
                objectMapper.readValue(servicePayPerUsePricesResponse.getContentAsString(),
                        new TypeReference<>() {
                        });
        assertEquals(flavorCount, payPerUsePriceResultList.size());
        assertTrue(payPerUsePriceResultList.stream().allMatch(price ->
                price.getRecurringPrice().getCurrency().equals(Currency.USD)));

        // Test get prices by flavor with PAY_PER_USE
        changeFlavorPriceNotOneTime(templateId, ocl, Currency.USD, PricingPeriod.MONTHLY);
        // mock ListProjectInvoker with Client failed.
        MockHttpServletResponse flavorPayPerUsePricesResponse =
                getServicePriceByFlavor(templateId, regionName, flavorName,
                        BillingMode.PAY_PER_USE);
        assertEquals(HttpStatus.OK.value(), flavorPayPerUsePricesResponse.getStatus());
        FlavorPriceResult flavorPayPerUsePriceResult =
                objectMapper.readValue(flavorPayPerUsePricesResponse.getContentAsString(),
                        FlavorPriceResult.class);
        assertNotNull(flavorPayPerUsePriceResult.getRecurringPrice());
        assertEquals(Currency.USD, flavorPayPerUsePriceResult.getRecurringPrice().getCurrency());
        assertEquals(BillingMode.PAY_PER_USE, flavorPayPerUsePriceResult.getBillingMode());
        assertNull(flavorPayPerUsePriceResult.getOneTimePaymentPrice());
        deleteServiceTemplate(templateId);
    }

    @Test
    @WithJwt(file = "jwt_all_roles.json")
    void testServicePricingApiWithHuaweiCloudBssClient() throws Exception {
        // Setup
        Ocl ocl = oclLoader.getOcl(
                URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        String flavorName = ocl.getFlavors().getServiceFlavors().getFirst().getName();
        String regionName = ocl.getCloudServiceProvider().getRegions().getFirst().getName();
        ServiceTemplateDetailVo serviceTemplateDetails = registerServiceTemplate(ocl);
        UUID templateId = serviceTemplateDetails.getServiceTemplateId();
        int flavorCount = ocl.getFlavors().getServiceFlavors().size();
        // Setup get price with pay per use
        addCredentialForHuaweiCloud();
        mockSdkClientsForHuaweiCloud();
        // mock ListProjectInvoker OK
        mockListProjectInvoker();
        // Mock ListOnDemandResourceRatingsInvoker with BssintClient failed
        mockListOnDemandResourceRatingsInvokerWithBssintlClientThrowException();
        // Mock ListOnDemandResourceRatingsInvoker with BssClient OK
        mockListOnDemandResourceRatingsInvokerWithBssClient();

        // Test get prices by service with PAY_PER_USE
        MockHttpServletResponse servicePayPerUsePricesResponse =
                getPricesByService(templateId, regionName, BillingMode.PAY_PER_USE);
        assertEquals(HttpStatus.OK.value(), servicePayPerUsePricesResponse.getStatus());

        List<FlavorPriceResult> payPerUsePriceResultList =
                objectMapper.readValue(servicePayPerUsePricesResponse.getContentAsString(),
                        new TypeReference<>() {
                        });
        assertEquals(flavorCount, payPerUsePriceResultList.size());
        assertTrue(payPerUsePriceResultList.stream().allMatch(price ->
                price.getRecurringPrice().getCurrency().equals(Currency.CNY)));

        // Test get prices by flavor with PAY_PER_USE
        changeFlavorPriceNotOneTime(templateId, ocl, Currency.CNY, PricingPeriod.MONTHLY);
        // mock ListProjectInvoker with Client failed.
        MockHttpServletResponse flavorPayPerUsePricesResponse =
                getServicePriceByFlavor(templateId, regionName, flavorName,
                        BillingMode.PAY_PER_USE);
        assertEquals(HttpStatus.OK.value(), flavorPayPerUsePricesResponse.getStatus());
        FlavorPriceResult flavorPayPerUsePriceResult =
                objectMapper.readValue(flavorPayPerUsePricesResponse.getContentAsString(),
                        FlavorPriceResult.class);
        assertNotNull(flavorPayPerUsePriceResult.getRecurringPrice());
        assertEquals(Currency.CNY, flavorPayPerUsePriceResult.getRecurringPrice().getCurrency());
        assertEquals(BillingMode.PAY_PER_USE, flavorPayPerUsePriceResult.getBillingMode());
        assertNull(flavorPayPerUsePriceResult.getOneTimePaymentPrice());
        deleteServiceTemplate(templateId);
    }


    @Test
    @WithJwt(file = "jwt_all_roles.json")
    void testServicePricingApiWithHuaweiCloudGlobalRestApi() throws Exception {
        // Setup
        Ocl ocl = oclLoader.getOcl(
                URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        String flavorName = ocl.getFlavors().getServiceFlavors().getFirst().getName();
        String regionName = ocl.getCloudServiceProvider().getRegions().getFirst().getName();
        ServiceTemplateDetailVo serviceTemplateDetails = registerServiceTemplate(ocl);
        UUID templateId = serviceTemplateDetails.getServiceTemplateId();
        int flavorCount = ocl.getFlavors().getServiceFlavors().size();
        // Setup get price with pay per use
        addCredentialForHuaweiCloud();
        mockListProjectInvokerWithClientThrowException();
        // Test get prices by service with PAY_PER_USE
        MockHttpServletResponse servicePayPerUsePricesResponse =
                getPricesByService(templateId, regionName, BillingMode.PAY_PER_USE);
        assertEquals(HttpStatus.OK.value(), servicePayPerUsePricesResponse.getStatus());

        List<FlavorPriceResult> payPerUsePriceResultList =
                objectMapper.readValue(servicePayPerUsePricesResponse.getContentAsString(),
                        new TypeReference<>() {
                        });
        assertEquals(flavorCount, payPerUsePriceResultList.size());
        assertTrue(payPerUsePriceResultList.stream().allMatch(price ->
                price.getRecurringPrice().getCurrency().equals(Currency.USD)));

        // Test get prices by flavor with PAY_PER_USE
        changeFlavorPriceNotOneTime(templateId, ocl, Currency.USD, PricingPeriod.DAILY);
        // mock ListProjectInvoker with Client failed.
        MockHttpServletResponse flavorPayPerUsePricesResponse =
                getServicePriceByFlavor(templateId, regionName, flavorName,
                        BillingMode.PAY_PER_USE);
        assertEquals(HttpStatus.OK.value(), flavorPayPerUsePricesResponse.getStatus());
        FlavorPriceResult flavorPayPerUsePriceResult =
                objectMapper.readValue(flavorPayPerUsePricesResponse.getContentAsString(),
                        FlavorPriceResult.class);
        assertNotNull(flavorPayPerUsePriceResult.getRecurringPrice());
        assertEquals(Currency.USD, flavorPayPerUsePriceResult.getRecurringPrice().getCurrency());
        assertEquals(BillingMode.PAY_PER_USE, flavorPayPerUsePriceResult.getBillingMode());
        assertNull(flavorPayPerUsePriceResult.getOneTimePaymentPrice());
        deleteServiceTemplate(templateId);
    }


    void mockListProjectInvoker() {
        KeystoneListProjectsResponse listProjectsResponse = new KeystoneListProjectsResponse();
        ProjectResult projectResult = new ProjectResult();
        projectResult.setId("huawei_project_test_id");
        listProjectsResponse.setProjects(List.of(projectResult));
        SyncInvoker<KeystoneListProjectsRequest, KeystoneListProjectsResponse> mockInvoker =
                mock(SyncInvoker.class);
        when(mockIamClient.keystoneListProjectsInvoker(any())).thenReturn(mockInvoker);
        when(mockInvoker.retryTimes(anyInt())).thenReturn(mockInvoker);
        when(mockInvoker.retryCondition(any())).thenReturn(mockInvoker);
        when(mockInvoker.backoffStrategy(any())).thenReturn(mockInvoker);
        when(mockInvoker.invoke()).thenReturn(listProjectsResponse);
    }

    void mockListProjectInvokerWithClientThrowException() {
        ClientRequestException clientRequestException = new ClientRequestException(403, "CBC.0150",
                "Access Denied", UUID.randomUUID().toString());

        SyncInvoker<KeystoneListProjectsRequest, KeystoneListProjectsResponse> mockInvoker =
                mock(SyncInvoker.class);
        when(mockIamClient.keystoneListProjectsInvoker(any())).thenReturn(mockInvoker);
        when(mockInvoker.retryTimes(anyInt())).thenReturn(mockInvoker);
        when(mockInvoker.retryCondition(any())).thenReturn(mockInvoker);
        when(mockInvoker.backoffStrategy(any())).thenReturn(mockInvoker);
        when(mockInvoker.invoke()).thenThrow(clientRequestException);
    }

    void mockListOnDemandResourceRatingsInvokerWithBssintlClient() {
        com.huaweicloud.sdk.bssintl.v2.model.ListOnDemandResourceRatingsResponse
                listOnDemandResourceRatingsResponse =
                new com.huaweicloud.sdk.bssintl.v2.model.ListOnDemandResourceRatingsResponse();
        listOnDemandResourceRatingsResponse.setAmount(BigDecimal.valueOf(10L));
        listOnDemandResourceRatingsResponse.setCurrency(Currency.USD.toValue());
        SyncInvoker<com.huaweicloud.sdk.bssintl.v2.model.ListOnDemandResourceRatingsRequest,
                com.huaweicloud.sdk.bssintl.v2.model.ListOnDemandResourceRatingsResponse>
                mockInvoker = mock(SyncInvoker.class);
        when(mockBssintlClient.listOnDemandResourceRatingsInvoker(any())).thenReturn(mockInvoker);
        when(mockInvoker.retryTimes(anyInt())).thenReturn(mockInvoker);
        when(mockInvoker.retryCondition(any())).thenReturn(mockInvoker);
        when(mockInvoker.backoffStrategy(any())).thenReturn(mockInvoker);
        when(mockInvoker.invoke()).thenReturn(listOnDemandResourceRatingsResponse);
    }

    private void mockListOnDemandResourceRatingsInvokerWithBssintlClientThrowException() {
        ClientRequestException clientRequestException = new ClientRequestException(403, "CBC.0150"
                , "Access Denied", UUID.randomUUID().toString());
        SyncInvoker<com.huaweicloud.sdk.bssintl.v2.model.ListOnDemandResourceRatingsRequest,
                com.huaweicloud.sdk.bssintl.v2.model.ListOnDemandResourceRatingsResponse>
                mockInvoker = mock(SyncInvoker.class);
        when(mockBssintlClient.listOnDemandResourceRatingsInvoker(any())).thenReturn(mockInvoker);
        when(mockInvoker.retryTimes(anyInt())).thenReturn(mockInvoker);
        when(mockInvoker.retryCondition(any())).thenReturn(mockInvoker);
        when(mockInvoker.backoffStrategy(any())).thenReturn(mockInvoker);
        when(mockInvoker.invoke()).thenThrow(clientRequestException);
    }


    void mockListOnDemandResourceRatingsInvokerWithBssClient() {
        ListOnDemandResourceRatingsResponse listOnDemandResourceRatingsResponse =
                new ListOnDemandResourceRatingsResponse();
        listOnDemandResourceRatingsResponse.setAmount(BigDecimal.valueOf(100L));
        listOnDemandResourceRatingsResponse.setCurrency(Currency.CNY.toValue());
        SyncInvoker<ListOnDemandResourceRatingsRequest, ListOnDemandResourceRatingsResponse>
                mockInvoker = mock(SyncInvoker.class);
        when(mockBssClient.listOnDemandResourceRatingsInvoker(any())).thenReturn(mockInvoker);
        when(mockInvoker.retryTimes(anyInt())).thenReturn(mockInvoker);
        when(mockInvoker.retryCondition(any())).thenReturn(mockInvoker);
        when(mockInvoker.backoffStrategy(any())).thenReturn(mockInvoker);
        when(mockInvoker.invoke()).thenReturn(listOnDemandResourceRatingsResponse);
    }

    void testServicePricingApiWithFlexibleEngine() throws Exception {
        // Setup
        Ocl ocl = oclLoader.getOcl(
                URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        ocl.getCloudServiceProvider().setName(Csp.FLEXIBLE_ENGINE);
        testGetServicePricing(ocl);
    }

    void changeFlavorPriceNotOneTime(UUID templateId, Ocl ocl, Currency currency,
                                     PricingPeriod pricingPeriod) {
        RatingMode ratingMode = ocl.getFlavors().getServiceFlavors().getFirst().getPricing();
        Price price = new Price();
        price.setCost(BigDecimal.valueOf(365 * 24L));
        price.setCurrency(currency);
        price.setPeriod(pricingPeriod);
        ratingMode.getResourceUsage().setLicensePrice(price);
        ratingMode.getResourceUsage().setMarkUpPrice(price);
        ServiceTemplateEntity serviceTemplate = serviceTemplateStorage.getServiceTemplateById(
                templateId);
        ocl.getFlavors().getServiceFlavors().getFirst().setPricing(ratingMode);
        serviceTemplate.setOcl(ocl);
        serviceTemplateStorage.storeAndFlush(serviceTemplate);
    }

    void testServicePricingApiWithOpenstack() throws Exception {
        // Setup
        Ocl ocl = oclLoader.getOcl(
                URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        ocl.getCloudServiceProvider().setName(Csp.OPENSTACK_TESTLAB);
        testGetServicePricing(ocl);
    }

    void testServicePricingApiWithPlusServer() throws Exception {
        Ocl ocl = oclLoader.getOcl(
                URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        ocl.getCloudServiceProvider().setName(Csp.PLUS_SERVER);
        testGetServicePricing(ocl);
    }

    private void testGetServicePricing(Ocl ocl) throws Exception {
        // Setup
        String flavorName = ocl.getFlavors().getServiceFlavors().getFirst().getName();
        String regionName = ocl.getCloudServiceProvider().getRegions().getFirst().getName();
        ServiceTemplateDetailVo serviceTemplate = registerServiceTemplate(ocl);
        UUID templateId = serviceTemplate.getServiceTemplateId();

        int flavorCount = ocl.getFlavors().getServiceFlavors().size();
        MockHttpServletResponse serviceFixedPricesResponse =
                getPricesByService(templateId, regionName, BillingMode.FIXED);

        List<FlavorPriceResult> fixedPriceResultList =
                objectMapper.readValue(serviceFixedPricesResponse.getContentAsString(),
                        new TypeReference<>() {
                        });
        assertEquals(HttpStatus.OK.value(), serviceFixedPricesResponse.getStatus());
        Assertions.assertEquals(flavorCount, fixedPriceResultList.size());


        MockHttpServletResponse servicePayPerUsePricesResponse =
                getPricesByService(templateId, regionName, BillingMode.PAY_PER_USE);

        List<FlavorPriceResult> payPerUsePriceResultList =
                objectMapper.readValue(servicePayPerUsePricesResponse.getContentAsString(),
                        new TypeReference<>() {
                        });
        assertEquals(HttpStatus.OK.value(), servicePayPerUsePricesResponse.getStatus());
        Assertions.assertEquals(flavorCount, payPerUsePriceResultList.size());

        MockHttpServletResponse fixedPriceResponse =
                getServicePriceByFlavor(templateId, regionName, flavorName, BillingMode.FIXED);
        FlavorPriceResult flavorPriceResult =
                objectMapper.readValue(fixedPriceResponse.getContentAsString(),
                        FlavorPriceResult.class);
        assertEquals(HttpStatus.OK.value(), fixedPriceResponse.getStatus());
        assertNotNull(flavorPriceResult.getRecurringPrice());
        assertNull(flavorPriceResult.getOneTimePaymentPrice());

        // Setup
        MockHttpServletResponse payPerUsePriceResponse =
                getServicePriceByFlavor(templateId, regionName, flavorName,
                        BillingMode.PAY_PER_USE);
        FlavorPriceResult flavorPriceResult1 =
                objectMapper.readValue(payPerUsePriceResponse.getContentAsString(),
                        FlavorPriceResult.class);
        assertEquals(HttpStatus.OK.value(), payPerUsePriceResponse.getStatus());
        assertNull(flavorPriceResult1.getRecurringPrice());
        assertNotNull(flavorPriceResult1.getOneTimePaymentPrice());

        // Setup
        changeFlavorPriceNotOneTime(templateId, ocl, Currency.CNY, PricingPeriod.YEARLY);
        MockHttpServletResponse payPerUsePriceResponse2 =
                getServicePriceByFlavor(templateId, regionName, flavorName,
                        BillingMode.PAY_PER_USE);
        FlavorPriceResult flavorPriceResult2 =
                objectMapper.readValue(payPerUsePriceResponse2.getContentAsString(),
                        FlavorPriceResult.class);
        assertEquals(HttpStatus.OK.value(), payPerUsePriceResponse2.getStatus());
        assertNotNull(flavorPriceResult2.getRecurringPrice());
        assertNull(flavorPriceResult2.getOneTimePaymentPrice());

        deleteServiceTemplate(templateId);
    }

    void testServicePricingApiThrowExceptions() throws Exception {
        // Setup
        UUID uuid = UUID.randomUUID();
        String regionName = "region";
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
                getServicePriceByFlavor(uuid, regionName, flavorName, billingMode);
        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), priceResponse1.getStatus());
        assertEquals(result1, priceResponse1.getContentAsString());

        // Setup
        Ocl ocl = oclLoader.getOcl(
                URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        ServiceTemplateDetailVo serviceTemplateDetails = registerServiceTemplate(ocl);
        UUID templateId = serviceTemplateDetails.getServiceTemplateId();
        expectedResult.setErrorMessage(
                String.format("Flavor %s not found in service template with id %s.",
                        flavorName, templateId));
        String result2 = objectMapper.writeValueAsString(expectedResult);
        // Run the test
        final MockHttpServletResponse priceResponse2 = getServicePriceByFlavor(templateId,
                regionName, flavorName, billingMode);
        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), priceResponse2.getStatus());
        assertEquals(result2, priceResponse2.getContentAsString());

        // Setup
        ServiceTemplateEntity serviceTemplate =
                serviceTemplateStorage.getServiceTemplateById(templateId);
        flavorName = ocl.getFlavors().getServiceFlavors().getFirst().getName();
        RatingMode ratingMode = ocl.getFlavors().getServiceFlavors().getFirst().getPricing();
        ratingMode.setResourceUsage(null);
        ratingMode.setFixedPrice(null);
        ocl.getFlavors().getServiceFlavors().getFirst().setPricing(ratingMode);
        serviceTemplate.setOcl(ocl);
        serviceTemplateStorage.storeAndFlush(serviceTemplate);
        expectedResult.setFlavorName(flavorName);
        expectedResult.setErrorMessage("BillingMode 'Fixed' can not be supported due to the "
                + "'FixedPrice' is null.");
        String result3 = objectMapper.writeValueAsString(expectedResult);
        // Run the test
        final MockHttpServletResponse priceResponse3 = getServicePriceByFlavor(templateId,
                regionName, flavorName, billingMode);
        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), priceResponse3.getStatus());
        assertEquals(result3, priceResponse3.getContentAsString());

        billingMode = BillingMode.PAY_PER_USE;
        expectedResult.setBillingMode(billingMode);
        expectedResult.setErrorMessage("BillingMode 'Pay-Per-Use' can not be supported "
                + "due to the 'ResourceUsage' is null.");
        String result4 = objectMapper.writeValueAsString(expectedResult);
        // Run the test
        final MockHttpServletResponse priceResponse4 = getServicePriceByFlavor(templateId,
                regionName, flavorName, billingMode);
        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), priceResponse4.getStatus());
        assertEquals(result4, priceResponse4.getContentAsString());

        // Setup
        ocl.getFlavors().getServiceFlavors().getFirst().setPricing(null);
        serviceTemplate.setOcl(ocl);
        serviceTemplateStorage.storeAndFlush(serviceTemplate);

        expectedResult.setErrorMessage(String.format("Flavor %s in service template with id %s "
                + "has no pricing.", flavorName, templateId));
        String result5 = objectMapper.writeValueAsString(expectedResult);
        // Run the test
        final MockHttpServletResponse priceResponse5 = getServicePriceByFlavor(templateId,
                regionName, flavorName, billingMode);

        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), priceResponse5.getStatus());
        assertEquals(result5, priceResponse5.getContentAsString());

        deleteServiceTemplate(templateId);
    }

    private MockHttpServletResponse getServicePriceByFlavor(
            UUID templateId, String region, String flavorName, BillingMode billingMode)
            throws Exception {
        return mockMvc.perform(
                        get("/xpanse/pricing/{templateId}/{region}/{billingMode}/{flavorName}",
                                templateId, region, billingMode, flavorName)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
    }

    private MockHttpServletResponse getPricesByService(
            UUID templateId, String region, BillingMode billingMode)
            throws Exception {
        return mockMvc.perform(
                        get("/xpanse/pricing/service/{templateId}/{region}/{billingMode}",
                                templateId, region, billingMode)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
    }

}
