/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.runtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.c4_soft.springaddons.security.oauth2.test.annotations.WithJwt;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import jakarta.annotation.Resource;
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
import org.eclipse.xpanse.modules.database.servicetemplate.DatabaseServiceTemplateStorage;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.models.response.Response;
import org.eclipse.xpanse.modules.models.response.ResultType;
import org.eclipse.xpanse.modules.models.servicetemplate.AvailabilityZoneConfig;
import org.eclipse.xpanse.modules.models.servicetemplate.DeployVariable;
import org.eclipse.xpanse.modules.models.servicetemplate.ModificationImpact;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployVariableDataType;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployVariableKind;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceRegistrationState;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
import org.eclipse.xpanse.modules.models.servicetemplate.view.ServiceTemplateDetailVo;
import org.eclipse.xpanse.runtime.util.ApisTestCommon;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
    @Resource
    private DatabaseServiceTemplateStorage serviceTemplateStorage;

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
        assertEquals(ocl.getCloudServiceProvider().getName(),
                serviceTemplateDetailVo.getCsp());
        assertEquals(ocl.getName().toLowerCase(Locale.ROOT),
                serviceTemplateDetailVo.getName());
        assertEquals(ocl.getServiceVersion(), serviceTemplateDetailVo.getVersion());

        // Setup detail request
        UUID id = serviceTemplateDetailVo.getId();
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
                        ocl.getCloudServiceProvider().getName().toValue(),
                        ocl.getName(), ocl.getServiceVersion(),
                        ocl.getServiceHostingType().toValue(),
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
        assertEquals(serviceTemplateDetailVo.getId(), updatedServiceTemplateDetailVo.getId());

        // Setup unregister request
        Response expectedResponse = Response.successResponse(Collections.singletonList(
                String.format("Unregister service template using id %s successful.", id)));
        // Run the test
        final MockHttpServletResponse unregisterResponse = unregister(id);
        // Verify the results
        assertEquals(HttpStatus.OK.value(), unregisterResponse.getStatus());
        assertEquals(unregisterResponse.getContentAsString(),
                objectMapper.writeValueAsString(expectedResponse));
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
        assertEquals(ocl.getCloudServiceProvider().getName(),
                serviceTemplateDetailVo.getCsp());
        assertEquals(ocl.getName().toLowerCase(Locale.ROOT),
                serviceTemplateDetailVo.getName());
        assertEquals(ocl.getServiceVersion(), serviceTemplateDetailVo.getVersion());

        // Setup fetch update request
        URL updateUrl =
                URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL();
        UUID id = serviceTemplateDetailVo.getId();
        // Run the test
        final MockHttpServletResponse fetchUpdateResponse = fetchUpdate(id, updateUrl.toString());
        ServiceTemplateDetailVo updatedServiceTemplateDetailVo =
                objectMapper.readValue(fetchUpdateResponse.getContentAsString(),
                        ServiceTemplateDetailVo.class);
        // Verify the results
        assertEquals(HttpStatus.OK.value(), fetchUpdateResponse.getStatus());
        assertEquals(serviceTemplateDetailVo.getId(), updatedServiceTemplateDetailVo.getId());
        unregister(id);
    }


    @Test
    @WithJwt(file = "jwt_isv.json")
    void testManageApisThrowsException() throws Exception {
        testManageApisThrowsServiceTemplateNotRegistered();
        testManageApisThrowsAccessDeniedException();

        testRegisterThrowsMethodArgumentNotValidException();
        testRegisterThrowsTerraformExecutionException();
        testRegisterThrowsInvalidValueSchemaException();
        testRegisterThrowsServiceTemplateAlreadyRegistered();

        testFetchThrowsRuntimeException();
        testListServiceTemplatesThrowsException();
    }

    void testManageApisThrowsServiceTemplateNotRegistered() throws Exception {
        // Setup
        UUID uuid = UUID.randomUUID();
        Response expectedResponse =
                Response.errorResponse(ResultType.SERVICE_TEMPLATE_NOT_REGISTERED,
                        Collections.singletonList(
                                String.format("Service template with id %s not found.", uuid)));
        String result = objectMapper.writeValueAsString(expectedResponse);

        // Run the test -detail
        final MockHttpServletResponse detailResponse = detail(uuid);
        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), detailResponse.getStatus());
        assertEquals(result, detailResponse.getContentAsString());

        URL updateUrl =
                URI.create("file:src/test/resources/ocl_terraform_from_git_test.yml").toURL();
        // Run the test -update
        Ocl updateOcl = oclLoader.getOcl(updateUrl);
        final MockHttpServletResponse updateResponse = update(uuid, updateOcl);
        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), updateResponse.getStatus());
        assertEquals(result, updateResponse.getContentAsString());

        // Run the test -fetchUpdate
        final MockHttpServletResponse fetchUpdateResponse = fetchUpdate(uuid, updateUrl.toString());
        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), fetchUpdateResponse.getStatus());
        assertEquals(result, fetchUpdateResponse.getContentAsString());

        // Run the test -unregister
        final MockHttpServletResponse unregisterResponse = unregister(uuid);
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
        ServiceTemplateDetailVo serviceTemplateDetail = objectMapper.readValue(
                response.getContentAsString(), ServiceTemplateDetailVo.class);
        UUID uuid = serviceTemplateDetail.getId();
        ServiceTemplateEntity serviceTemplateEntity =
                serviceTemplateStorage.getServiceTemplateById(uuid);
        serviceTemplateEntity.setNamespace("test");
        serviceTemplateStorage.storeAndFlush(serviceTemplateEntity);

        // Run the test detail
        final MockHttpServletResponse detailResponse = detail(uuid);
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
        final MockHttpServletResponse updateResponse = update(uuid, updateOcl);
        // Verify the results
        assertEquals(HttpStatus.FORBIDDEN.value(), updateResponse.getStatus());
        assertEquals(objectMapper.writeValueAsString(accessDeniedResponse),
                updateResponse.getContentAsString());

        // Run the test -fetchUpdate
        final MockHttpServletResponse fetchUpdateResponse = fetchUpdate(uuid, updateUrl.toString());
        // Verify the results
        assertEquals(HttpStatus.FORBIDDEN.value(), fetchUpdateResponse.getStatus());
        assertEquals(objectMapper.writeValueAsString(accessDeniedResponse),
                fetchUpdateResponse.getContentAsString());


        // Run the test unregister
        final MockHttpServletResponse unregisterResponse = unregister(uuid);
        // Verify the results
        assertEquals(HttpStatus.FORBIDDEN.value(), unregisterResponse.getStatus());
        assertEquals(objectMapper.writeValueAsString(accessDeniedResponse),
                unregisterResponse.getContentAsString());

        unregister(uuid);
    }

    void testRegisterThrowsMethodArgumentNotValidException() throws Exception {
        // Setup
        Ocl ocl = oclLoader.getOcl(
                URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        ocl.setBilling(null);
        Response expectedResponse = Response.errorResponse(ResultType.UNPROCESSABLE_ENTITY,
                Collections.singletonList("billing:must not be null"));
        // Run the test
        final MockHttpServletResponse response = register(ocl);
        // Verify the results
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), response.getStatus());
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

        ocl.getDeployment().setServiceAvailability(null);
        DeployVariable deployVariableWithRepeatName =
                ocl.getDeployment().getVariables().getLast();
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
                ocl2.getDeployment().getServiceAvailability().getFirst();
        availabilityZoneConfig.setDisplayName("newDisplayName");
        ocl2.getDeployment().getServiceAvailability().add(availabilityZoneConfig);

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

        String errorMessage3 = String.format(
                "Value schema key %s in deploy variable %s is invalid", errorSchemaKey,
                errorVariable.getName());
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
        ServiceTemplateDetailVo serviceTemplateDetail = objectMapper.readValue(
                response.getContentAsString(), ServiceTemplateDetailVo.class);
        Response expectedResponse =
                Response.errorResponse(ResultType.SERVICE_TEMPLATE_ALREADY_REGISTERED,
                        Collections.singletonList(
                                String.format("Service template already registered with id %s",
                                        serviceTemplateDetail.getId())));
        String result = objectMapper.writeValueAsString(expectedResponse);
        // Run the test
        final MockHttpServletResponse registerSameResponse = register(ocl);
        // Verify the results
        assertEquals(HttpStatus.BAD_REQUEST.value(), registerSameResponse.getStatus());
        assertEquals(registerSameResponse.getContentAsString(), result);
        unregister(serviceTemplateDetail.getId());
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
