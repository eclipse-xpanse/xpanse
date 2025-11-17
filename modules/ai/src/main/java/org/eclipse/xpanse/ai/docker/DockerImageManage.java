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
import org.eclipse.xpanse.ai.config.AiProperties;
import org.eclipse.xpanse.modules.models.common.exceptions.XpanseUnhandledException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** Bean to manage docker image build and push. */
@Slf4j
@Component
@Profile("ai")
public class DockerImageManage {

    private static final String DEFAULT_VERSION = "latest";
    private final AiProperties aiProperties;

    /** Constructor method. */
    @Autowired
    public DockerImageManage(AiProperties aiProperties) {
        this.aiProperties = aiProperties;
    }

    /** creates docker image and pushes to a registry. */
    public String createAndPushImage(String codePath, String imageName)
            throws IOException, InterruptedException {
        String fullImageName =
                aiProperties.getDocker().getRegistryUrl()
                        + '/'
                        + (aiProperties.getDocker().getRegistryOrganization().isBlank()
                                ? aiProperties.getDocker().getRegistryUsername()
                                : aiProperties.getDocker().getRegistryOrganization())
                        + '/'
                        + imageName
                        + ':'
                        + DEFAULT_VERSION;

        DockerClientConfig config =
                DefaultDockerClientConfig.createDefaultConfigBuilder()
                        .withRegistryUrl(aiProperties.getDocker().getRegistryUrl())
                        .withRegistryUsername(aiProperties.getDocker().getRegistryUsername())
                        .withRegistryPassword(aiProperties.getDocker().getRegistryPassword())
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

                if (!aiProperties.getDocker().getProxyUrl().isBlank()) {
                    log.debug(
                            "using proxy URL {} for docker image build.",
                            aiProperties.getDocker().getProxyUrl());
                    buildImageCmd.withBuildArg(
                            "HTTPS_PROXY", aiProperties.getDocker().getProxyUrl());
                }
                buildImageCmd.exec(resultCallback);
                resultCallback.awaitCompletion();
                log.info("Image built successfully.");

                log.info(
                        "Pushing image: {} to {}",
                        fullImageName,
                        aiProperties.getDocker().getRegistryUrl());
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
