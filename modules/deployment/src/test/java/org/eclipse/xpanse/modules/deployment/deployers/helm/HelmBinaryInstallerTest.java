/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.helm;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import com.github.tomakehurst.wiremock.common.ClasspathFileSource;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.extension.responsetemplating.TemplateEngine;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.util.Collections;
import org.eclipse.xpanse.common.proxy.ProxyConfigurationManager;
import org.eclipse.xpanse.modules.deployment.config.DeploymentProperties;
import org.eclipse.xpanse.modules.deployment.deployers.deployertools.DeployerTarGzFileManage;
import org.eclipse.xpanse.modules.deployment.deployers.deployertools.DeployerToolUtils;
import org.eclipse.xpanse.modules.deployment.deployers.deployertools.DeployerToolVersionsFetcher;
import org.eclipse.xpanse.modules.deployment.deployers.deployertools.cache.DeployerToolVersionsCache;
import org.eclipse.xpanse.modules.deployment.deployers.deployertools.cache.DeployerToolVersionsCacheManager;
import org.eclipse.xpanse.modules.deployment.deployers.helm.installer.HelmBinaryInstaller;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = {
            DeployerToolUtils.class,
            HelmBinaryInstaller.class,
            DeployerTarGzFileManage.class,
            DeployerToolVersionsFetcher.class,
            DeployerToolVersionsCacheManager.class,
            DeployerToolVersionsCache.class,
            ProxyConfigurationManager.class,
            DeploymentProperties.class
        })
@TestPropertySource(
        properties = {
            "xpanse.deployer.helm.install-dir=/tmp/helm",
            "xpanse.deployer.support-only-default-versions-enabled=true",
            "xpanse.deployer.helm.default-supported-versions=3.17.3,3.17.2",
            "xpanse.deployer.helm.github.repository=helm/helm",
            "xpanse.deployer.helm.download-base-url=https://get.helm.sh/"
        })
@Import(RefreshAutoConfiguration.class)
public class HelmBinaryInstallerTest {

    @RegisterExtension
    static WireMockExtension wireMockExtension =
            WireMockExtension.newInstance()
                    .options(
                            wireMockConfig()
                                    .dynamicPort()
                                    .extensions(
                                            new ResponseTemplateTransformer(
                                                    TemplateEngine.defaultTemplateEngine(),
                                                    false,
                                                    new ClasspathFileSource(
                                                            "src/test/resources/mappings"),
                                                    Collections.emptyList())))
                    .build();

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add(
                "xpanse.deployer.helm.github.apiEndpoint",
                wireMockExtension.getRuntimeInfo()::getHttpBaseUrl);
    }

    @Autowired private HelmBinaryInstaller helmBinaryInstaller;

    @Test
    public void installHelmBinary() {
        helmBinaryInstaller.getExecutorPathThatMatchesRequiredVersion("=v3.17.0");
    }
}
