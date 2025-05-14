/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.ai.template;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.template.PebbleTemplate;
import jakarta.annotation.Resource;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.api.controllers.ServiceTemplateApi;
import org.eclipse.xpanse.modules.models.ai.enums.AiApplicationType;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.request.ServiceTemplateRequestInfo;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

/** Generates a service template and registers it. */
@Slf4j
@Component
@Profile("ai")
public class ServiceTemplateGenerator {

    @Resource private ServiceTemplateApi serviceTemplateApi;

    private static final String TEMPLATES_FOLDER = "templates";
    private final Map<String, Object> templateContext = new HashMap<>();

    /** Generates a service template and registers it to catalog. */
    public ServiceTemplateRequestInfo generateServiceTemplate(
            AiApplicationType aiApplicationType, String imageFullUrl) throws Exception {
        PebbleEngine engine =
                new PebbleEngine.Builder()
                        .strictVariables(true)
                        .newLineTrimming(false)
                        .autoEscaping(true)
                        .build();
        templateContext.put("image_name", String.format(imageFullUrl, aiApplicationType.toValue()));
        templateContext.put("name", aiApplicationType.toValue());

        String serviceTemplate =
                executeTemplate(
                        ResourceUtils.getFile(
                                        "classpath:"
                                                + String.format(
                                                        "%s/%s",
                                                        TEMPLATES_FOLDER,
                                                        "mcp/mcp-ecs-with-service-template.yml.peb"))
                                .getAbsolutePath(),
                        engine);
        log.info(serviceTemplate);
        return createOcl(serviceTemplate);
    }

    private String executeTemplate(String templateName, PebbleEngine pebbleEngine)
            throws IOException {
        PebbleTemplate template = pebbleEngine.getTemplate(templateName);
        Writer writer = new StringWriter();
        template.evaluate(writer, this.templateContext);
        String templateOutput = writer.toString();
        log.info("Template info:\n{}", templateOutput);
        return templateOutput;
    }

    private ServiceTemplateRequestInfo createOcl(String oclTemplate) throws Exception {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        Ocl ocl = mapper.readValue(oclTemplate, Ocl.class);
        try {
            return serviceTemplateApi.createServiceTemplate(ocl);
        } catch (Exception e) {

            throw e;
        }
    }
}
