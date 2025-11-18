/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.deployertools;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.common.systemcmd.SystemCmd;
import org.eclipse.xpanse.common.systemcmd.SystemCmdResult;
import org.eclipse.xpanse.modules.deployment.deployers.deployertools.cache.DeployerToolVersionsCacheManager;
import org.eclipse.xpanse.modules.models.common.exceptions.InvalidDeployerToolException;
import org.eclipse.xpanse.modules.models.servicetemplate.DeployerTool;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.semver4j.Semver;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/** Utils for DeployerTool. */
@Slf4j
@Component
public class DeployerToolUtils {

    private static final String POSIX_USER_INSTALL_FOLDER = ".local/bin";
    private static final String WINDOWS_USER_INSTALL_FOLDER = "USERPROFILE";
    private static final Pattern DEPLOYER_TOOL_REQUIRED_VERSION_PATTERN =
            Pattern.compile(DeployerTool.DEPLOYER_TOOL_REQUIRED_VERSION_REGEX);
    private final SystemCmd systemCmd = new SystemCmd();
    private final DeployerToolVersionsCacheManager versionsCacheManager;

    /** Constructor method. */
    public DeployerToolUtils(DeployerToolVersionsCacheManager versionsCacheManager) {
        this.versionsCacheManager = versionsCacheManager;
    }

    /**
     * Get executor path which matches the required version.
     *
     * @param executorNamePrefix executor name prefix
     * @param versionCommandArgument argument to be passed to the binary to output the version
     * @param versionCommandOutputPattern pattern to get version from command output
     * @param installationDir installation directory
     * @param requiredOperator operator in required version
     * @param requiredNumber number in required version
     * @return return the version of which is matched required, otherwise return null.
     */
    public String getExecutorPathMatchedRequiredVersion(
            String executorNamePrefix,
            String versionCommandArgument,
            Pattern versionCommandOutputPattern,
            String installationDir,
            String requiredOperator,
            String requiredNumber) {
        // Get path of executor matched required version in the installation dir.
        File installDir = new File(installationDir);
        if (!installDir.exists() || !installDir.isDirectory()) {
            return null;
        }
        File[] files = installDir.listFiles();
        if (Objects.isNull(files) || files.length == 0) {
            return null;
        }
        Map<String, File> executorVersionFileMap = new HashMap<>();
        Arrays.stream(files)
                .filter(
                        f ->
                                f.isFile()
                                        && f.canExecute()
                                        && f.getName().startsWith(executorNamePrefix))
                .forEach(
                        f -> {
                            String versionNumber =
                                    getVersionFromExecutorPath(
                                            f.getAbsolutePath(), executorNamePrefix);
                            executorVersionFileMap.put(versionNumber, f);
                        });
        if (CollectionUtils.isEmpty(executorVersionFileMap)) {
            return null;
        }
        String findBestVersion =
                findBestVersion(executorVersionFileMap.keySet(), requiredOperator, requiredNumber);
        if (StringUtils.isNotBlank(findBestVersion)) {
            File executorFile = executorVersionFileMap.get(findBestVersion);
            if (checkIfExecutorIsMatchedRequiredVersion(
                    executorFile,
                    versionCommandArgument,
                    versionCommandOutputPattern,
                    requiredOperator,
                    requiredNumber)) {
                log.info(
                        "Found the installed executor {} matched the required version {} "
                                + "successfully.",
                        executorFile.getAbsolutePath(),
                        requiredOperator + requiredNumber);
                return executorFile.getAbsolutePath();
            }
        }
        return null;
    }

    /**
     * Get the operator and number from the required version.
     *
     * @param requiredVersion required version
     * @return string array, the first element is operator, the second element is number.
     */
    public String[] getOperatorAndNumberFromRequiredVersion(String requiredVersion) {

        String version = requiredVersion.replaceAll("\\s+", "").toLowerCase().replaceAll("v", "");
        if (StringUtils.isNotBlank(version)) {
            Matcher matcher = DEPLOYER_TOOL_REQUIRED_VERSION_PATTERN.matcher(version);
            if (matcher.find()) {
                String[] operatorAndNumber = new String[2];
                operatorAndNumber[0] = matcher.group(1);
                operatorAndNumber[1] = matcher.group(0).replaceAll("^(=|>=|<=)", "");
                return operatorAndNumber;
            }
        }
        String errorMsg =
                String.format("Invalid deployer tool required version format:%s", requiredVersion);
        throw new InvalidDeployerToolException(errorMsg);
    }

    /**
     * Get the best available version to install executor with required version.
     *
     * @param requiredOperator operator in required version
     * @param requiredNumber number in required version
     * @return the best available version existed in download url.
     */
    public String getBestAvailableVersionMatchingRequiredVersion(
            DeployerKind deployerKind, String requiredOperator, String requiredNumber) {
        Set<String> availableVersions =
                versionsCacheManager.getAvailableVersionsOfDeployerTool(deployerKind);
        String bestAvailableVersion =
                findBestVersion(availableVersions, requiredOperator, requiredNumber);
        if (StringUtils.isNotBlank(bestAvailableVersion)) {
            log.info(
                    "Found the best available version {} for the deployer tool {} by the "
                            + "required version {}.",
                    bestAvailableVersion,
                    deployerKind.toValue(),
                    requiredOperator + requiredNumber);
            return bestAvailableVersion;
        }
        String errorMsg =
                String.format(
                        "Failed to find available versions for the "
                                + "deployer tool %s by the required version %s.",
                        deployerKind.toValue(), requiredOperator + requiredNumber);
        log.error(errorMsg);
        throw new InvalidDeployerToolException(errorMsg);
    }

