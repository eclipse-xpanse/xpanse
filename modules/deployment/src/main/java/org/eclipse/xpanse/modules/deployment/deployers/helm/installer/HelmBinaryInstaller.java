/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.helm.installer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.deployment.config.DeploymentProperties;
import org.eclipse.xpanse.modules.deployment.deployers.deployertools.DeployerTarGzFileManage;
import org.eclipse.xpanse.modules.deployment.deployers.deployertools.DeployerToolUtils;
import org.eclipse.xpanse.modules.models.common.exceptions.InvalidDeployerToolException;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

/** Component to manage helm binary installation based on the service template requirements. */
@Slf4j
@Component
public class HelmBinaryInstaller {

    private static final String HELM_FILE_NAME_IN_TAR_BALL = "/helm";

    /** The pattern of the output of the command helm version. */
    public static final Pattern HELM_VERSION_OUTPUT_PATTERN =
            Pattern.compile("v(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})\\b");

    /**
     * the pattern to be appended to the helmBaseDownloadUrl. In this case, it's just the file name.
     * Format is helm-$TAG-$OS-$ARCH.tar.gz *
     */
    private static final String HELM_BINARY_DOWNLOAD_URL_FORMAT = "helm-v%s-%s-%s.tar.gz";

    private static final String HELM_EXECUTOR_NAME_PREFIX = "helm-";

    private static final String HELM_VERSION_COMMAND = " version";

    private final DeployerToolUtils deployerToolUtils;

    private final DeployerTarGzFileManage deployerTarGzFileManage;

    private final DeploymentProperties deploymentProperties;

    /** Constructor for component. */
    @Autowired
    public HelmBinaryInstaller(
            DeployerToolUtils deployerToolUtils,
            DeployerTarGzFileManage deployerTarGzFileManage,
            DeploymentProperties deploymentProperties) {
        this.deployerToolUtils = deployerToolUtils;
        this.deployerTarGzFileManage = deployerTarGzFileManage;
        this.deploymentProperties = deploymentProperties;
    }

    /**
     * Find the executable binary path of the Helm tool that matches the required version. If no
     * matching executable binary is found, install the Helm tool with the required version and then
     * return the path.
     *
     * @param requiredVersion The required version of Helm tool.
     * @return The path of the executable binary.
     */
    @Retryable(
            retryFor = InvalidDeployerToolException.class,
            maxAttemptsExpression = "${xpanse.http-client-request.retry-max-attempts}",
            backoff =
                    @Backoff(delayExpression = "${xpanse.http-client-request.delay-milliseconds}"))
    public String getExecutorPathThatMatchesRequiredVersion(String requiredVersion) {
        if (StringUtils.isBlank(requiredVersion)) {
            log.info("No required version of helm is specified, use the default helm.");
            return "helm";
        }
        String[] operatorAndNumber =
                deployerToolUtils.getOperatorAndNumberFromRequiredVersion(requiredVersion);
        String requiredOperator = operatorAndNumber[0];
        String requiredNumber = operatorAndNumber[1];
        // Get path of the executor matched required version in the environment.
        String matchedVersionExecutorPath =
                deployerToolUtils.getExecutorPathMatchedRequiredVersion(
                        HELM_EXECUTOR_NAME_PREFIX,
                        HELM_VERSION_COMMAND,
                        HELM_VERSION_OUTPUT_PATTERN,
                        this.getHelmInstallDir().toAbsolutePath().toString(),
                        requiredOperator,
                        requiredNumber);
        if (StringUtils.isBlank(matchedVersionExecutorPath)) {
            log.info(
                    "No helm executor matched the required version {} from the "
                            + "helm installation dir {}, start to download and install one.",
                    requiredVersion,
                    this.getHelmInstallDir().toAbsolutePath());
            return installHelmByRequiredVersion(requiredOperator, requiredNumber);
        }
        return matchedVersionExecutorPath;
    }

    private String installHelmByRequiredVersion(String requiredOperator, String requiredNumber) {
        String bestVersionNumber =
                deployerToolUtils.getBestAvailableVersionMatchingRequiredVersion(
                        DeployerKind.HELM, requiredOperator, requiredNumber);
        Path helmInstaller = getInstallSubDir(bestVersionNumber);
        deployerTarGzFileManage.downloadExtractAndCopyFileToInstallerLocation(
                HELM_FILE_NAME_IN_TAR_BALL,
                DeployerKind.HELM,
                getDownloadFileUrl(bestVersionNumber),
                helmInstaller);
        if (deployerToolUtils.checkIfExecutorCanBeExecuted(
                helmInstaller.toFile(), HELM_VERSION_COMMAND)) {
            log.info("helm with version {}  installed successfully.", bestVersionNumber);
            return helmInstaller.toFile().getAbsolutePath();
        }
        String errorMsg =
                String.format(
                        "Installing helm with version %s into the dir %s " + "failed. ",
                        bestVersionNumber, this.getHelmInstallDir().toAbsolutePath());
        log.error(errorMsg);
        throw new InvalidDeployerToolException(errorMsg);
    }

    private Path getInstallSubDir(String bestVersion) {
        return Paths.get(
                this.getHelmInstallDir().toAbsolutePath().toString(),
                HELM_EXECUTOR_NAME_PREFIX + bestVersion);
    }

    private String getArchitecture() {
        String archName = System.getProperty("os.arch").toLowerCase();
        return switch (archName) {
            case "x86_64", "amd64" -> "amd64";
            case "aarch64" -> "arm64";
            case "i386", "i686", "x86" -> "386";
            default ->
                    throw new InvalidDeployerToolException("Unsupported architecture: " + archName);
        };
    }

    private String getDownloadFileUrl(String version) {
        return this.deploymentProperties.getHelm().getDownloadBaseUrl()
                + String.format(
                        HELM_BINARY_DOWNLOAD_URL_FORMAT,
                        version,
                        System.getProperty("os.name").toLowerCase(),
                        getArchitecture());
    }

    private Path getHelmInstallDir() {
        if (StringUtils.isBlank(deploymentProperties.getHelm().getInstallDir())) {
            return deployerToolUtils.getUserAppInstallFolder();
        } else {
            return Paths.get(deploymentProperties.getHelm().getInstallDir());
        }
    }
}
