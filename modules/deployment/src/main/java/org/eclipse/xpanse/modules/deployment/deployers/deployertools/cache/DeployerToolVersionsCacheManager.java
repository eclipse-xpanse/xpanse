/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.deployertools.cache;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Resource;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.deployment.deployers.deployertools.DeployerToolVersionsFetcher;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/** Bean to update the cache of versions of Terraform. */
@Slf4j
@Component
public class DeployerToolVersionsCacheManager
        implements ApplicationListener<ApplicationStartedEvent> {
    @Resource private DeployerToolVersionsCache versionsCache;
    @Resource private DeployerToolVersionsFetcher versionsFetcher;

    /** Initialize the versions caches for all deployer tools. */
    @Override
    public void onApplicationEvent(@Nonnull ApplicationStartedEvent event) {
        initializeCache();
    }

    private void initializeCache() {
        Arrays.stream(DeployerKind.values())
                .forEach(
                        deployerKind -> {
                            log.info(
                                    "Initializing versions cache for deployer tool {}.",
                                    deployerKind.toValue());
                            Set<String> versions =
                                    versionsCache.getVersionsCacheOfDeployerTool(deployerKind);
                            log.info(
                                    "Initialized versions cache for deployer tool {} with versions"
                                            + " {}.",
                                    deployerKind.toValue(),
                                    versions);
                        });
    }

    /**
     * Update the versions cache for all deployer tools fetched from their website. This method is
     * scheduled run once a day.
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void fetchVersionsFromWebsiteAndLoadCache() {
        log.info("Scheduled to fetch versions from website and update the cache.");
        Arrays.stream(DeployerKind.values())
                .forEach(
                        deployerKind -> {
                            try {
                                log.info(
                                        "Fetching versions from website for deployer tool {}.",
                                        deployerKind.toValue());
                                fetchVersionsFromWebsiteAndLoadCacheForDeployerTool(deployerKind);
                            } catch (Exception e) {
                                log.error(
                                        "Failed to fetch versions from website for deployer tool"
                                                + " {}.",
                                        deployerKind.toValue(),
                                        e);
                            }
                        });
    }

    /**
     * Update the versions cache for all deployer tools fetched from their website when the cached
     * versions are empty or the cached versions are the same as the default version list. This
     * method is scheduled to run every one hour.
     */
    @Scheduled(cron = "0 1 * * * ?")
    public void fetchVersionsFromWebsiteAndLoadCacheIfCacheHasOnlyDefaultVersions() {
        Arrays.stream(DeployerKind.values())
                .forEach(
                        deployerKind -> {
                            try {
                                Set<String> cachedVersions =
                                        versionsCache.getVersionsCacheOfDeployerTool(deployerKind);
                                Set<String> defaultVersions =
                                        versionsFetcher.getVersionsFromDefaultConfigOfDeployerTool(
                                                deployerKind);
                                if (CollectionUtils.isEmpty(cachedVersions)
                                        || Objects.equals(cachedVersions, defaultVersions)) {
                                    fetchVersionsFromWebsiteAndLoadCacheForDeployerTool(
                                            deployerKind);
                                }
                            } catch (Exception e) {
                                log.error(
                                        "Failed to fetch versions from website for deployer tool"
                                                + " {}.",
                                        deployerKind.toValue(),
                                        e);
                            }
                        });
    }

    /**
     * Get the available versions cache of deployer tool. when the cached versions is empty, or the
     * cached versions is the same as the default versions, then fetch the versions from the website
     * of the deployer tool, and update the cache.
     *
     * @param deployerKind The kind of deployer tool.
     * @return The available versions cache of deployer tool.
     */
    public Set<String> getAvailableVersionsOfDeployerTool(DeployerKind deployerKind) {
        Set<String> cachedVersions = versionsCache.getVersionsCacheOfDeployerTool(deployerKind);
        Set<String> defaultVersions =
                versionsFetcher.getVersionsFromDefaultConfigOfDeployerTool(deployerKind);
        if (CollectionUtils.isEmpty(cachedVersions)
                || Objects.equals(cachedVersions, defaultVersions)) {
            try {
                return fetchVersionsFromWebsiteAndLoadCacheForDeployerTool(deployerKind);
            } catch (Exception e) {
                log.error(
                        "Failed to fetch versions from website for deployer tool {}.",
                        deployerKind.toValue(),
                        e);
                return defaultVersions;
            }
        }
        return cachedVersions;
    }

    private Set<String> fetchVersionsFromWebsiteAndLoadCacheForDeployerTool(
            DeployerKind deployerKind) {
        Set<String> availableVersionsFromWebsite =
                versionsFetcher.fetchOfficialVersionsOfDeployerTool(deployerKind);
        versionsCache.updateCachedVersionsOfDeployerTool(
                deployerKind, availableVersionsFromWebsite);
        log.info(
                "Successfully updated versions cache for deployer tool {} with versions " + "{}.",
                deployerKind.toValue(),
                availableVersionsFromWebsite);
        return availableVersionsFromWebsite;
    }
}
