/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.runtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.c4_soft.springaddons.security.oauth2.test.annotations.WithJwt;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import jakarta.transaction.Transactional;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.database.service.DatabaseDeployServiceStorage;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.database.service.ServiceQueryModel;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.models.billing.enums.BillingMode;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.response.Response;
import org.eclipse.xpanse.modules.models.response.ResultType;
import org.eclipse.xpanse.modules.models.servicetemplate.AvailabilityZoneConfig;
import org.eclipse.xpanse.modules.models.servicetemplate.DeployVariable;
import org.eclipse.xpanse.modules.models.servicetemplate.ModificationImpact;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.Region;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceFlavorWithPrice;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployVariableDataType;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployVariableKind;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceRegistrationState;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
import org.eclipse.xpanse.modules.models.servicetemplate.view.ServiceTemplateDetailVo;
import org.eclipse.xpanse.runtime.util.ApisTestCommon;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.semver4j.Semver;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * Test for ServiceTemplateManageApi.
 */
@Slf4j
@Transactional
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {"spring.profiles.active=oauth,zitadel,zitadel-testbed"})
@AutoConfigureMockMvc
class ServiceTemplateApiTest extends ApisTestCommon {

    private final OclLoader oclLoader = new OclLoader();

    @MockBean
    private DatabaseDeployServiceStorage mockDeployServiceStorage;

    @Test
    @WithJwt(file = "jwt_isv.json")
    void testManageApisWorkWell() throws Exception {
        testServiceTemplateApisWorkWell();
        testFetchApisWorkWell();
    }

