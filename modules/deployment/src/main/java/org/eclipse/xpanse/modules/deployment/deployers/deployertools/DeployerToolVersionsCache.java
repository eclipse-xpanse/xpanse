/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.deployertools;


import static org.eclipse.xpanse.modules.cache.consts.CacheConstants.DEPLOYER_VERSIONS_CACHE_NAME;

import jakarta.annotation.Resource;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/**
 * Bean to update the cache of versions of OpenTofu.
 */
@Slf4j
@Component
public class DeployerToolVersionsCache {

    @Resource
    private DeployerToolVersionsFetcher versionsFetcher;

    /**
     * Get the available versions of OpenTofu.
     *
     * @return Set of available versions.
     */
    @Cacheable(value = DEPLOYER_VERSIONS_CACHE_NAME, key = "#deployerKind")
    public Set<String> getVersionsCacheOfDeployerTool(DeployerKind deployerKind) {
        try {
            return versionsFetcher.fetchOfficialVersionsOfDeployerTool(deployerKind);
        } catch (Exception e) {
            return versionsFetcher.getVersionsFromDefaultConfigOfDeployerTool(deployerKind);
        }
    }

    /**
     * Update the cache of versions of OpenTofu.
     *
     * @param versions List of available versions.
     */
    @CachePut(value = DEPLOYER_VERSIONS_CACHE_NAME, key = "#deployerKind")
    public void updateCachedVersionsOfDeployerTool(DeployerKind deployerKind,
                                                   Set<String> versions) {
        log.info("Updated versions cache of deployer:{} with versions:{}.",
                deployerKind.toValue(), versions);
    }



}
