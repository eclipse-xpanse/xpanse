/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.deployertools;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.common.exceptions.ClientApiCallFailedException;
import org.eclipse.xpanse.modules.models.common.exceptions.InvalidDeployerToolException;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTag;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.GitHubRateLimitHandler;
import org.kohsuke.github.PagedIterable;
import org.kohsuke.github.connector.GitHubConnectorResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

/**
 * Beans for fetching all available versions of the deployer tool.
 */
@Slf4j
@Component
public class DeployerToolVersionsFetcher {

    private static final Pattern OFFICIAL_VERSION_PATTERN =
            Pattern.compile("^v(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})$");
    @Value("${deployer.terraform.github.api.endpoint:https//api.github.com}")
    private String terraformGithubApiEndpoint;
    @Value("${deployer.terraform.github.repository:hashicorp/terraform}")
    private String terraformGithubRepository;
    @Value("${deployer.terraform.versions}")
    private String terraformDefaultVersionsStr;

    @Value("${deployer.opentofu.github.api.endpoint:https//api.github.com}")
    private String openTofuGithubApiEndpoint;
    @Value("${deployer.opentofu.github.repository:opentofu/opentofu}")
    private String openTofuGithubRepository;
    @Value("${deployer.opentofu.versions}")
    private String openTofuDefaultVersionsStr;


    /**
     * Fetch all available versions from the website of deployer.
     *
     * @return all available versions.
     */
    @Retryable(retryFor = Exception.class,
            maxAttemptsExpression = "${http.request.retry.max.attempts}",
            backoff = @Backoff(delayExpression = "${http.request.retry.delay.milliseconds}"))
    public Set<String> fetchOfficialVersionsOfDeployerTool(DeployerKind deployerKind)
            throws Exception {
        Set<String> allVersions = new HashSet<>();
        String apiEndpoint = getGithubApiEndpoint(deployerKind);
        String githubRepository = getGithubRepository(deployerKind);
        GitHub gitHub = new GitHubBuilder()
                .withEndpoint(apiEndpoint)
                .withRateLimitHandler(getGithubRateLimitHandler())
                .build();
        GHRepository repository = gitHub.getRepository(githubRepository);
        PagedIterable<GHTag> tags = repository.listTags();
        tags.forEach(tag -> {
            String version = tag.getName();
            if (OFFICIAL_VERSION_PATTERN.matcher(version).matches()) {
                // remove the prefix 'v'
                allVersions.add(version.substring(1));
            }
        });
        log.info("Get available versions {} from website for deployer tool {}.", allVersions,
                deployerKind.toValue());
        if (allVersions.isEmpty()) {
            String errorMsg = String.format("No available versions found from website for deployer "
                    + "tool %s", deployerKind.toValue());
            throw new InvalidDeployerToolException(errorMsg);
        }
        return allVersions;
    }

    /**
     * Get default versions from config.
     *
     * @return default versions.
     */
    public Set<String> getVersionsFromDefaultConfigOfDeployerTool(DeployerKind deployerKind) {
        String defaultVersionsString = getDefaultVersionsString(deployerKind);
        Set<String> defaultVersions =
                Set.of(defaultVersionsString.replaceAll("//s+", "").split(","));
        log.info("Get versions {} from default config value {} for deployer tool {}",
                defaultVersions, defaultVersionsString, deployerKind.toValue());
        return defaultVersions;
    }


    private String getGithubApiEndpoint(DeployerKind deployerKind) {
        return switch (deployerKind) {
            case TERRAFORM -> terraformGithubApiEndpoint;
            case OPEN_TOFU -> openTofuGithubApiEndpoint;
        };
    }

    private String getGithubRepository(DeployerKind deployerKind) {
        return switch (deployerKind) {
            case TERRAFORM -> terraformGithubRepository;
            case OPEN_TOFU -> openTofuGithubRepository;
        };
    }

    private String getDefaultVersionsString(DeployerKind deployerKind) {
        return switch (deployerKind) {
            case TERRAFORM -> terraformDefaultVersionsStr;
            case OPEN_TOFU -> openTofuDefaultVersionsStr;
        };
    }

    private GitHubRateLimitHandler getGithubRateLimitHandler() {
        return new GitHubRateLimitHandler() {
            @Override
            public void onError(@Nonnull GitHubConnectorResponse response) {
                String limit = response.header("X-RateLimit-Limit");
                String remaining = response.header("X-RateLimit-Remaining");
                String reset = response.header("X-RateLimit-Reset");
                String errorMsg = String.format("GitHub API rate limit exceeded. "
                        + "Rate limit: %s, remaining: %s, reset time: %s", limit, remaining, reset);
                throw new ClientApiCallFailedException(errorMsg);
            }
        };
    }
}
