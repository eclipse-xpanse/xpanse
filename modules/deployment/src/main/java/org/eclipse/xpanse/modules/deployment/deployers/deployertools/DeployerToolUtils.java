/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.deployertools;

import jakarta.annotation.Resource;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.common.systemcmd.SystemCmd;
import org.eclipse.xpanse.common.systemcmd.SystemCmdResult;
import org.eclipse.xpanse.modules.models.common.exceptions.InvalidDeployerToolException;
import org.eclipse.xpanse.modules.models.servicetemplate.DeployerTool;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.semver4j.Semver;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * Utils for DeployerTool.
 */
@Slf4j
@Component
public class DeployerToolUtils {

    private static final Pattern DEPLOYER_TOOL_REQUIRED_VERSION_PATTERN =
            Pattern.compile(DeployerTool.DEPLOYER_TOOL_REQUIRED_VERSION_REGEX);
    private static final String OS_NAME = System.getProperty("os.name").toLowerCase();
    private static final String OS_ARCH = System.getProperty("os.arch").toLowerCase();
    private final SystemCmd systemCmd = new SystemCmd();
    @Resource
    private DeployerToolVersionsCacheManager versionsCacheManager;

    /**
     * Get executor path which matches the required version.
     *
     * @param executorNamePrefix          executor name prefix
     * @param versionCommandOutputPattern pattern to get version from command output
     * @param installationDir             installation directory
     * @param requiredOperator            operator in required version
     * @param requiredNumber              number in required version
     * @return return the version of which is matched required, otherwise return null.
     */
    public String getExecutorPathMatchedRequiredVersion(String executorNamePrefix,
                                                        Pattern versionCommandOutputPattern,
                                                        String installationDir,
                                                        String requiredOperator,
                                                        String requiredNumber) {
        // Get path of executor matched required version in the installation dir.
        File installDir = new File(installationDir);
        if (!installDir.exists() || !installDir.isDirectory()) {
            return null;
        }
        Map<String, File> executorVersionFileMap = new HashMap<>();
        Arrays.stream(installDir.listFiles())
                .filter(f -> f.isFile() && f.canExecute()
                        && f.getName().startsWith(executorNamePrefix))
                .forEach(f -> {
                    String versionNumber =
                            getVersionFromExecutorPath(f.getAbsolutePath(), executorNamePrefix);
                    executorVersionFileMap.put(versionNumber, f);
                });
        if (CollectionUtils.isEmpty(executorVersionFileMap)) {
            return null;
        }
        String findBestVersion = findBestVersion(executorVersionFileMap.keySet(),
                requiredOperator, requiredNumber);
        if (StringUtils.isNotBlank(findBestVersion)) {
            File executorFile = executorVersionFileMap.get(findBestVersion);
            if (checkIfExecutorIsMatchedRequiredVersion(executorFile, versionCommandOutputPattern,
                    requiredOperator, requiredNumber)) {
                log.info("Found the installed executor {} matched the required version {} "
                                + "successfully.", executorFile.getAbsolutePath(),
                        requiredOperator + requiredNumber);
                return executorFile.getAbsolutePath();
            }
        }
        return null;
    }

    /**
     * Install the executor with specific version into the path.
     *
     * @param executorNamePrefix      executor name prefix
     * @param versionNumber           version number
     * @param binaryDownloadUrlFormat binary download url format
     * @param downloadBaseUrl         download base url
     * @param installationDir         installation directory
     * @return executor path
     */
    public File installDeployerToolWithVersion(String executorNamePrefix, String versionNumber,
                                               String binaryDownloadUrlFormat,
                                               String downloadBaseUrl, String installationDir) {
        // Install the executor with specific version into the path.
        String executorName = getExecutorNameWithVersion(executorNamePrefix, versionNumber);
        File executorFile = new File(installationDir, executorName);
        File parentDir = executorFile.getParentFile();
        try {
            if (!parentDir.exists()) {
                log.info("Created the installation dir {} {}.", parentDir.getAbsolutePath(),
                        parentDir.mkdirs() ? "successfully" : "failed");
            }
            // download the binary zip file from website into the installation directory
            File terraformZipFile =
                    downloadDeployerToolBinaryZipFile(binaryDownloadUrlFormat, downloadBaseUrl,
                            versionNumber, installationDir);
            // extract the executable binary from the zip file
            extractExecutorFromZipFile(terraformZipFile, executorFile, executorNamePrefix);
        } catch (IOException e) {
            String errorMsg =
                    String.format("Failed to install deployer tool with version %s.", executorName);
            log.error(errorMsg, e);
            throw new InvalidDeployerToolException(errorMsg);
        }
        // delete the non-executable files
        deleteNonExecutorFiles(parentDir, executorNamePrefix);
        return executorFile;
    }


