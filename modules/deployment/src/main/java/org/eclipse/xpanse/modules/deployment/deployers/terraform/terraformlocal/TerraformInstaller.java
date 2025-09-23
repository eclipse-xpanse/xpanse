/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformlocal;

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

/** Defines methods for handling terraform with required version. */
@Slf4j
@Component
public class TerraformInstaller {

    /** The pattern of the output of the command terraform -v. */
    public static final Pattern TERRAFORM_VERSION_OUTPUT_PATTERN =
            Pattern.compile("^Terraform\\s+v(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})\\b");

    private static final String TERRAFORM_VERSION_COMMAND_ARGUMENT = " version";

    private static final String TERRAFORM_BINARY_DOWNLOAD_URL_FORMAT =
            "%s/%s/terraform_%s_%s_%s.zip";
    private static final String TERRAFORM_EXECUTOR_NAME_PREFIX = "terraform-";

    @Value("${deployer.terraform.download.base.url:https://releases.hashicorp.com/terraform}")
    private String terraformDownloadBaseUrl;

    @Value("${deployer.terraform.install.dir:/opt/terraform}")
    private String terraformInstallDir;

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
            log.info("No required version of terraform is specified, use the default terraform.");
            return "terraform";
        }
        String[] operatorAndNumber =
                deployerToolUtils.getOperatorAndNumberFromRequiredVersion(requiredVersion);
        String requiredOperator = operatorAndNumber[0];
        String requiredNumber = operatorAndNumber[1];
        // Get path of the executor matched required version in the environment.
        String matchedVersionExecutorPath =
                deployerToolUtils.getExecutorPathMatchedRequiredVersion(
                        TERRAFORM_EXECUTOR_NAME_PREFIX,
                        TERRAFORM_VERSION_COMMAND_ARGUMENT,
                        TERRAFORM_VERSION_OUTPUT_PATTERN,
                        this.terraformInstallDir,
                        requiredOperator,
                        requiredNumber);
        if (StringUtils.isBlank(matchedVersionExecutorPath)) {
            log.info(
                    "No terraform executor matched the required version {} from the "
                            + "terraform installation dir {}, start to download and install one.",
                    requiredVersion,
                    this.terraformInstallDir);
            return installTerraformByRequiredVersion(requiredOperator, requiredNumber);
        }
        return matchedVersionExecutorPath;
    }

    /**
     * Get exact version of terraform by executor path.
     *
     * @param executorPath executor path
     * @return version of terraform
     */
    public String getExactVersionOfTerraform(String executorPath) {
        return deployerToolUtils.getExactVersionOfExecutor(
                executorPath, TERRAFORM_VERSION_COMMAND_ARGUMENT, TERRAFORM_VERSION_OUTPUT_PATTERN);
    }

    private String installTerraformByRequiredVersion(
            String requiredOperator, String requiredNumber) {
        String bestVersionNumber =
                deployerToolUtils.getBestAvailableVersionMatchingRequiredVersion(
                        DeployerKind.TERRAFORM, requiredOperator, requiredNumber);
        File installedExecutorFile =
                deployerToolUtils.installDeployerToolWithVersion(
                        TERRAFORM_EXECUTOR_NAME_PREFIX,
                        bestVersionNumber,
                        TERRAFORM_BINARY_DOWNLOAD_URL_FORMAT,
                        this.terraformDownloadBaseUrl,
                        this.terraformInstallDir);
        if (deployerToolUtils.checkIfExecutorCanBeExecuted(
                installedExecutorFile, TERRAFORM_VERSION_COMMAND_ARGUMENT)) {
            log.info("Terraform with version {}  installed successfully.", installedExecutorFile);
            return installedExecutorFile.getAbsolutePath();
        }
        String errorMsg =
                String.format(
                        "Installing terraform with version %s into the dir %s " + "failed. ",
                        bestVersionNumber, this.terraformInstallDir);
        log.error(errorMsg);
        throw new InvalidDeployerToolException(errorMsg);
    }
}
