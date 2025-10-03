/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformlocal;

import java.io.File;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.deployment.deployers.deployertools.DeployerToolUtils;
import org.eclipse.xpanse.modules.deployment.deployers.deployertools.DeployerZipFileManage;
import org.eclipse.xpanse.modules.models.common.exceptions.InvalidDeployerToolException;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    private static final String TERRAFORM_EXECUTOR_NAME_IN_INSTALLER_ZIP = "terraform";

    private static final String OS_NAME = System.getProperty("os.name").toLowerCase();
    private static final String OS_ARCH = System.getProperty("os.arch").toLowerCase();

    private final String terraformDownloadBaseUrl;

    private final String terraformInstallDir;

    private final DeployerToolUtils deployerToolUtils;

    private final DeployerZipFileManage deployerZipFileManage;

    /** Constructor method for the bean. */
    @Autowired
    public TerraformInstaller(
            @Value(
                            "${deployer.terraform.download.base.url:https://releases.hashicorp.com/terraform}")
                    String terraformDownloadBaseUrl,
            @Value("${deployer.terraform.install.dir}") String terraformInstallDir,
            DeployerToolUtils deployerToolUtils,
            DeployerZipFileManage deployerZipFileManage) {
        this.terraformDownloadBaseUrl = terraformDownloadBaseUrl;
        this.deployerToolUtils = deployerToolUtils;
        this.deployerZipFileManage = deployerZipFileManage;
        // if not specific location is provided, then use the default user's app location.
        this.terraformInstallDir =
                terraformInstallDir.isBlank()
                        ? deployerToolUtils.getUserAppInstallFolder().toFile().getAbsolutePath()
                        : terraformInstallDir;
        log.info("Terraform deployer uses {} for installing binaries", this.terraformInstallDir);
    }

    /**
     * Find the executable binary path of the Terraform tool that matches the required version. If
     * no matching executable binary is found, install the Terraform tool with the required version
     * and then return the path.
     *
     * @param requiredVersion The required version of Terraform tool.
     * @return The path of the executable binary.
     */
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
                deployerZipFileManage.downloadZipExtractAndInstall(
                        TERRAFORM_EXECUTOR_NAME_IN_INSTALLER_ZIP,
                        getExecutorNameWithVersion(
                                TERRAFORM_EXECUTOR_NAME_PREFIX, bestVersionNumber),
                        getExecutorBinaryDownloadUrl(bestVersionNumber),
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

    private String getExecutorBinaryDownloadUrl(String versionNumber) {
        return String.format(
                TERRAFORM_BINARY_DOWNLOAD_URL_FORMAT,
                this.terraformDownloadBaseUrl,
                versionNumber,
                versionNumber,
                getOperatingSystemCode(),
                OS_ARCH);
    }

    private String getOperatingSystemCode() {
        if (OS_NAME.contains("windows")) {
            return "windows";
        } else if (OS_NAME.contains("linux")) {
            return "linux";
        } else if (OS_NAME.contains("mac")) {
            return "darwin";
        } else if (OS_NAME.contains("freebsd")) {
            return "freebsd";
        } else if (OS_NAME.contains("openbsd")) {
            return "openbsd";
        } else if (OS_NAME.contains("solaris") || OS_NAME.contains("sunos")) {
            return "solaris";
        }
        return "Unsupported OS";
    }

    public String getExecutorNameWithVersion(String executorNamePrefix, String versionNumber) {
        return executorNamePrefix + versionNumber;
    }
}
