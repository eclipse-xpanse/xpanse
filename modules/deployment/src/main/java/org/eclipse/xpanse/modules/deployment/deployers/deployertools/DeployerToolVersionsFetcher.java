/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.deployertools;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.common.proxy.ProxyConfigurationManager;
import org.eclipse.xpanse.modules.deployment.config.DeploymentProperties;
import org.eclipse.xpanse.modules.models.common.exceptions.ClientApiCallFailedException;
import org.eclipse.xpanse.modules.models.common.exceptions.RateLimiterException;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTag;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.GitHubRateLimitHandler;
import org.kohsuke.github.PagedIterable;
import org.kohsuke.github.connector.GitHubConnectorResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpMethod;
import org.springframework.retry.RetryContext;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.stereotype.Component;

/** Beans for fetching all available versions of the deployer tool. */
@Slf4j
@RefreshScope
@Component
public class DeployerToolVersionsFetcher {

    private static final Pattern OFFICIAL_VERSION_PATTERN =
            Pattern.compile("^v(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})$");

    private final DeploymentProperties deploymentProperties;

    private final ProxyConfigurationManager proxyConfigurationManager;

    /** Constructor method. */
    @Autowired
    public DeployerToolVersionsFetcher(
            DeploymentProperties deploymentProperties,
            ProxyConfigurationManager proxyConfigurationManager) {
        this.deploymentProperties = deploymentProperties;
        this.proxyConfigurationManager = proxyConfigurationManager;
    }

    /**
     * Fetch all available versions from the website of deployer.
     *
     * @return all available versions.
     */
    @Retryable(
            retryFor = ClientApiCallFailedException.class,
            maxAttemptsExpression = "${xpanse.http-client-request.retry-max-attempts}",
            backoff =
                    @Backoff(delayExpression = "${xpanse.http-client-request.delay-milliseconds}"))
    public Set<String> fetchOfficialVersionsOfDeployerTool(DeployerKind deployerKind) {
        RetryContext retryContext = RetrySynchronizationManager.getContext();
        int retryCount = Objects.isNull(retryContext) ? 0 : retryContext.getRetryCount();
        log.info(
                "Start to fetch available versions from website for deployer tool {}."
                        + " Retry count: {}",
                deployerKind.toValue(),
                retryCount);
        Set<String> allVersions = new HashSet<>();
        try {
            String apiEndpoint = getGithubApiEndpoint(deployerKind);
            if (!isEndpointReachable(apiEndpoint)) {
                throw new ClientApiCallFailedException(
                        String.format("Endpoint %s is not reachable.", apiEndpoint));
            }
            String githubRepository = getGithubRepository(deployerKind);
            GitHubBuilder gitHubBuilder =
                    new GitHubBuilder()
                            .withEndpoint(apiEndpoint)
                            .withRateLimitHandler(getGithubRateLimitHandler());
            if (proxyConfigurationManager.getHttpsProxy() != null) {
                log.info("using proxy to connect to github");
                gitHubBuilder.withProxy(proxyConfigurationManager.getHttpsProxy());
            }
            GitHub gitHub = gitHubBuilder.build();
            GHRepository repository = gitHub.getRepository(githubRepository);
            PagedIterable<GHTag> tags = repository.listTags();
            tags.forEach(
                    tag -> {
                        String version = tag.getName();
                        if (OFFICIAL_VERSION_PATTERN.matcher(version).matches()) {
                            // remove the prefix 'v'
                            allVersions.add(version.substring(1));
                        }
                    });
        } catch (RateLimiterException e) {
            log.error(e.getMessage());
            throw e;
        } catch (Exception e) {
            String errorMsg =
                    String.format(
                            "Failed to fetch available versions from website for "
                                    + "deployer tool %s.",
                            deployerKind.toValue());
            log.error("{} Retry count: {}", errorMsg, retryCount, e);
            throw new ClientApiCallFailedException(errorMsg);
        }
        log.info(
                "Get available versions {} from website for deployer tool {}." + " Retry count: {}",
                allVersions,
                deployerKind.toValue(),
                retryCount);
        if (allVersions.isEmpty()) {
            String errorMsg =
                    String.format(
                            "No available versions found from website for " + "deployer tool %s",
                            deployerKind.toValue());
            throw new ClientApiCallFailedException(errorMsg);
        }
        return allVersions;
    }

    private String getGithubApiEndpoint(DeployerKind deployerKind) {
        return switch (deployerKind) {
            case TERRAFORM -> deploymentProperties.getTerraformLocal().getGithub().getApiEndpoint();
            case OPEN_TOFU -> deploymentProperties.getOpentofuLocal().getGithub().getApiEndpoint();
            case HELM -> deploymentProperties.getHelm().getGithub().getApiEndpoint();
        };
    }

    private boolean isEndpointReachable(String endpoint) {
        try {
            URL url = URI.create(endpoint).toURL();
            HttpURLConnection connection =
                    Objects.nonNull(proxyConfigurationManager.getHttpsProxy())
                            ? (HttpURLConnection)
                                    url.openConnection(proxyConfigurationManager.getHttpsProxy())
                            : (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(10000);
            connection.setRequestMethod(HttpMethod.HEAD.name());
            if (isNoRetriesAllowed(connection.getResponseCode(), connection.getResponseMessage())) {
                throw new RateLimiterException(connection.getResponseMessage());
            }
            return true;
        } catch (RateLimiterException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new ClientApiCallFailedException(
                    "Failed to connect to the endpoint: " + endpoint);
        }
    }

    private String getGithubRepository(DeployerKind deployerKind) {
        return switch (deployerKind) {
            case TERRAFORM -> deploymentProperties.getTerraformLocal().getGithub().getRepository();
            case OPEN_TOFU -> deploymentProperties.getOpentofuLocal().getGithub().getRepository();
            case HELM -> deploymentProperties.getHelm().getGithub().getRepository();
        };
    }

    /** Returns the default supported versions a deployer tool. */
    public List<String> getDefaultVersionsByDeployerKind(DeployerKind deployerKind) {
        return switch (deployerKind) {
            case TERRAFORM ->
                    deploymentProperties.getTerraformLocal().getDefaultSupportedVersions();
            case OPEN_TOFU -> deploymentProperties.getOpentofuLocal().getDefaultSupportedVersions();
            case HELM -> deploymentProperties.getHelm().getDefaultSupportedVersions();
        };
    }

    private GitHubRateLimitHandler getGithubRateLimitHandler() {
        return new GitHubRateLimitHandler() {
            @Override
            public void onError(@Nonnull GitHubConnectorResponse response) {
                String limit = response.header("X-RateLimit-Limit");
                String remaining = response.header("X-RateLimit-Remaining");
                String reset = response.header("X-RateLimit-Reset");
                String errorMsg =
                        String.format(
                                "GitHub API rate limit exceeded. "
                                        + "Rate limit: %s, remaining: %s, reset time: %s",
                                limit, remaining, reset);
                throw new RateLimiterException(errorMsg);
            }
        };
    }

    private boolean isNoRetriesAllowed(int responseCode, String responseMessage) {
        if (responseCode == 429) {
            return true;
        } else {
            return responseMessage.toLowerCase().contains("rate limit exceeded")
                    && responseMessage.toLowerCase().contains("too many requests");
        }
    }
}
