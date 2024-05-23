/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.c4_soft.springaddons.security.oauth2.test.annotations.WithJwt;
import com.huaweicloud.sdk.bss.v2.model.ListOnDemandResourceRatingsRequest;
import com.huaweicloud.sdk.bss.v2.model.ListOnDemandResourceRatingsResponse;
import com.huaweicloud.sdk.core.invoker.SyncInvoker;
import com.huaweicloud.sdk.iam.v3.model.KeystoneListProjectsRequest;
import com.huaweicloud.sdk.iam.v3.model.KeystoneListProjectsResponse;
import com.huaweicloud.sdk.iam.v3.model.ProjectResult;
import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.eclipse.xpanse.modules.database.servicetemplate.DatabaseServiceTemplateStorage;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.models.billing.Price;
import org.eclipse.xpanse.modules.models.billing.RatingMode;
import org.eclipse.xpanse.modules.models.billing.ServicePrice;
import org.eclipse.xpanse.modules.models.billing.enums.BillingMode;
import org.eclipse.xpanse.modules.models.billing.enums.Currency;
import org.eclipse.xpanse.modules.models.billing.enums.PricingPeriod;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.response.Response;
import org.eclipse.xpanse.modules.models.response.ResultType;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
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
 * Test for ServiceTemplateManageApi.
 */
@SuppressWarnings("unchecked")
@Transactional
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {"spring.profiles.active=oauth,zitadel,zitadel-testbed"})
@AutoConfigureMockMvc
class ServicePricingApiTest extends ApisTestCommon {

    private final OclLoader oclLoader = new OclLoader();
    @Resource
    private DatabaseServiceTemplateStorage serviceTemplateStorage;

    @BeforeEach
    void setUp() {
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
        testServicePricingApiWithScs();
    }