    private File downloadDeployerToolBinaryZipFile(String binaryDownloadUrlFormat,
                                                   String downloadBaseUrl, String versionNumber,
                                                   String installationDir) throws IOException {
        String binaryDownloadUrl = getExecutorBinaryDownloadUrl(
                binaryDownloadUrlFormat, downloadBaseUrl, versionNumber);
        String binaryZipFileName = getExecutorBinaryZipFileName(binaryDownloadUrl);
        File binaryZipFile = new File(installationDir, binaryZipFileName);
        URL url = URI.create(binaryDownloadUrl).toURL();
        try (ReadableByteChannel rbc = Channels.newChannel(url.openStream());
                FileOutputStream fos = new FileOutputStream(binaryZipFile, false)) {
            log.info("Downloading deployer tool binary file from {} to {}", url,
                    binaryZipFile.getAbsolutePath());
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            log.info("Downloaded deployer tool binary file from {} to {} successfully.", url,
                    binaryZipFile.getAbsolutePath());
        }
        return binaryZipFile;
    }


    private void extractExecutorFromZipFile(File binaryZipFile, File executorFile,
                                            String executorNamePrefix) throws IOException {
        if (!binaryZipFile.exists()) {
            String errorMsg = String.format("Deployer tool binary zip file %s not found.",
                    binaryZipFile.getAbsolutePath());
            log.error(errorMsg);
            throw new IOException(errorMsg);
        }
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(binaryZipFile))) {
            log.info("Unzipping deployer tool binary zip file {}", binaryZipFile.getAbsolutePath());
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    String entryName = entry.getName();
                    File entryDestinationFile = new File(executorFile.getParentFile(), entryName);
                    if (isExecutorFileInZipForTerraform(entryName, executorNamePrefix)) {
                        extractFile(zis, entryDestinationFile);
                        Files.move(entryDestinationFile.toPath(), executorFile.toPath(),
                                StandardCopyOption.REPLACE_EXISTING);
                        log.info("Unzipped deployer tool file {} and extract the executor {} "
                                        + "successfully.", binaryZipFile.getAbsolutePath(),
                                executorFile.getAbsolutePath());
                    }
                }
            }
        }
    }

    private boolean isExecutorFileInZipForTerraform(String entryName, String executorNamePrefix) {
        executorNamePrefix = executorNamePrefix.substring(0, executorNamePrefix.length() - 1);
        return entryName.startsWith(executorNamePrefix);
    }


    private void extractFile(ZipInputStream zis, File destinationFile) throws IOException {
        try (BufferedOutputStream bos = new BufferedOutputStream(
                new FileOutputStream(destinationFile))) {
            byte[] bytesIn = new byte[4096];
            int read;
            while ((read = zis.read(bytesIn)) != -1) {
                bos.write(bytesIn, 0, read);
            }
        }
    }

    private void deleteNonExecutorFiles(File dir, String executorNamePrefix) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteNonExecutorFiles(file, executorNamePrefix);
                } else {
                    if (!file.getName().startsWith(executorNamePrefix) && !file.delete()) {
                        log.warn("Failed to delete file {}.", file.getAbsolutePath());
                    }
                }
            }
        }
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
     * @param requiredNumber   number in required version
     * @return the best available version existed in download url.
     */
    public String getBestAvailableVersionMatchingRequiredVersion(DeployerKind deployerKind,
                                                                 String requiredOperator,
                                                                 String requiredNumber) {
        Set<String> availableVersions =
                versionsCacheManager.getAvailableVersionsOfDeployerTool(deployerKind);
        String bestAvailableVersion =
                findBestVersion(availableVersions, requiredOperator, requiredNumber);
        if (StringUtils.isNotBlank(bestAvailableVersion)) {
            log.info("Found the best available version {} for the deployer tool {} by the "
                            + "required version {}.", bestAvailableVersion, deployerKind.toValue(),
                    requiredOperator + requiredNumber);
            return bestAvailableVersion;
        }
        String errorMsg = String.format("Failed to find available versions for the "
                        + "deployer tool %s by the required version %s.",
                deployerKind.toValue(), requiredOperator + requiredNumber);
        log.error(errorMsg);
        throw new InvalidDeployerToolException(errorMsg);
    }

    /**
     * Find the best version from all available versions.
     *
     * @param allAvailableVersions all available versions.
     * @param requiredOperator     operator of the required version.
     * @param requiredNumber       number of the required version.
     * @return the best version.
     */
    private String findBestVersion(Set<String> allAvailableVersions, String requiredOperator,
                                   String requiredNumber) {
        if (CollectionUtils.isEmpty(allAvailableVersions) || StringUtils.isBlank(requiredOperator)
                || StringUtils.isBlank(requiredNumber)) {
            return null;
        }
        Semver requiredSemver = new Semver(requiredNumber);
        return switch (requiredOperator) {
            case "=" -> allAvailableVersions.stream()
                    .filter(v -> new Semver(v).isEqualTo(requiredSemver)).findAny().orElse(null);
            case ">=" -> allAvailableVersions.stream()
                    .filter(v -> new Semver(v).isGreaterThanOrEqualTo(requiredSemver))
                    .min(Comparator.naturalOrder()).orElse(null);
            case "<=" -> allAvailableVersions.stream()
                    .filter(v -> new Semver(v).isLowerThanOrEqualTo(requiredSemver))
                    .max(Comparator.naturalOrder()).orElse(null);
            default -> null;
        };
    }

    /**
     * Check the version of installed executor is matched required version.
     *
     * @param executorFile     executor file
     * @param requiredOperator operator in required version
     * @param requiredNumber   number in required version
     * @return true if the version is valid, otherwise return false.
     */
    public boolean checkIfExecutorIsMatchedRequiredVersion(File executorFile,
                                                           Pattern versionCommandOutputPattern,
                                                           String requiredOperator,
                                                           String requiredNumber) {
        String versionNumber = getExactVersionOfExecutor(
                executorFile.getAbsolutePath(), versionCommandOutputPattern);
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
    public boolean checkIfExecutorCanBeExecuted(File executorFile) {
        String versionOutput = getVersionCommandOutput(executorFile);
        return StringUtils.isNotBlank(versionOutput);
    }

    /**
     * Get exact version of executor.
     *
     * @param executorPath executor path
     * @return exact version of executor.
     */
    public String getExactVersionOfExecutor(String executorPath,
                                            Pattern versionCommandOutputPattern) {
        String versionOutput = getVersionCommandOutput(new File(executorPath));
        Matcher matcher = versionCommandOutputPattern.matcher(versionOutput);
        if (matcher.find()) {
            // return only the version number.
            return matcher.group(1);
        }
        return null;
    }


    private String getVersionCommandOutput(File executorFile) {
        try {
            if (!executorFile.exists() && !executorFile.isFile()) {
                return null;
            }
            if (!executorFile.canExecute()) {
                SystemCmdResult chmodResult = systemCmd.execute(
                        String.format("chmod +x %s", executorFile.getAbsolutePath()));
                if (!chmodResult.isCommandSuccessful()) {
                    log.error(chmodResult.getCommandStdError());
                }
            }
            SystemCmdResult versionCheckResult =
                    systemCmd.execute(executorFile.getAbsolutePath() + " version");
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

    private boolean isVersionSatisfied(String actualNumber, String requiredOperator,
                                       String requiredNumber) {
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


    /**
     * Get executor name with version.
     *
     * @param versionNumber version number
     * @return binary file name
     */
    public String getExecutorNameWithVersion(String executorNamePrefix, String versionNumber) {
        return executorNamePrefix + versionNumber;
    }


    /**
     * Get whole download url of the executor binary file.
     *
     * @param binaryDownloadUrlFormat binary download url format
     * @param downloadBaseUrl         download base url
     * @param versionNumber           version number
     * @return whole download url of the executor binary file
     */
    private String getExecutorBinaryDownloadUrl(String binaryDownloadUrlFormat,
                                                String downloadBaseUrl, String versionNumber) {
        return String.format(binaryDownloadUrlFormat, downloadBaseUrl, versionNumber, versionNumber,
                getOperatingSystemCode(), OS_ARCH);
    }

    private String getExecutorBinaryZipFileName(String executorBinaryDownloadUrl) {
        return executorBinaryDownloadUrl.substring(executorBinaryDownloadUrl.lastIndexOf("/") + 1);
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
}
