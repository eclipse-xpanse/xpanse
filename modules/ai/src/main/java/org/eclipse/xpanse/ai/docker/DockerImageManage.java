/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.ai.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.BuildImageCmd;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.api.model.BuildResponseItem;
import com.github.dockerjava.api.model.ResponseItem;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.common.exceptions.XpanseUnhandledException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** Bean to manage docker image build and push. */
@Slf4j
@Component
@Profile("ai")
public class DockerImageManage {

    private static final String DEFAULT_VERSION = "latest";
    private final String dockerRegistryUrl;
    private final String dockerRegistryUsername;
    private final String dockerRegistryPassword;
    private final String dockerRegistryOrganization;
    private final String dockerImageBuildProxyUrl;

    /** Constructor method. */
    @Autowired
    public DockerImageManage(
            @Value("${ai.docker.registry.url}") String dockerRegistryUrl,
            @Value("${ai.docker.registry.username}") String dockerRegistryUsername,
            @Value("${ai.docker.registry.password}") String dockerRegistryPassword,
            @Value("${ai.docker.registry.organization}") String dockerRegistryOrganization,
            @Value("${docker.image.build.proxy.url}") String dockerImageBuildProxyUrl) {
        this.dockerRegistryUrl = dockerRegistryUrl;
        this.dockerRegistryUsername = dockerRegistryUsername;
        this.dockerRegistryPassword = dockerRegistryPassword;
        this.dockerRegistryOrganization = dockerRegistryOrganization;
        this.dockerImageBuildProxyUrl = dockerImageBuildProxyUrl;
    }

    /** creates docker image and pushes to a registry. */
    public String createAndPushImage(String codePath, String imageName)
            throws IOException, InterruptedException {
        String fullImageName =
                dockerRegistryUrl
                        + '/'
                        + (dockerRegistryOrganization.isBlank()
                                ? dockerRegistryUsername
                                : dockerRegistryOrganization)
                        + '/'
                        + imageName
                        + ':'
                        + DEFAULT_VERSION;

        DockerClientConfig config =
                DefaultDockerClientConfig.createDefaultConfigBuilder()
                        .withRegistryUrl(dockerRegistryUrl)
                        .withRegistryUsername(dockerRegistryUsername)
                        .withRegistryPassword(dockerRegistryPassword)
                        .withDockerTlsVerify(false)
                        .withDockerHost("unix:///var/run/docker.sock")
                        .build();

        DockerHttpClient httpClient =
                new ApacheDockerHttpClient.Builder()
                        .dockerHost(config.getDockerHost())
                        .maxConnections(100)
                        .connectionTimeout(Duration.ofMinutes(10))
                        .responseTimeout(Duration.ofMinutes(10))
                        .build();

        try {
            try (DockerClient dockerClient =
                    DockerClientBuilder.getInstance(config)
                            .withDockerHttpClient(httpClient)
                            .build()) {
                log.info("Building image: {}", fullImageName);

                BuildImageResultCallback resultCallback =
                        new BuildImageResultCallback() {
                            @Override
                            public void onNext(BuildResponseItem item) {
                                log.info(item.getStream());
                                ResponseItem.ErrorDetail errorDetail = item.getErrorDetail();
                                if (item.isErrorIndicated() && errorDetail != null) {
                                    throw new XpanseUnhandledException(errorDetail.getMessage());
                                }
                            }
                        };
                BuildImageCmd buildImageCmd =
                        dockerClient
                                .buildImageCmd(new File(codePath))
                                .withTags(Collections.singleton(fullImageName))
                                .withNoCache(true)
                                .withTags(Collections.singleton(fullImageName));

                if (!dockerImageBuildProxyUrl.isBlank()) {
                    log.debug(
                            "using proxy URL {} for docker image build.", dockerImageBuildProxyUrl);
                    buildImageCmd.withBuildArg("HTTPS_PROXY", dockerImageBuildProxyUrl);
                }
                buildImageCmd.exec(resultCallback);
                resultCallback.awaitCompletion();
                log.info("Image built successfully.");

                log.info("Pushing image: {} to {}", fullImageName, dockerRegistryUrl);
                dockerClient.pushImageCmd(fullImageName).start().awaitCompletion();
            }
            log.info("Image pushed successfully to GitHub Packages.");

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
        return fullImageName;
    }
}