    /**
     * Find the best version from all available versions.
     *
     * @param allAvailableVersions all available versions.
     * @param requiredOperator operator of the required version.
     * @param requiredNumber number of the required version.
     * @return the best version.
     */
    private String findBestVersion(
            Set<String> allAvailableVersions, String requiredOperator, String requiredNumber) {
        if (CollectionUtils.isEmpty(allAvailableVersions)
                || StringUtils.isBlank(requiredOperator)
                || StringUtils.isBlank(requiredNumber)) {
            return null;
        }
        Semver requiredSemver = new Semver(requiredNumber);
        return switch (requiredOperator) {
            case "=" ->
                    allAvailableVersions.stream()
                            .filter(v -> new Semver(v).isEqualTo(requiredSemver))
                            .findAny()
                            .orElse(null);
            case ">=" ->
                    allAvailableVersions.stream()
                            .filter(v -> new Semver(v).isGreaterThanOrEqualTo(requiredSemver))
                            .min(Comparator.naturalOrder())
                            .orElse(null);
            case "<=" ->
                    allAvailableVersions.stream()
                            .filter(v -> new Semver(v).isLowerThanOrEqualTo(requiredSemver))
                            .max(Comparator.naturalOrder())
                            .orElse(null);
            default -> null;
        };
    }

    /**
     * Check the version of installed executor is matched required version.
     *
     * @param executorFile executor file
     * @param requiredOperator operator in required version
     * @param requiredNumber number in required version
     * @return true if the version is valid, otherwise return false.
     */
    public boolean checkIfExecutorIsMatchedRequiredVersion(
            File executorFile,
            String versionCommandArgument,
            Pattern versionCommandOutputPattern,
            String requiredOperator,
            String requiredNumber) {
        String versionNumber =
                getExactVersionOfExecutor(
                        executorFile.getAbsolutePath(),
                        versionCommandArgument,
                        versionCommandOutputPattern);
        if (StringUtils.isNotBlank(versionNumber)) {
            return isVersionSatisfied(versionNumber, requiredOperator, requiredNumber);
        }
        return false;
    }

    /**
     * Check if the executor can be executed.
     *
     * @param executorFile executor file
     * @return If true, the executor can be executed, otherwise return false.
     */
    public boolean checkIfExecutorCanBeExecuted(File executorFile, String versionCommandArgument) {
        String versionOutput = getVersionCommandOutput(executorFile, versionCommandArgument);
        return StringUtils.isNotBlank(versionOutput);
    }

    /**
     * Get exact version of executor.
     *
     * @param executorPath executor path
     * @return exact version of executor.
     */
    public String getExactVersionOfExecutor(
            String executorPath,
            String versionCommandArgument,
            Pattern versionCommandOutputPattern) {
        String versionOutput =
                getVersionCommandOutput(new File(executorPath), versionCommandArgument);
        if (StringUtils.isNotBlank(versionOutput)) {
            Matcher matcher = versionCommandOutputPattern.matcher(versionOutput);
            if (matcher.find()) {
                // return only the version number.
                return matcher.group(1);
            }
        }
        return null;
    }

    private String getVersionCommandOutput(File executorFile, String versionCommandArguments) {
        try {
            if (!executorFile.exists() && !executorFile.isFile()) {
                return null;
            }
            if (!executorFile.canExecute()) {
                SystemCmdResult chmodResult =
                        systemCmd.execute(
                                String.format("chmod +x %s", executorFile.getAbsolutePath()));
                if (!chmodResult.isCommandSuccessful()) {
                    log.error(chmodResult.getCommandStdError());
                }
            }
            SystemCmdResult versionCheckResult =
                    systemCmd.execute(executorFile.getAbsolutePath() + versionCommandArguments);
            if (versionCheckResult.isCommandSuccessful()) {
                return versionCheckResult.getCommandStdOutput();
            } else {
                log.error(versionCheckResult.getCommandStdError());
                return null;
            }
        } catch (Exception e) {
            log.error("Failed to get version of executor {}.", executorFile.getAbsolutePath(), e);
            return null;
        }
    }

    private String getVersionFromExecutorPath(String executorPath, String executorNamePrefix) {
        if (executorPath.contains(executorNamePrefix)) {
            return Arrays.asList(executorPath.split(executorNamePrefix)).getLast();
        }
        return null;
    }

    private boolean isVersionSatisfied(
            String actualNumber, String requiredOperator, String requiredNumber) {
        Semver actualSemver = new Semver(actualNumber);
        Semver requiredSemver = new Semver(requiredNumber);
        if ("=".equals(requiredOperator)) {
            return actualSemver.isEqualTo(requiredSemver);
        } else if (">=".equals(requiredOperator)) {
            return actualSemver.isGreaterThanOrEqualTo(requiredSemver);
        } else if ("<=".equals(requiredOperator)) {
            return actualSemver.isLowerThanOrEqualTo(requiredSemver);
        }
        return false;
    }

    public String getFileNameFromDownloadUrl(String executorBinaryDownloadUrl) {
        return executorBinaryDownloadUrl.substring(executorBinaryDownloadUrl.lastIndexOf("/") + 1);
    }

    /**
     * Function returns the user's default local binary installation folder. It handles both windows
     * and POSIX operating systems.
     */
    public Path getUserAppInstallFolder() {
        String os = System.getProperty("os.name").toLowerCase();
        // install binary on the local folders where the users have full access.
        if (os.contains("win")) {
            return Paths.get(
                    System.getenv(WINDOWS_USER_INSTALL_FOLDER),
                    "AppData",
                    "Local",
                    "Microsoft",
                    "WindowsApps");
        } else {
            return Paths.get(FileUtils.getUserDirectoryPath(), POSIX_USER_INSTALL_FOLDER);
        }
    }
}
