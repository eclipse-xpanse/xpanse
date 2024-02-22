package org.eclipse.xpanse.runtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.c4_soft.springaddons.security.oauth2.test.annotations.WithJwt;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.OffsetDateTimeSerializer;
import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.common.enums.Category;
import org.eclipse.xpanse.modules.models.response.Response;
import org.eclipse.xpanse.modules.models.response.ResultType;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceRegistrationState;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
import org.eclipse.xpanse.modules.models.servicetemplate.view.ServiceTemplateDetailVo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@Slf4j
@Transactional
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {"spring.profiles.active=zitadel,zitadel-testbed"})
@AutoConfigureMockMvc
class CspServiceTemplateApiTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static String id;
    private static ServiceTemplateDetailVo serviceTemplateDetailVo;
    private static Ocl ocl;
    @Resource
    private MockMvc mockMvc;

    @BeforeAll
    static void configureObjectMapper() {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new SimpleModule().addSerializer(OffsetDateTime.class,
                OffsetDateTimeSerializer.INSTANCE));
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false);

    }

    @Test
    @WithJwt(file = "jwt_admin_csp.json")
    void testCspManageServiceTemplates() throws Exception {
        testListAllServiceTemplatesThrowsException();
        testRegister();
        testListAllServiceTemplates();
        testUnregister();
        testListAllServiceTemplatesReturnsNoItems();
    }


    void testRegister() throws Exception {
        // Setup
        ocl = new OclLoader().getOcl(URI.create("file:src/test/resources/ocl_test.yaml").toURL());
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        String requestBody = yamlMapper.writeValueAsString(ocl);

        // Run the test
        final MockHttpServletResponse registerResponse = mockMvc.perform(
                        post("/xpanse/service_templates").content(requestBody)
                                .contentType("application/x-yaml").accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        serviceTemplateDetailVo = objectMapper.readValue(registerResponse.getContentAsString(),
                ServiceTemplateDetailVo.class);
        id = serviceTemplateDetailVo.getId().toString();

        // Verify the results
        Assertions.assertEquals(HttpStatus.OK.value(), registerResponse.getStatus());
        Assertions.assertEquals(ServiceRegistrationState.APPROVAL_PENDING,
                serviceTemplateDetailVo.getServiceRegistrationState());
        Assertions.assertEquals(ocl.getCategory(), serviceTemplateDetailVo.getCategory());
        Assertions.assertEquals(ocl.getCloudServiceProvider().getName(),
                serviceTemplateDetailVo.getCsp());
        Assertions.assertEquals(ocl.getName().toLowerCase(Locale.ROOT),
                serviceTemplateDetailVo.getName());
        Assertions.assertEquals(ocl.getServiceVersion(), serviceTemplateDetailVo.getVersion());
    }

    void testListAllServiceTemplates() throws Exception {
        List<ServiceTemplateDetailVo> serviceTemplateDetailVos = List.of(serviceTemplateDetailVo);

        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(
                        get("/xpanse/service_templates/all").param("categoryName", "middleware")
                                .param("cspName", "huawei").param("serviceName", "kafka-cluster")
                                .param("serviceVersion", "v3.3.2").accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        List<ServiceTemplateDetailVo> result =
                Arrays.stream(objectMapper.readValue(response.getContentAsString(),
                        ServiceTemplateDetailVo[].class)).toList();

        // Verify the results
        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertThat(
                serviceTemplateDetailVos).usingRecursiveFieldByFieldElementComparatorIgnoringFields(
                "lastModifiedTime").isEqualTo(
                result);

    }

    void testListAllServiceTemplatesThrowsException() throws Exception {
        // Setup
        String errorMessage = "Failed to convert value of type 'java.lang.String' to required type";
        Response expectedResponse =
                Response.errorResponse(ResultType.UNPROCESSABLE_ENTITY, List.of(errorMessage));

        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(
                        get("/xpanse/service_templates/all").param("categoryName", Category.AI.toValue())
                                .param("cspName", "errorCspName").param("serviceName", "kafka-cluster")
                                .param("serviceVersion", "v3.3.2").accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        Response resultResponse =
                objectMapper.readValue(response.getContentAsString(), Response.class);

        // Verify the results
        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), response.getStatus());
        Assertions.assertFalse(resultResponse.getSuccess());
        Assertions.assertEquals(expectedResponse.getSuccess(), resultResponse.getSuccess());
        Assertions.assertEquals(expectedResponse.getResultType(), resultResponse.getResultType());
    }

    void testUnregister() throws Exception {
        // Setup
        Response expectedResponse = Response.successResponse(Collections.singletonList(
                String.format("Unregister service template using id %s successful.", id)));
        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(
                        delete("/xpanse/service_templates/{id}", id).accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // Verify the results
        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());
        Assertions.assertEquals(response.getContentAsString(),
                objectMapper.writeValueAsString(expectedResponse));
    }

    void testListAllServiceTemplatesReturnsNoItems() throws Exception {
        // Setup
        String result = "[]";

        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(
                        get("/xpanse/service_templates/all").param("categoryName", "middleware")
                                .param("cspName", "aws").param("serviceName", "kafka-cluster")
                                .param("serviceVersion", "v3.3.2").accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // Verify the results
        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());
        Assertions.assertEquals(result, response.getContentAsString());
    }
}
