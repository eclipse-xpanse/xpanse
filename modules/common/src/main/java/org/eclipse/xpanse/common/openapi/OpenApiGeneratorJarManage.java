/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.common.openapi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * Class to manage all methods related to Openapi generator jar.
 */
@Getter
@Component
@Slf4j
public class OpenApiGeneratorJarManage implements
        ApplicationListener<ContextRefreshedEvent> {

    private static final String OPENAPI_GENERATOR_FILE_NAME = "openapi-generator-cli.jar";

    private final String clientDownLoadUrl;

    private final String openapiPath;

    /**
     * Constructor for instantiating OpenApiGeneratorJarManage bean.
     *
     * @param clientDownLoadUrl URL for downloading openapi-generator jar from maven central.
     * @param openapiPath       folder where the jar must be available.
     */
    @Autowired
    public OpenApiGeneratorJarManage(
            @Value("${openapi.generator.client.download-url}") String clientDownLoadUrl,
            @Value("${openapi.path:openapi/}") String openapiPath) {
        this.clientDownLoadUrl = clientDownLoadUrl;
        this.openapiPath = openapiPath;
    }

    /**
     * OpenApiGeneratorJarManage
     * Get the openapi-generator-cli.jar from Resources.
     * The download part is only for local development and is not required when building a "jar"
     * or using the Docker image of the application.
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        File existingJarFile = getCliFile();
        if (existingJarFile.exists()) {
            log.info(OPENAPI_GENERATOR_FILE_NAME + " already exists.");
            return;
        }
        ClassPathResource resource = new ClassPathResource(OPENAPI_GENERATOR_FILE_NAME);
        if (resource.exists()) {
            log.info(OPENAPI_GENERATOR_FILE_NAME + " found in resources. Copying it.");
            try {
                FileUtils.copyURLToFile(resource.getURL(), getCliFile());
            } catch (IOException e) {
                log.error("creating cli jar file from resources failed", e);
            }
        } else {
            log.info(OPENAPI_GENERATOR_FILE_NAME + " not found. Downloading it.");
            this.downloadClientJar();
        }
    }

    public File getCliFile() {
        return new File(getOpenApiWorkdir(), OPENAPI_GENERATOR_FILE_NAME);
    }

    /**
     * Download openapi-generator-cli.jar from the maven repository or the URL specified by the
     * `@openapi.generator.client.download-url@` into the work directory.
     *
     * @return returns if the download is successful.
     */
    public boolean downloadClientJar() {
        try {
            log.info("Downloading openapi generator client jar from URL {}", clientDownLoadUrl);
            URL url = URI.create(clientDownLoadUrl).toURL();
            File cliJarFile = getCliFile();
            FileUtils.copyURLToFile(url, cliJarFile);
            log.info("Downloading openapi generator client into path {} from URL{} successful.",
                    cliJarFile.getPath(), clientDownLoadUrl);
            return true;
        } catch (IOException ioException) {
            log.error("Downloading openapi generator client jar from URL failed.", ioException);
            return false;
        }
    }

    /**
     * Method to determine the directory location where openapi files must be created.
     */
    public String getOpenApiWorkdir() {
        String rootPath = System.getProperty("user.dir");
        try {
            int modulesIndex = rootPath.indexOf("modules");
            if (modulesIndex > 1) {
                rootPath = rootPath.substring(0, modulesIndex);
            }
            int runtimeIndex = rootPath.indexOf("runtime");
            if (runtimeIndex > 1) {
                rootPath = rootPath.substring(0, runtimeIndex);
            }
            File openApiDir = new File(rootPath, openapiPath);
            if (!openApiDir.exists() && !openApiDir.mkdirs()) {
                throw new FileNotFoundException("Create open API workspace failed!");
            }
            return openApiDir.getPath();

        } catch (IOException e) {
            log.error("Create open API workdir failed!", e);
        }
        return rootPath;

    }
}
