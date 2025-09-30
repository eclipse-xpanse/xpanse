/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.deployertools;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.eclipse.xpanse.modules.models.common.exceptions.InvalidDeployerToolException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Component manages downloading, extracting binaries from zip files. */
@Slf4j
@Component
public class DeployerZipFileManage {

    private final DeployerToolUtils deployerToolUtils;

    @Autowired
    public DeployerZipFileManage(DeployerToolUtils deployerToolUtils) {
        this.deployerToolUtils = deployerToolUtils;
    }

    /**
     * Install the executor with specific version into the path. The function supports only
     * downloading zip files.
     *
     * @param executorNameInZipFile executor name prefix. This the name of the required executor
     *     file inside the zip file.
     * @param downloadUrl download base url
     * @param installationDir installation directory
     * @return executor path
     */
    public File downloadZipExtractAndInstall(
            String executorNameInZipFile,
            String executorNameToBeCreated,
            String downloadUrl,
            String installationDir) {
        File executorFile = new File(installationDir, executorNameToBeCreated);
        File parentDir = executorFile.getParentFile();
        File binaryZipFile;
        try {
            if (!parentDir.exists()) {
                log.info(
                        "Creating the installation dir {} {}.",
                        parentDir.getAbsolutePath(),
                        parentDir.mkdirs() ? "is successful" : "failed");
            }
            // download the binary zip file from website into the installation directory
            String binaryZipFileName = deployerToolUtils.getFileNameFromDownloadUrl(downloadUrl);
            binaryZipFile =
                    new File(
                            Files.createTempDirectory(UUID.randomUUID().toString()).toFile(),
                            binaryZipFileName);
            // Downloads the tar file and write it directly to temp file.
            try (InputStream in = URI.create(downloadUrl).toURL().openStream()) {
                log.info("Downloading binaries from {}", downloadUrl);
                Files.copy(in, binaryZipFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            // extract the executable binary from the zip file
            extractExecutorFromZipFile(binaryZipFile, executorFile, executorNameInZipFile);
        } catch (Exception e) {
            String errorMsg =
                    String.format("Failed to install deployer tool %s.", executorNameToBeCreated);
            log.error(errorMsg, e);
            throw new InvalidDeployerToolException(errorMsg);
        }
        // delete the non-executable files
        deleteTemporaryDownloadFolder(binaryZipFile.getParentFile());
        return executorFile;
    }

    private void extractExecutorFromZipFile(
            File binaryZipFile, File executorFile, String executorNameInZipFile)
            throws IOException {
        if (!binaryZipFile.exists()) {
            String errorMsg =
                    String.format(
                            "Deployer tool binary zip file %s not found.",
                            binaryZipFile.getAbsolutePath());
            log.error(errorMsg);
            throw new InvalidDeployerToolException(errorMsg);
        }
        try (ZipFile zipFile = new ZipFile(binaryZipFile)) {
            ZipEntry entry = zipFile.getEntry(executorNameInZipFile);
            if (entry == null) {
                log.error("File {} not found in zip!", executorNameInZipFile);
                throw new InvalidDeployerToolException(
                        String.format("File %s not found in zip!", executorNameInZipFile));
            }
            log.info("Required file found in zip. Extracting it now.");
            try (InputStream in = zipFile.getInputStream(entry)) {
                Files.copy(in, executorFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    private void deleteTemporaryDownloadFolder(File downloadTempFolder) {
        log.info("Deleting temporary download folder at {}", downloadTempFolder.getAbsolutePath());
        FileUtils.deleteQuietly(downloadTempFolder);
    }
}
