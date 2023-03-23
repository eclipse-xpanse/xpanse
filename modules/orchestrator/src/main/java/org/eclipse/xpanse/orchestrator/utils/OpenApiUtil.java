/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.database.register.RegisterServiceEntity;
import org.eclipse.xpanse.modules.models.resource.DeployVariable;
import org.eclipse.xpanse.modules.models.utils.DeployVariableValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * OpenApiUtil.
 */
@Slf4j
@Component
public class OpenApiUtil {

    private final DeployVariableValidator deployVariableValidator;

    @Autowired
    public OpenApiUtil(DeployVariableValidator deployVariableValidator) {
        this.deployVariableValidator = deployVariableValidator;
    }

    /**
     * update OpenApi for registered service .
     *
     * @param registerService Registered services.
     */
    public void updateServiceApi(RegisterServiceEntity registerService) {
        String rootPath = System.getProperty("user.dir");
        File folder = new File(rootPath + "/openapi");
        File file = new File(folder, registerService.getId() + ".html");
        if (file.exists()) {
            file.delete();
        }
        creatServiceApi(registerService);
    }

    /**
     * create OpenApi for registered service .
     *
     * @param registerService Registered services.
     */
    public String creatServiceApi(RegisterServiceEntity registerService) {
        List<DeployVariable> deployVariables = registerService.getOcl().getDeployment()
                .getContext();
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String propertiesStr = null;
        String requiredStr = null;
        try {
            propertiesStr =
                    mapper.writeValueAsString(
                            deployVariableValidator.getVariableApiInfoMap(deployVariables)).replace(
                            "\"[", "[").replace("]\"", "]").replace("\\", "").replace("'", "");
            requiredStr = mapper.writeValueAsString(
                    deployVariableValidator.getRequiredKeySet(deployVariables));
        } catch (JsonProcessingException e) {
            log.error("Failed to write value as string.", e);
        }
        String apiJson = String.format("{\n"
                + "    \"openapi\": \"3.0.1\",\n"
                + "    \"info\": {\n"
                + "      \"title\": \"OpenAPI definition\",\n"
                + "      \"version\": \"v0\"\n"
                + "    },\n"
                + "    \"servers\": [\n"
                + "      {\n"
                + "        \"url\": \"http://localhost:8080\",\n"
                + "        \"description\": \"Generated server url\"\n"
                + "      }\n"
                + "    ],\n"
                + "    \"paths\": {\n"
                + "      \"/xpanse/service\": {\n"
                + "        \"post\": {\n"
                + "          \"tags\": [\n"
                + "            \"Service\"\n"
                + "          ],\n"
                + "          \"operationId\": \"start\",\n"
                + "          \"requestBody\": {\n"
                + "            \"content\": {\n"
                + "              \"application/json\": {\n"
                + "                \"schema\": {\n"
                + "                  \"$ref\": \"#/components/schemas/CreateRequest\"\n"
                + "                }\n"
                + "              }\n"
                + "            },\n"
                + "            \"required\": true\n"
                + "          },\n"
                + "          \"responses\": {\n"
                + "            \"202\": {\n"
                + "              \"description\": \"Accepted\",\n"
                + "              \"content\": {\n"
                + "                \"*/*\": {\n"
                + "                  \"schema\": {\n"
                + "                    \"type\": \"string\",\n"
                + "                    \"format\": \"uuid\"\n"
                + "                  }\n"
                + "                }\n"
                + "              }\n"
                + "            },\n"
                + "            \"400\": {\n"
                + "              \"description\": \"Bad Request\",\n"
                + "              \"content\": {\n"
                + "                \"*/*\": {\n"
                + "                  \"schema\": {\n"
                + "                    \"$ref\": \"#/components/schemas/Response\"\n"
                + "                  }\n"
                + "                }\n"
                + "              }\n"
                + "            },\n"
                + "            \"404\": {\n"
                + "              \"description\": \"Not Found\",\n"
                + "              \"content\": {\n"
                + "                \"*/*\": {\n"
                + "                  \"schema\": {\n"
                + "                    \"$ref\": \"#/components/schemas/Response\"\n"
                + "                  }\n"
                + "                }\n"
                + "              }\n"
                + "            },\n"
                + "            \"500\": {\n"
                + "              \"description\": \"Internal Server Error\",\n"
                + "              \"content\": {\n"
                + "                \"*/*\": {\n"
                + "                  \"schema\": {\n"
                + "                    \"$ref\": \"#/components/schemas/Response\"\n"
                + "                  }\n"
                + "                }\n"
                + "              }\n"
                + "            }\n"
                + "          }\n"
                + "        }\n"
                + "      }\n"
                + "    },\n"
                + "    \"components\": {\n"
                + "      \"schemas\": {\n"
                + "        \"Response\": {\n"
                + "          \"required\": [\n"
                + "            \"code\",\n"
                + "            \"message\",\n"
                + "            \"success\"\n"
                + "          ],\n"
                + "          \"type\": \"object\",\n"
                + "          \"properties\": {\n"
                + "            \"code\": {\n"
                + "              \"type\": \"string\",\n"
                + "              \"description\": \"The result code of response.\"\n"
                + "            },\n"
                + "            \"message\": {\n"
                + "              \"type\": \"string\",\n"
                + "              \"description\": \"The result message of response.\"\n"
                + "            },\n"
                + "            \"success\": {\n"
                + "              \"type\": \"boolean\",\n"
                + "              \"description\": \"The success boolean of response.\"\n"
                + "            }\n"
                + "          }\n"
                + "        },\n"
                + "        \"property\": {\n"
                + "          \"required\": %s,\n"
                + "          \"type\": \"object\",\n"
                + "          \"properties\": %s\n"
                + "        },\n"
                + "        \"CreateRequest\": {\n"
                + "          \"required\": [\n"
                + "            \"category\",\n"
                + "            \"csp\",\n"
                + "            \"flavor\",\n"
                + "            \"name\",\n"
                + "            \"region\",\n"
                + "            \"version\"\n"
                + "          ],\n"
                + "          \"type\": \"object\",\n"
                + "          \"properties\": {\n"
                + "            \"category\": {\n"
                + "              \"type\": \"string\",\n"
                + "              \"description\": \"The category of the service\",\n"
                + "              \"enum\": [\n"
                + "                \"ai\",\n"
                + "                \"compute\",\n"
                + "                \"container\",\n"
                + "                \"storage\",\n"
                + "                \"network\",\n"
                + "                \"database\",\n"
                + "                \"media_service\",\n"
                + "                \"security\",\n"
                + "                \"middleware\",\n"
                + "                \"others\"\n"
                + "              ]\n"
                + "            },\n"
                + "            \"name\": {\n"
                + "              \"type\": \"string\",\n"
                + "              \"description\": \"The name of the service\"\n"
                + "            },\n"
                + "            \"version\": {\n"
                + "              \"type\": \"string\",\n"
                + "              \"description\": \"The version of service\"\n"
                + "            },\n"
                + "            \"region\": {\n"
                + "              \"type\": \"string\",\n"
                + "              \"description\": \"The region of the provider.\"\n"
                + "            },\n"
                + "            \"csp\": {\n"
                + "              \"type\": \"string\",\n"
                + "              \"description\": \"The csp of the Service.\",\n"
                + "              \"enum\": [\n"
                + "                \"aws\",\n"
                + "                \"azure\",\n"
                + "                \"alibaba\",\n"
                + "                \"huawei\",\n"
                + "                \"openstack\"\n"
                + "              ]\n"
                + "            },\n"
                + "            \"flavor\": {\n"
                + "              \"type\": \"string\",\n"
                + "              \"description\": \"The flavor of the Service.\"\n"
                + "            },\n"
                + "            \"property\": {\n"
                + "              \"$ref\": \"#/components/schemas/property\"\n"
                + "            }\n"
                + "          }\n"
                + "        }\n"
                + "      }\n"
                + "    }\n"
                + "  }", requiredStr, propertiesStr);
        String yamlFileName = String.format("%s" + ".yaml", registerService.getId());
        File openapi = new File("openapi");
        openapi.mkdir();
        try {
            try (FileWriter apiWriter =
                    new FileWriter("./openapi" + File.separator + yamlFileName)) {
                apiWriter.write(apiJson);
            }
            String comm = "java -jar ./lib/openapi-generator-cli.jar generate -i "
                    + "./openapi/" + yamlFileName + " -g "
                    + "html2 -o ./openapi";
            Process exec = Runtime.getRuntime()
                    .exec(comm);
            exec.waitFor();
            // Modify the file name to serviceId.html
            File oldFile = new File("./openapi/index.html");
            File newFile = new File("./openapi/" + registerService.getId() + ".html");
            oldFile.renameTo(newFile);

            File yamlFile = new File(
                    "./openapi" + File.separator + yamlFileName);
            yamlFile.delete();
            log.info("serviceApi create success.");
            return "http://localhost:8080/openapi/" + registerService.getId() + ".html";
        } catch (IOException | InterruptedException ex) {
            log.error("serviceApi create failed.", ex);
            throw new RuntimeException("serviceApi create failed.", ex);
        }
    }

    /**
     * delete OpenApi for registered service using the ID.
     *
     * @param id ID of registered service.
     */
    public void deleteServiceApi(String id) {
        File file = new File("openapi/" + id + ".html");
        file.delete();
    }

}
