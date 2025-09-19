/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.opentofu.opentofulocal;

import jakarta.annotation.Resource;
import java.io.File;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.deployment.deployers.deployertools.DeployerToolUtils;
import org.eclipse.xpanse.modules.models.common.exceptions.InvalidDeployerToolException;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

/** Defines methods for handling open tofu with required version. */
@Slf4j
@Component
public class OpenTofuInstaller {

    /** The pattern of the output of the command tofu -v. */
    public static final Pattern OPEN_TOFU_VERSION_OUTPUT_PATTERN =
            Pattern.compile("^OpenTofu\\s+v(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})\\b");

    private static final String OPEN_TOFU_BINARY_DOWNLOAD_URL_FORMAT =
            "%s/download/v%s/tofu_%s_%s_%s.zip";
    private static final String OPEN_TOFU_EXECUTOR_NAME_PREFIX = "tofu-";

    @Value("${deployer.opentofu.download.base.url:https://github.com/opentofu/opentofu/releases}")
    private String openTofuDownloadBaseUrl;

    @Value("${deployer.opentofu.install.dir:/opt/opentofu}")
    private String openTofuInstallDir;

    @Resource private DeployerToolUtils deployerToolUtils;

    /**
     * Find the executable binary path of the Terraform tool that matches the required version. If
     * no matching executable binary is found, install the Terraform tool with the required version
     * and then return the path.
     *
     * @param requiredVersion The required version of Terraform tool.
     * @return The path of the executable binary.
     */
    @Retryable(
            retryFor = InvalidDeployerToolException.class,
            maxAttemptsExpression = "${http.request.retry.max.attempts}",
            backoff = @Backoff(delayExpression = "${http.request.retry.delay.milliseconds}"))
    public String getExecutorPathThatMatchesRequiredVersion(String requiredVersion) {
        if (StringUtils.isBlank(requiredVersion)) {
            log.info("No required version of openTofu is specified, use the default openTofu.");
            return "tofu";
        }
        String[] operatorAndNumber =
                deployerToolUtils.getOperatorAndNumberFromRequiredVersion(requiredVersion);
        String requiredOperator = operatorAndNumber[0];
        String requiredNumber = operatorAndNumber[1];
        // Get path of the executor matched required version in the environment.
        String matchedVersionExecutorPath =
                deployerToolUtils.getExecutorPathMatchedRequiredVersion(
                        OPEN_TOFU_EXECUTOR_NAME_PREFIX,
                        OPEN_TOFU_VERSION_OUTPUT_PATTERN,
                        this.openTofuInstallDir,
                        requiredOperator,
                        requiredNumber);
        if (StringUtils.isBlank(matchedVersionExecutorPath)) {
            log.info(
                    "Not found any openTofu executor matched the required version {} from the "
                            + "openTofu installation dir {}, start to download and install one.",
                    requiredVersion,
                    this.openTofuInstallDir);
            return installOpenTofuByRequiredVersion(requiredOperator, requiredNumber);
        }
        return matchedVersionExecutorPath;
    }

    /**
     * Get exact version of OpenTofu by the executor path.
     *
     * @param executorPath executor path
     * @return version number of OpenTofu
     */
    public String getExactVersionOfOpenTofu(String executorPath) {
        return deployerToolUtils.getExactVersionOfExecutor(
                executorPath, OPEN_TOFU_VERSION_OUTPUT_PATTERN);
    }

    private String installOpenTofuByRequiredVersion(
            String requiredOperator, String requiredNumber) {
        String bestVersionNumber =
                deployerToolUtils.getBestAvailableVersionMatchingRequiredVersion(
                        DeployerKind.OPEN_TOFU, requiredOperator, requiredNumber);
        File installedExecutorFile =
                deployerToolUtils.installDeployerToolWithVersion(
                        OPEN_TOFU_EXECUTOR_NAME_PREFIX,
                        bestVersionNumber,
                        OPEN_TOFU_BINARY_DOWNLOAD_URL_FORMAT,
                        this.openTofuDownloadBaseUrl,
                        this.openTofuInstallDir);
        if (deployerToolUtils.checkIfExecutorCanBeExecuted(installedExecutorFile)) {
            log.info("OpenTofu with version {}  installed successfully.", installedExecutorFile);
            return installedExecutorFile.getAbsolutePath();
        }
        String errorMsg =
                String.format(
                        "Installing openTofu with version %s into the dir %s " + "failed. ",
                        bestVersionNumber, this.openTofuInstallDir);
        log.error(errorMsg);
        throw new InvalidDeployerToolException(errorMsg);
    }
}