    void testServiceTemplateApisWorkWell() throws Exception {
        // Setup register request
        Ocl ocl = oclLoader.getOcl(
                URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        ocl.setName("serviceTemplateApiTest-01");
        // Run the test
        final MockHttpServletResponse registerResponse = register(ocl);
        ServiceTemplateDetailVo serviceTemplateDetailVo =
                objectMapper.readValue(registerResponse.getContentAsString(),
                        ServiceTemplateDetailVo.class);
        // Verify the results
        assertEquals(HttpStatus.OK.value(), registerResponse.getStatus());
        assertEquals(ServiceRegistrationState.APPROVAL_PENDING,
                serviceTemplateDetailVo.getServiceRegistrationState());
        assertEquals(ocl.getCategory(), serviceTemplateDetailVo.getCategory());
        assertEquals(ocl.getCloudServiceProvider().getName(), serviceTemplateDetailVo.getCsp());
        assertEquals(ocl.getName().toLowerCase(Locale.ROOT), serviceTemplateDetailVo.getName());
        assertEquals(new Semver(ocl.getServiceVersion()).getVersion(),
                serviceTemplateDetailVo.getVersion());

        // Setup detail request
        UUID id = serviceTemplateDetailVo.getServiceTemplateId();
        String result = objectMapper.writeValueAsString(serviceTemplateDetailVo);
        // Run the test
        final MockHttpServletResponse detailResponse = detail(id);
        // Verify the results
        assertEquals(HttpStatus.OK.value(), detailResponse.getStatus());
        assertEquals(result, detailResponse.getContentAsString());


        // Setup list request
        List<ServiceTemplateDetailVo> serviceTemplateDetailVos = List.of(serviceTemplateDetailVo);
        // Run the test
        final MockHttpServletResponse response =
                listServiceTemplatesWithParams(ocl.getCategory().toValue(),
                        ocl.getCloudServiceProvider().getName().toValue(), ocl.getName(),
                        serviceTemplateDetailVo.getVersion(), ocl.getServiceHostingType().toValue(),
                        ServiceRegistrationState.APPROVAL_PENDING.toValue());
        // Verify the results
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertThat(
                serviceTemplateDetailVos).usingRecursiveFieldByFieldElementComparatorIgnoringFields(
                "lastModifiedTime").isEqualTo(Arrays.stream(
                objectMapper.readValue(response.getContentAsString(),
                        ServiceTemplateDetailVo[].class)).toList());


        // Setup update request
        Ocl updateOcl = oclLoader.getOcl(
                URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        updateOcl.setName("serviceTemplateApiTest-01");
        // Run the test
        final MockHttpServletResponse updateResponse = update(id, updateOcl);
        ServiceTemplateDetailVo updatedServiceTemplateDetailVo =
                objectMapper.readValue(updateResponse.getContentAsString(),
                        ServiceTemplateDetailVo.class);
        // Verify the results
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(serviceTemplateDetailVo.getServiceTemplateId(),
                updatedServiceTemplateDetailVo.getServiceTemplateId());


        // Setup unregister request
        // Run the test
        final MockHttpServletResponse unregisterResponse = unregister(id);
        ServiceTemplateDetailVo unregisteredServiceTemplateDetailVo =
                objectMapper.readValue(unregisterResponse.getContentAsString(),
                        ServiceTemplateDetailVo.class);
        // Verify the results
        assertEquals(HttpStatus.OK.value(), unregisterResponse.getStatus());
        assertEquals(unregisteredServiceTemplateDetailVo.getServiceRegistrationState(),
                ServiceRegistrationState.UNREGISTERED);

        // Setup reRegister request
        // Run the test
        final MockHttpServletResponse reRegisterResponse = reRegisterServiceTemplate(id);
        ServiceTemplateDetailVo reRegisteredServiceTemplateDetailVo =
                objectMapper.readValue(reRegisterResponse.getContentAsString(),
                        ServiceTemplateDetailVo.class);
        // Verify the results
        assertEquals(HttpStatus.OK.value(), reRegisterResponse.getStatus());
        assertEquals(reRegisteredServiceTemplateDetailVo.getServiceRegistrationState(),
                ServiceRegistrationState.APPROVAL_PENDING);


        // Setup delete request
        unregister(id);
        // Run the test
        final MockHttpServletResponse deleteResponse = deleteTemplate(id);
        // Verify the results
        assertEquals(HttpStatus.NO_CONTENT.value(), deleteResponse.getStatus());
    }

    void testFetchApisWorkWell() throws Exception {
        // Setup fetch request
        URL url = URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL();
        Ocl ocl = oclLoader.getOcl(url);
        // Run the test
        final MockHttpServletResponse fetchResponse = fetch(url.toString());
        ServiceTemplateDetailVo serviceTemplateDetailVo =
                objectMapper.readValue(fetchResponse.getContentAsString(),
                        ServiceTemplateDetailVo.class);
        // Verify the results
        assertEquals(HttpStatus.OK.value(), fetchResponse.getStatus());
        assertEquals(ServiceRegistrationState.APPROVAL_PENDING,
                serviceTemplateDetailVo.getServiceRegistrationState());
        assertEquals(ocl.getCategory(), serviceTemplateDetailVo.getCategory());
        assertEquals(ocl.getCloudServiceProvider().getName(), serviceTemplateDetailVo.getCsp());
        assertEquals(ocl.getName().toLowerCase(Locale.ROOT), serviceTemplateDetailVo.getName());
        assertEquals(ocl.getServiceVersion(), serviceTemplateDetailVo.getVersion());

        // Setup fetch update request
        URL updateUrl = URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL();
        UUID id = serviceTemplateDetailVo.getServiceTemplateId();
        // Run the test
        final MockHttpServletResponse fetchUpdateResponse = fetchUpdate(id, updateUrl.toString());
        ServiceTemplateDetailVo updatedServiceTemplateDetailVo =
                objectMapper.readValue(fetchUpdateResponse.getContentAsString(),
                        ServiceTemplateDetailVo.class);
        // Verify the results
        assertEquals(HttpStatus.OK.value(), fetchUpdateResponse.getStatus());
        assertEquals(serviceTemplateDetailVo.getServiceTemplateId(),
                updatedServiceTemplateDetailVo.getServiceTemplateId());

        deleteServiceTemplate(serviceTemplateDetailVo.getServiceTemplateId());
    }


    @Test
    @WithJwt(file = "jwt_isv.json")
    void testManageApisThrowsException() throws Exception {
        testManageApisThrowsServiceTemplateNotRegistered();
        testManageApisThrowsAccessDeniedException();

        testRegisterThrowsMethodArgumentNotValidException();
        testRegisterThrowsPluginNotFoundException();
        testRegisterThrowsTerraformExecutionException();
        testRegisterThrowsInvalidValueSchemaException();
        testRegisterThrowsServiceTemplateAlreadyRegistered();
        testRegisterThrowsInvalidServiceVersionException();
        testRegisterThrowsInvalidServiceFlavorsException();

        testFetchThrowsRuntimeException();
        testListServiceTemplatesThrowsException();
        testDeleteServiceTemplateThrowsException();
    }

    void testManageApisThrowsServiceTemplateNotRegistered() throws Exception {
        // Setup
        UUID id = UUID.randomUUID();
        Response expectedResponse =
                Response.errorResponse(ResultType.SERVICE_TEMPLATE_NOT_REGISTERED,
                        Collections.singletonList(
                                String.format("Service template with id %s not found.", id)));
        String result = objectMapper.writeValueAsString(expectedResponse);

        // Run the test -detail
        final MockHttpServletResponse detailResponse = detail(id);
        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), detailResponse.getStatus());
        assertEquals(result, detailResponse.getContentAsString());

        URL updateUrl =
                URI.create("file:src/test/resources/ocl_terraform_from_git_test.yml").toURL();
        // Run the test -update
        Ocl updateOcl = oclLoader.getOcl(updateUrl);
        final MockHttpServletResponse updateResponse = update(id, updateOcl);
        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), updateResponse.getStatus());
        assertEquals(result, updateResponse.getContentAsString());

        // Run the test -fetchUpdate
        final MockHttpServletResponse fetchUpdateResponse = fetchUpdate(id, updateUrl.toString());
        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), fetchUpdateResponse.getStatus());
        assertEquals(result, fetchUpdateResponse.getContentAsString());

        // Run the test -unregister
        final MockHttpServletResponse unregisterResponse = unregister(id);
        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), unregisterResponse.getStatus());
        assertEquals(result, unregisterResponse.getContentAsString());
    }

    void testManageApisThrowsAccessDeniedException() throws Exception {

        Response accessDeniedResponse = Response.errorResponse(ResultType.ACCESS_DENIED,
                Collections.singletonList("No permissions to view or manage service template "
                        + "belonging to other namespaces."));
        // Setup
        Ocl ocl = oclLoader.getOcl(
                URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        ocl.setName("serviceTemplateApiTest-02");
        MockHttpServletResponse response = register(ocl);
        ServiceTemplateDetailVo serviceTemplateDetail =
                objectMapper.readValue(response.getContentAsString(),
                        ServiceTemplateDetailVo.class);
        UUID id = serviceTemplateDetail.getServiceTemplateId();
        ServiceTemplateEntity serviceTemplateEntity =
                serviceTemplateStorage.getServiceTemplateById(id);
        serviceTemplateEntity.setNamespace("test");
        serviceTemplateStorage.storeAndFlush(serviceTemplateEntity);

        // Run the test detail
        final MockHttpServletResponse detailResponse = detail(id);
        // Verify the results
        assertEquals(HttpStatus.FORBIDDEN.value(), detailResponse.getStatus());
        assertEquals(objectMapper.writeValueAsString(accessDeniedResponse),
                detailResponse.getContentAsString());

        // Setup request update
        URL updateUrl =
                URI.create("file:src/test/resources/ocl_terraform_from_git_test.yml").toURL();
        // Run the test update
        Ocl updateOcl = oclLoader.getOcl(updateUrl);
        updateOcl.setName("serviceTemplateApiTest-02");
        final MockHttpServletResponse updateResponse = update(id, updateOcl);
        // Verify the results
        assertEquals(HttpStatus.FORBIDDEN.value(), updateResponse.getStatus());
        assertEquals(objectMapper.writeValueAsString(accessDeniedResponse),
                updateResponse.getContentAsString());

        // Run the test -fetchUpdate
        final MockHttpServletResponse fetchUpdateResponse = fetchUpdate(id, updateUrl.toString());
        // Verify the results
        assertEquals(HttpStatus.FORBIDDEN.value(), fetchUpdateResponse.getStatus());
        assertEquals(objectMapper.writeValueAsString(accessDeniedResponse),
                fetchUpdateResponse.getContentAsString());


        // Run the test unregister
        final MockHttpServletResponse unregisterResponse = unregister(id);
        // Verify the results
        assertEquals(HttpStatus.FORBIDDEN.value(), unregisterResponse.getStatus());
        assertEquals(objectMapper.writeValueAsString(accessDeniedResponse),
                unregisterResponse.getContentAsString());

        // Run the test re-register
        final MockHttpServletResponse reRegisterResponse = reRegisterServiceTemplate(id);
        // Verify the results
        assertEquals(HttpStatus.FORBIDDEN.value(), reRegisterResponse.getStatus());
        assertEquals(objectMapper.writeValueAsString(accessDeniedResponse),
                reRegisterResponse.getContentAsString());

        // Run the test deleteTemplate
        final MockHttpServletResponse deleteResponse = deleteTemplate(id);
        // Verify the results
        assertEquals(HttpStatus.FORBIDDEN.value(), deleteResponse.getStatus());
        assertEquals(objectMapper.writeValueAsString(accessDeniedResponse),
                deleteResponse.getContentAsString());

        deleteServiceTemplate(id);
    }

    void testRegisterThrowsMethodArgumentNotValidException() throws Exception {
        // Setup
        Ocl ocl = oclLoader.getOcl(
                URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        ocl.setCategory(null);
        BillingMode duplicateBillingMode = ocl.getBilling().getBillingModes().getFirst();
        ocl.getBilling().getBillingModes().add(duplicateBillingMode);
        Region duplicateRegion = ocl.getCloudServiceProvider().getRegions().getFirst();
        ocl.getCloudServiceProvider().getRegions().add(duplicateRegion);
        AvailabilityZoneConfig duplicateAvailabilityZoneConfig =
                ocl.getDeployment().getServiceAvailabilityConfigs().getFirst();
        ocl.getDeployment().getServiceAvailabilityConfigs().add(duplicateAvailabilityZoneConfig);
        DeployVariable duplicateDeployVariable = ocl.getDeployment().getVariables().getFirst();
        ocl.getDeployment().getVariables().add(duplicateDeployVariable);
        ServiceFlavorWithPrice duplicateFlavor = ocl.getFlavors().getServiceFlavors().getFirst();
        ocl.getFlavors().getServiceFlavors().add(duplicateFlavor);
        String duplicateEmail = ocl.getServiceProviderContactDetails().getEmails().getFirst();
        ocl.getServiceProviderContactDetails().getEmails().add(duplicateEmail);
        String duplicatePhone = ocl.getServiceProviderContactDetails().getPhones().getFirst();
        ocl.getServiceProviderContactDetails().getPhones().add(duplicatePhone);
        String duplicateWebsite = ocl.getServiceProviderContactDetails().getWebsites().getFirst();
        ocl.getServiceProviderContactDetails().getWebsites().add(duplicateWebsite);
        String duplicateAddress = ocl.getServiceProviderContactDetails().getChats().getFirst();
        ocl.getServiceProviderContactDetails().getChats().add(duplicateAddress);
        // Run the test
        final MockHttpServletResponse response = register(ocl);
        Response result = objectMapper.readValue(response.getContentAsString(), Response.class);
        // Verify the results
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), response.getStatus());
        assertEquals(result.getResultType(), ResultType.UNPROCESSABLE_ENTITY);
        assertFalse(result.getDetails().isEmpty());
    }

    void testRegisterThrowsPluginNotFoundException() throws Exception {
        // Setup
        Ocl ocl = oclLoader.getOcl(
                URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        Csp csp = Csp.AWS;
        ocl.getCloudServiceProvider().setName(csp);
        Response expectedResponse = Response.errorResponse(ResultType.PLUGIN_NOT_FOUND,
                Collections.singletonList(
                        String.format("Can't find suitable plugin for the Csp %s", csp.toValue())));
        // Run the test
        final MockHttpServletResponse response = register(ocl);
        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
        assertEquals(objectMapper.writeValueAsString(expectedResponse),
                response.getContentAsString());
    }

    void testRegisterThrowsTerraformExecutionException() throws Exception {
        // Setup
        Ocl ocl = oclLoader.getOcl(
                URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        ocl.getDeployment().setDeployer("error_" + ocl.getDeployment().getDeployer());
        Response expectedResponse = Response.errorResponse(ResultType.TERRAFORM_EXECUTION_FAILED,
                Collections.singletonList("Executor Exception:TFExecutor.tfInit failed"));
        // Run the test
        final MockHttpServletResponse response = register(ocl);
        Response responseModel =
                objectMapper.readValue(response.getContentAsString(), Response.class);
        // Verify the results
        assertEquals(HttpStatus.BAD_GATEWAY.value(), response.getStatus());
        assertEquals(responseModel.getResultType(), expectedResponse.getResultType());
        assertEquals(responseModel.getSuccess(), expectedResponse.getSuccess());
    }

    void testRegisterThrowsInvalidValueSchemaException() throws Exception {
        // Setup
        Ocl ocl = oclLoader.getOcl(
                URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());

        ocl.getDeployment().setServiceAvailabilityConfigs(null);
        DeployVariable deployVariable = ocl.getDeployment().getVariables().getLast();
        DeployVariable deployVariableWithRepeatName = new DeployVariable();
        BeanUtils.copyProperties(deployVariable, deployVariableWithRepeatName);
        deployVariableWithRepeatName.setValue("newValue");
        ocl.getDeployment().getVariables().add(deployVariableWithRepeatName);

        String errorMessage = String.format(
                "The deploy variable configuration list with duplicated variable name %s",
                deployVariableWithRepeatName.getName());
        Response expectedResponse =
                Response.errorResponse(ResultType.VARIABLE_SCHEMA_DEFINITION_INVALID,
                        Collections.singletonList(errorMessage));
        // Run the test
        final MockHttpServletResponse response = register(ocl);

        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
        assertEquals(objectMapper.writeValueAsString(expectedResponse),
                response.getContentAsString());

        // Setup
        Ocl ocl2 = oclLoader.getOcl(
                URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        AvailabilityZoneConfig availabilityZoneConfig =
                ocl2.getDeployment().getServiceAvailabilityConfigs().getFirst();
        AvailabilityZoneConfig availabilityZoneConfigWithRepeatName =
                new AvailabilityZoneConfig();
        BeanUtils.copyProperties(availabilityZoneConfig, availabilityZoneConfigWithRepeatName);
        availabilityZoneConfigWithRepeatName.setDisplayName("newDisplayName");
        ocl2.getDeployment().getServiceAvailabilityConfigs()
                .add(availabilityZoneConfigWithRepeatName);

        String errorMessage2 = String.format(
                "The availability zone configuration list with duplicated variable name %s",
                availabilityZoneConfig.getVarName());
        Response expectedResponse2 =
                Response.errorResponse(ResultType.VARIABLE_SCHEMA_DEFINITION_INVALID,
                        Collections.singletonList(errorMessage2));
        // Run the test
        final MockHttpServletResponse response2 = register(ocl2);

        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), response2.getStatus());
        assertEquals(objectMapper.writeValueAsString(expectedResponse2),
                response2.getContentAsString());

        // Setup
        Ocl ocl3 = oclLoader.getOcl(
                URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        DeployVariable errorVariable = new DeployVariable();
        errorVariable.setKind(DeployVariableKind.VARIABLE);
        errorVariable.setDataType(DeployVariableDataType.STRING);
        errorVariable.setMandatory(true);
        errorVariable.setName("errorVarName");
        errorVariable.setDescription("description");
        errorVariable.setExample("example");
        String errorSchemaKey = "errorSchemaKey";
        errorVariable.setValue("errorValue");
        errorVariable.setValueSchema(Collections.singletonMap(errorSchemaKey, "errorSchemaValue"));
        ModificationImpact modificationImpact = new ModificationImpact();
        modificationImpact.setIsDataLost(true);
        modificationImpact.setIsServiceInterrupted(true);
        errorVariable.setModificationImpact(modificationImpact);
        ocl3.getDeployment().setVariables(List.of(errorVariable));

        String errorMessage3 = String.format("Value schema key %s in deploy variable %s is invalid",
                errorSchemaKey, errorVariable.getName());
        Response expectedResponse3 =
                Response.errorResponse(ResultType.VARIABLE_SCHEMA_DEFINITION_INVALID,
                        Collections.singletonList(errorMessage3));
        // Run the test
        final MockHttpServletResponse response3 = register(ocl3);

        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), response3.getStatus());
        assertEquals(objectMapper.writeValueAsString(expectedResponse3),
                response3.getContentAsString());
    }

    void testRegisterThrowsServiceTemplateAlreadyRegistered() throws Exception {
        // Setup
        Ocl ocl = oclLoader.getOcl(
                URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        ocl.setName("serviceTemplateApiTest-03");
        MockHttpServletResponse response = register(ocl);
        ServiceTemplateDetailVo serviceTemplateDetail =
                objectMapper.readValue(response.getContentAsString(),
                        ServiceTemplateDetailVo.class);
        Response expectedResponse =
                Response.errorResponse(ResultType.SERVICE_TEMPLATE_ALREADY_REGISTERED,
                        Collections.singletonList(
                                String.format("Service template already registered with id %s",
                                        serviceTemplateDetail.getServiceTemplateId())));
        String result = objectMapper.writeValueAsString(expectedResponse);
        // Run the test
        final MockHttpServletResponse registerSameResponse = register(ocl);
        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), registerSameResponse.getStatus());
        assertEquals(registerSameResponse.getContentAsString(), result);
        unregister(serviceTemplateDetail.getServiceTemplateId());
        deleteTemplate(serviceTemplateDetail.getServiceTemplateId());
    }

    void testRegisterThrowsInvalidServiceVersionException() throws Exception {
        // Setup
        Ocl ocl = oclLoader.getOcl(
                URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        ocl.setName("serviceTemplateApiTest-04");
        String serviceVersion = "ErrorVersion";
        ocl.setServiceVersion(serviceVersion);
        String errorMsg1 = String.format("The service version %s is a invalid semver version.",
                serviceVersion);
        Response expectedResponse1 = Response.errorResponse(ResultType.INVALID_SERVICE_VERSION,
                Collections.singletonList(errorMsg1));
        // Run the test
        final MockHttpServletResponse registerResponse1 = register(ocl);
        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), registerResponse1.getStatus());
        assertEquals(registerResponse1.getContentAsString(),
                objectMapper.writeValueAsString(expectedResponse1));

        String existingServiceVersion = "1.0.0";
        ocl.setServiceVersion(existingServiceVersion);
        ServiceTemplateDetailVo serviceTemplate = registerServiceTemplate(ocl);


        // Setup
        String lowerVersion = "0.0.1";
        ocl.setServiceVersion(lowerVersion);
        String errorMsg2 = String.format("The version %s of service must be higher than the"
                        + " highest version %s of the registered services with same name", lowerVersion,
                existingServiceVersion);
        Response expectedResponse2 = Response.errorResponse(ResultType.INVALID_SERVICE_VERSION,
                Collections.singletonList(errorMsg2));
        // Run the test
        final MockHttpServletResponse registerResponse2 = register(ocl);
        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), registerResponse2.getStatus());
        assertEquals(registerResponse2.getContentAsString(),
                objectMapper.writeValueAsString(expectedResponse2));

        unregister(serviceTemplate.getServiceTemplateId());
        deleteTemplate(serviceTemplate.getServiceTemplateId());
    }

    void testRegisterThrowsInvalidServiceFlavorsException() throws Exception {
        // Setup
        Ocl ocl = oclLoader.getOcl(
                URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        ServiceFlavorWithPrice favor = ocl.getFlavors().getServiceFlavors().getFirst();
        ServiceFlavorWithPrice duplicatedFlavor = new ServiceFlavorWithPrice();
        BeanUtils.copyProperties(favor, duplicatedFlavor);
        duplicatedFlavor.setFeatures(List.of("Feature"));
        ocl.getFlavors().getServiceFlavors().add(duplicatedFlavor);
        String duplicatedFlavorName = duplicatedFlavor.getName();
        String errorMsg1 =
                String.format("Service flavor with name %s is duplicated.", duplicatedFlavorName);
        ServiceFlavorWithPrice errorBillingFlavor = ocl.getFlavors().getServiceFlavors().getLast();
        String errorBillingFlavorName = errorBillingFlavor.getName();
        errorBillingFlavor.getPricing().setFixedPrice(null);
        errorBillingFlavor.getPricing().setResourceUsage(null);
        String errorMsg2 = String.format("Service flavor %s has no 'resourceUsage' defined in "
                + "'pricing' for the billing mode 'pay-per-use'.", errorBillingFlavorName);
        String errorMsg3 = String.format("Service flavor %s has no 'fixedPrice' defined in "
                + "'pricing' for the billing mode 'fixed'.", errorBillingFlavorName);
        List<String> expectedDetails = Arrays.asList(errorMsg1, errorMsg2, errorMsg3);

        // Run the test
        final MockHttpServletResponse registerResponse = register(ocl);
        Response response =
                objectMapper.readValue(registerResponse.getContentAsString(), Response.class);
        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), registerResponse.getStatus());
        assertEquals(response.getResultType(), ResultType.INVALID_SERVICE_FLAVORS);
        assertTrue(response.getDetails().containsAll(expectedDetails));
    }


    void testFetchThrowsRuntimeException() throws Exception {
        // Setup
        String fileUrl =
                URI.create("file:src/test/resources/ocl_terraform_error.yml").toURL().toString();
        Response expectedResponse = Response.errorResponse(ResultType.RUNTIME_ERROR,
                Collections.singletonList("java.io.FileNotFoundException:"));

        // Run the test
        final MockHttpServletResponse response = fetch(fileUrl);
        Response responseModel =
                objectMapper.readValue(response.getContentAsString(), Response.class);
        // Verify the results
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatus());
        assertEquals(responseModel.getResultType(), expectedResponse.getResultType());
        assertEquals(responseModel.getSuccess(), expectedResponse.getSuccess());
    }

    void testListServiceTemplatesThrowsException() throws Exception {
        // Setup
        String errorMessage = "Failed to convert value of type 'java.lang.String' to required type";
        Response expectedResponse =
                Response.errorResponse(ResultType.UNPROCESSABLE_ENTITY, List.of(errorMessage));

        // Run the test
        final MockHttpServletResponse response =
                listServiceTemplatesWithParams("errorCategory", null, null, null, null, null);
        Response resultResponse =
                objectMapper.readValue(response.getContentAsString(), Response.class);

        // Verify the results
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), response.getStatus());
        assertEquals(expectedResponse.getSuccess(), resultResponse.getSuccess());
        assertEquals(expectedResponse.getResultType(), resultResponse.getResultType());
    }

    void testDeleteServiceTemplateThrowsException() throws Exception {
        // Setup
        Ocl ocl = oclLoader.getOcl(
                URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        ocl.setName("serviceTemplateApiTest-05");
        ServiceTemplateDetailVo serviceTemplate = registerServiceTemplate(ocl);
        UUID id = serviceTemplate.getServiceTemplateId();
        // Setup
        String errorMsg = String.format("Service template with id %s is not unregistered.", id);
        Response expectedResponse =
                Response.errorResponse(ResultType.SERVICE_TEMPLATE_STILL_IN_USE, List.of(errorMsg));
        MockHttpServletResponse deleteResponse = deleteTemplate(id);

        assertEquals(HttpStatus.BAD_REQUEST.value(), deleteResponse.getStatus());
        assertEquals(deleteResponse.getContentAsString(),
                objectMapper.writeValueAsString(expectedResponse));

        String errorMsg2 = String.format("Service template with id %s is still in use.", id);
        Response expectedResponse2 =
                Response.errorResponse(ResultType.SERVICE_TEMPLATE_STILL_IN_USE,
                        List.of(errorMsg2));
        unregister(serviceTemplate.getServiceTemplateId());
        when(mockDeployServiceStorage.listServices(any(ServiceQueryModel.class))).thenReturn(
                List.of(new DeployServiceEntity()));
        MockHttpServletResponse deleteResponse2 = deleteTemplate(id);
        assertEquals(HttpStatus.BAD_REQUEST.value(), deleteResponse2.getStatus());
        assertEquals(deleteResponse2.getContentAsString(),
                objectMapper.writeValueAsString(expectedResponse2));

        deleteTemplate(serviceTemplate.getServiceTemplateId());
    }

    MockHttpServletResponse register(Ocl ocl) throws Exception {
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        String requestBody = yamlMapper.writeValueAsString(ocl);
        return mockMvc.perform(post("/xpanse/service_templates").content(requestBody)
                        .contentType("application/x-yaml").accept(MediaType.APPLICATION_JSON)).andReturn()
                .getResponse();
    }

    MockHttpServletResponse update(UUID id, Ocl ocl) throws Exception {
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        String requestBody = yamlMapper.writeValueAsString(ocl);
        return mockMvc.perform(put("/xpanse/service_templates/{id}", id).content(requestBody)
                        .contentType("application/x-yaml").accept(MediaType.APPLICATION_JSON)).andReturn()
                .getResponse();
    }

    MockHttpServletResponse fetch(String url) throws Exception {
        return mockMvc.perform(post("/xpanse/service_templates/file").param("oclLocation", url)
                .accept(MediaType.APPLICATION_JSON)).andReturn().getResponse();
    }

    MockHttpServletResponse fetchUpdate(UUID id, String url) throws Exception {
        return mockMvc.perform(
                put("/xpanse/service_templates/file/{id}", id).param("oclLocation", url)
                        .accept(MediaType.APPLICATION_JSON)).andReturn().getResponse();
    }

    MockHttpServletResponse detail(UUID id) throws Exception {
        return mockMvc.perform(
                        get("/xpanse/service_templates/{id}", id).accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
    }

    MockHttpServletResponse unregister(UUID id) throws Exception {
        return mockMvc.perform(
                        put("/xpanse/service_templates/unregister/{id}", id).accept(
                                MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
    }

    MockHttpServletResponse reRegisterServiceTemplate(UUID id) throws Exception {
        return mockMvc.perform(
                        put("/xpanse/service_templates/re-register/{id}", id).accept(
                                MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
    }

    MockHttpServletResponse deleteTemplate(UUID id) throws Exception {
        return mockMvc.perform(
                        delete("/xpanse/service_templates/{id}", id).accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
    }

    MockHttpServletResponse listServiceTemplatesWithParams(String categoryName, String cspName,
                                                           String serviceName,
                                                           String serviceVersion,
                                                           String serviceHostingType,
                                                           String serviceRegistrationState)
            throws Exception {
        MockHttpServletRequestBuilder getRequestBuilder = get("/xpanse/service_templates");
        if (StringUtils.isNotBlank(categoryName)) {
            getRequestBuilder = getRequestBuilder.param("categoryName", categoryName);
        }
        if (StringUtils.isNotBlank(cspName)) {
            getRequestBuilder = getRequestBuilder.param("cspName", cspName);
        }
        if (StringUtils.isNotBlank(serviceName)) {
            getRequestBuilder = getRequestBuilder.param("serviceName", serviceName);
        }
        if (StringUtils.isNotBlank(serviceVersion)) {
            getRequestBuilder = getRequestBuilder.param("serviceVersion", serviceVersion);
        }
        if (StringUtils.isNotBlank(serviceHostingType)) {
            getRequestBuilder = getRequestBuilder.param("serviceHostingType", serviceHostingType);
        }
        if (StringUtils.isNotBlank(serviceRegistrationState)) {
            getRequestBuilder =
                    getRequestBuilder.param("serviceRegistrationState", serviceRegistrationState);
        }
        return mockMvc.perform(getRequestBuilder).andReturn().getResponse();
    }

}
