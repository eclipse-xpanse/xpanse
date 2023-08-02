/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.common.openapi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Objects;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.common.exceptions.OpenApiFileGenerationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Bean to provide helper methods for generating OpenAPI files.
 */
@Slf4j
@Component
public class OpenApiUtil {

    @Getter
    private final String clientDownLoadUrl;
    @Getter
    private final String openapiPath;
    private final Integer port;

    /**
     * OpenApiUtil constructor.
     */
    @Autowired
    public OpenApiUtil(@Value("${openapi.download-generator-client-url:https://repo1.maven.org/"
            + "maven2/org/openapitools/openapi-generator-cli/6.6.0/"
            + "openapi-generator-cli.6.6.0.jar}")
                       String clientDownLoadUrl,
                       @Value("${openapi.path:openapi/}") String openapiPath,
                       @Value("${server.port:8080}") Integer port) {
        this.clientDownLoadUrl = clientDownLoadUrl;
        this.openapiPath = openapiPath;
        this.port = port;
    }

    /**
     * Get API url from headers. This ensures the correct URL returned to client even when the
     * request is routed via a load balancer.
     *
     * @return serviceUrl to access openapi URL.
     */
    public String getServiceUrl() {
        try {
            return ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        } catch (Exception e) {
            String host = "localhost";
            try {
                InetAddress address = InetAddress.getLocalHost();
                host = address.getHostAddress();
            } catch (UnknownHostException ex) {
                log.error("Get localHost error.", ex);
            }
            return "http://" + host + ":" + port;
        }

    }

    /**
     * Get openApi Url.
     *
     * @return openApiUrl
     */
    public String getOpenApiUrl(String id) {
        if (openapiPath.endsWith("/")) {
            return getServiceUrl() + "/" + openapiPath + id + ".html";
        }
        return getServiceUrl() + "/" + openapiPath + "/" + id + ".html";
    }

    /**
     * Download openapi-generator-cli.jar from the maven repository or the URL specified by the
     * `@openapi.download-generator-client-url` into the work directory.
     *
     * @param workdir The work directory of the openApi.
     */
    public boolean downloadClientJar(String workdir) throws IOException {
        File workDir = new File(workdir);
        if (!workDir.exists() && !workDir.mkdirs()) {
            throw new OpenApiFileGenerationException("Download client jar failed.");
        }
        String execJarName = "openapi-generator-cli.jar";
        File execJarFile = new File(workdir, execJarName);
        if (!execJarFile.exists() && !execJarFile.canExecute()) {
            log.info("Download openapi client:{} from URL:{} start.",
                    execJarFile.getPath(), clientDownLoadUrl);
            String jarTempFile = execJarName + ".temp";
            File downloadTemp = new File(workDir, jarTempFile);
            FileOutputStream fos = null;
            boolean downloadEnd;
            try {
                URL url = new URL(clientDownLoadUrl);
                URLConnection con = url.openConnection();
                ReadableByteChannel rbc = Channels.newChannel(con.getInputStream());
                fos = new FileOutputStream(downloadTemp);
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                downloadEnd = true;
                log.info("Download openapi client:{} from URL:{} end.",
                        execJarFile.getPath(), clientDownLoadUrl);
            } finally {
                if (Objects.nonNull(fos)) {
                    fos.close();
                }
            }
            if (downloadEnd && downloadTemp.renameTo(execJarFile)) {
                log.info("Download openapi client:{} from URL:{} success.",
                        execJarFile.getPath(), clientDownLoadUrl);
                return true;
            }
        }
        return true;
    }

    /**
     * Get the work directory of the openApi.
     *
     * @return workdir  The work directory of the openApi.
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