    void testServicePricingApiWithHuaweiCloud() throws Exception {
        // Setup
        Ocl ocl = oclLoader.getOcl(
                URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        ocl.setName("HuaweiCloud-Service-Test-" + UUID.randomUUID());
        String flavorName = ocl.getFlavors().getServiceFlavors().getFirst().getName();
        String regionName = ocl.getCloudServiceProvider().getRegions().getFirst().getName();
        ServiceTemplateDetailVo serviceTemplateDetails = registerServiceTemplate(ocl);
        UUID templateId = serviceTemplateDetails.getId();
        MockHttpServletResponse fixedPriceResponse =
                getServicePriceByFlavor(templateId, regionName, flavorName, BillingMode.FIXED);
        ServicePrice servicePrice = objectMapper.readValue(fixedPriceResponse.getContentAsString(),
                ServicePrice.class);
        assertEquals(HttpStatus.OK.value(), fixedPriceResponse.getStatus());
        Assertions.assertNotNull(servicePrice.getRecurringPrice());
        Assertions.assertNull(servicePrice.getOneTimePaymentPrice());

        // Setup
        mockSdkClientsForHuaweiCloud();
        addCredentialForHuaweiCloud();
        mockListProjectInvoker();
        mockListOnDemandResourceRatingsInvoker();
        MockHttpServletResponse payPerUsePriceResponse =
                getServicePriceByFlavor(templateId, regionName, flavorName,
                        BillingMode.PAY_PER_USE);
        ServicePrice servicePrice1 =
                objectMapper.readValue(payPerUsePriceResponse.getContentAsString(),
                        ServicePrice.class);
        assertEquals(HttpStatus.OK.value(), payPerUsePriceResponse.getStatus());
        Assertions.assertNotNull(servicePrice1.getRecurringPrice());
        Assertions.assertNotNull(servicePrice1.getOneTimePaymentPrice());

        // Setup
        changeFlavorPriceNotOneTime(templateId, ocl);
        MockHttpServletResponse payPerUsePriceResponse2 =
                getServicePriceByFlavor(templateId, regionName, flavorName,
                        BillingMode.PAY_PER_USE);
        ServicePrice servicePrice2 =
                objectMapper.readValue(payPerUsePriceResponse2.getContentAsString(),
                        ServicePrice.class);
        assertEquals(HttpStatus.OK.value(), payPerUsePriceResponse2.getStatus());
        Assertions.assertNotNull(servicePrice2.getRecurringPrice());
        Assertions.assertNull(servicePrice2.getOneTimePaymentPrice());

        unregisterServiceTemplate(templateId);
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

    void mockListOnDemandResourceRatingsInvoker() {
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
        ocl.setName("FlexibleEngine-Service-Test-" + UUID.randomUUID());
        ocl.getCloudServiceProvider().setName(Csp.FLEXIBLE_ENGINE);
        testGetServicePricing(ocl);
    }

    void changeFlavorPriceNotOneTime(UUID templateId, Ocl ocl) {
        RatingMode ratingMode = ocl.getFlavors().getServiceFlavors().getFirst().getPricing();
        Price price = new Price();
        price.setCost(BigDecimal.valueOf(365 * 24L));
        price.setCurrency(Currency.CNY);
        price.setPeriod(PricingPeriod.YEARLY);
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
        ocl.setName("Openstack-Service-Test-" + UUID.randomUUID());
        ocl.getCloudServiceProvider().setName(Csp.OPENSTACK);
        testGetServicePricing(ocl);
    }

    void testServicePricingApiWithScs() throws Exception {
        Ocl ocl = oclLoader.getOcl(
                URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        ocl.setName("Scs-Service-Test-" + UUID.randomUUID());
        ocl.getCloudServiceProvider().setName(Csp.SCS);
        testGetServicePricing(ocl);
    }

    private void testGetServicePricing(Ocl ocl) throws Exception {
        // Setup
        String flavorName = ocl.getFlavors().getServiceFlavors().getFirst().getName();
        String regionName = ocl.getCloudServiceProvider().getRegions().getFirst().getName();
        ServiceTemplateDetailVo serviceTemplate = registerServiceTemplate(ocl);
        UUID templateId = serviceTemplate.getId();

        MockHttpServletResponse fixedPriceResponse =
                getServicePriceByFlavor(templateId, regionName, flavorName, BillingMode.FIXED);
        ServicePrice servicePrice = objectMapper.readValue(fixedPriceResponse.getContentAsString(),
                ServicePrice.class);
        assertEquals(HttpStatus.OK.value(), fixedPriceResponse.getStatus());
        Assertions.assertNotNull(servicePrice.getRecurringPrice());
        Assertions.assertNull(servicePrice.getOneTimePaymentPrice());

        // Setup
        MockHttpServletResponse payPerUsePriceResponse =
                getServicePriceByFlavor(templateId, regionName, flavorName,
                        BillingMode.PAY_PER_USE);
        ServicePrice servicePrice1 =
                objectMapper.readValue(payPerUsePriceResponse.getContentAsString(),
                        ServicePrice.class);
        assertEquals(HttpStatus.OK.value(), payPerUsePriceResponse.getStatus());
        Assertions.assertNull(servicePrice1.getRecurringPrice());
        Assertions.assertNotNull(servicePrice1.getOneTimePaymentPrice());

        // Setup
        changeFlavorPriceNotOneTime(templateId, ocl);
        MockHttpServletResponse payPerUsePriceResponse2 =
                getServicePriceByFlavor(templateId, regionName, flavorName,
                        BillingMode.PAY_PER_USE);
        ServicePrice servicePrice2 =
                objectMapper.readValue(payPerUsePriceResponse2.getContentAsString(),
                        ServicePrice.class);
        assertEquals(HttpStatus.OK.value(), payPerUsePriceResponse2.getStatus());
        Assertions.assertNotNull(servicePrice2.getRecurringPrice());
        Assertions.assertNull(servicePrice2.getOneTimePaymentPrice());

        unregisterServiceTemplate(templateId);
    }

    void testServicePricingApiThrowExceptions() throws Exception {
        // Setup
        UUID uuid = UUID.randomUUID();
        String regionName = "region";
        String flavorName = "flavor-error-test";
        BillingMode billingMode = BillingMode.FIXED;
        Response expectedResponse1 =
                Response.errorResponse(ResultType.SERVICE_TEMPLATE_NOT_REGISTERED,
                        Collections.singletonList(
                                String.format("Service template with id %s not found.",
                                        uuid)));
        String result1 = objectMapper.writeValueAsString(expectedResponse1);
        // Run the test
        final MockHttpServletResponse priceResponse1 =
                getServicePriceByFlavor(uuid, regionName, flavorName, billingMode);
        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), priceResponse1.getStatus());
        assertEquals(result1, priceResponse1.getContentAsString());

        // Setup
        Ocl ocl = oclLoader.getOcl(
                URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        ocl.setName("ServicePricingApi-error");
        ServiceTemplateDetailVo serviceTemplateDetails = registerServiceTemplate(ocl);
        UUID templateId = serviceTemplateDetails.getId();
        Response expectedResponse2 =
                Response.errorResponse(ResultType.SERVICE_PRICE_CALCULATION_FAILED,
                        Collections.singletonList(
                                String.format("Flavor %s not found in service template with id %s.",
                                        flavorName, templateId)));
        String result2 = objectMapper.writeValueAsString(expectedResponse2);
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
        Response expectedResponse3 =
                Response.errorResponse(ResultType.SERVICE_PRICE_CALCULATION_FAILED,
                        Collections.singletonList(
                                "BillingMode 'Pay-Per-Use' can not be supported due to the "
                                        + "'ResourceUsage' is null."));
        String result3 = objectMapper.writeValueAsString(expectedResponse3);
        // Run the test
        final MockHttpServletResponse priceResponse3 = getServicePriceByFlavor(templateId,
                regionName, flavorName, BillingMode.FIXED);
        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), priceResponse3.getStatus());
        assertEquals(result3, priceResponse3.getContentAsString());


        Response expectedResponse4 =
                Response.errorResponse(ResultType.SERVICE_PRICE_CALCULATION_FAILED,
                        Collections.singletonList("BillingMode 'Fixed' can not be supported due to "
                                + "the 'FixedPrice' is null."));
        String result4 = objectMapper.writeValueAsString(expectedResponse4);
        // Run the test
        final MockHttpServletResponse priceResponse4 = getServicePriceByFlavor(templateId,
                regionName, flavorName, BillingMode.PAY_PER_USE);
        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), priceResponse4.getStatus());
        assertEquals(result4, priceResponse4.getContentAsString());

        // Setup
        ocl.getFlavors().getServiceFlavors().getFirst().setPricing(null);
        serviceTemplate.setOcl(ocl);
        serviceTemplateStorage.storeAndFlush(serviceTemplate);
        Response expectedResponse5 =
                Response.errorResponse(ResultType.SERVICE_PRICE_CALCULATION_FAILED,
                        Collections.singletonList(String.format(
                                "Flavor %s in service template with id %s has no pricing.",
                                flavorName, templateId)));
        String result5 = objectMapper.writeValueAsString(expectedResponse5);
        // Run the test
        final MockHttpServletResponse priceResponse5 = getServicePriceByFlavor(templateId,
                regionName, flavorName, billingMode);

        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), priceResponse5.getStatus());
        assertEquals(result5, priceResponse5.getContentAsString());

        serviceTemplateStorage.removeById(templateId);
    }

    private MockHttpServletResponse getServicePriceByFlavor(
            UUID templateId, String region, String flavorName, BillingMode billingMode)
            throws Exception {
        return mockMvc.perform(
                        get("/xpanse/pricing/{templateId}/{region}/{flavorName}/{billingMode}",
                                templateId, region, flavorName, billingMode)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
    }

}
