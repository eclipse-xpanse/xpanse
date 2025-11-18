/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.deployertools.cache;

import static org.eclipse.xpanse.modules.cache.consts.CacheConstants.DEPLOYER_VERSIONS_CACHE_NAME;

import java.util.HashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.deployment.config.DeploymentProperties;
import org.eclipse.xpanse.modules.deployment.deployers.deployertools.DeployerToolVersionsFetcher;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/** Bean to update the cache of versions of OpenTofu. */
@Slf4j
@Component
public class DeployerToolVersionsCache {

    private final DeployerToolVersionsFetcher versionsFetcher;
    private final DeploymentProperties deploymentProperties;

    public DeployerToolVersionsCache(
            DeployerToolVersionsFetcher versionsFetcher,
            DeploymentProperties deploymentProperties) {
        this.versionsFetcher = versionsFetcher;
        this.deploymentProperties = deploymentProperties;
    }

    /**
     * Get the available versions of OpenTofu.
     *
     * @return Set of available versions.
     */
    @Cacheable(value = DEPLOYER_VERSIONS_CACHE_NAME, key = "#deployerKind")
    public Set<String> getVersionsCacheOfDeployerTool(DeployerKind deployerKind) {
        if (deploymentProperties.getSupportOnlyDefaultVersionsEnabled()) {
            return new HashSet<>(versionsFetcher.getDefaultVersionsByDeployerKind(deployerKind));
        }
        try {
            return versionsFetcher.fetchOfficialVersionsOfDeployerTool(deployerKind);
        } catch (Exception e) {
            log.error(
                    "Failed to fetch versions from website for deploy tool {}, get "
                            + "versions from default config.",
                    deployerKind.toValue(),
                    e);
            return new HashSet<>(versionsFetcher.getDefaultVersionsByDeployerKind(deployerKind));
        }
    }

    /**
     * Update the cache of versions of OpenTofu.
     *
     * @param versions List of available versions.
     */
    @CachePut(value = DEPLOYER_VERSIONS_CACHE_NAME, key = "#deployerKind")
    public void updateCachedVersionsOfDeployerTool(
            DeployerKind deployerKind, Set<String> versions) {
        log.info(
                "Updated versions cache of deployer:{} with versions:{}.",
                deployerKind.toValue(),
                versions);
    }
}
