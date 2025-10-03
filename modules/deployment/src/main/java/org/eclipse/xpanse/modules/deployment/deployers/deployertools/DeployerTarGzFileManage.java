/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.deployertools;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.GZIPInputStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.FileUtils;
import org.eclipse.xpanse.modules.deployment.deployers.helm.exceptions.HelmBinaryInstallationFailedException;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.springframework.stereotype.Component;

/**
 * Component responsible for downloading the tar.gz files and helm distribution websites and install
 * it.
 */
@Slf4j
@Component
public class DeployerTarGzFileManage {

    /**
     * Main method to download, unpack and copy the binary.
     *
     * @param binaryFileNameInTarBall name of the binary file in the tar ball that must be copied.
     * @param deployerKind type of the deployer.
     * @param url full url of the file to be downloaded
     * @param installPath Path where the binary must be finally copied to.
     */
    public void downloadExtractAndCopyFileToInstallerLocation(
            String binaryFileNameInTarBall,
            DeployerKind deployerKind,
            String url,
            Path installPath) {
        Path parentInstallFolder = installPath.getParent();
        Path temporaryFileForTarGz;
        try {
            if (parentInstallFolder != null) {
                Files.createDirectories(parentInstallFolder);
            } else {
                throw new HelmBinaryInstallationFailedException(
                        "installation path provided is null.");
            }
            temporaryFileForTarGz = Files.createTempFile(deployerKind.toValue(), ".tar.gz");

            // Downloads the tar file and write it directly to temp file.
            try (InputStream in = URI.create(url).toURL().openStream()) {
                log.info("Downloading binaries from {}", url);
                Files.copy(in, temporaryFileForTarGz, StandardCopyOption.REPLACE_EXISTING);
            }
            // Chain streams - gzip is streamed as input to tar and
            // then tar extracts only the helm binary form the tar ball.
            try (TarArchiveInputStream tarIn =
                    new TarArchiveInputStream(
                            new GZIPInputStream(Files.newInputStream(temporaryFileForTarGz)))) {
                TarArchiveEntry entry;
                // gets the next file in the tar ball.
                while ((entry = tarIn.getNextEntry()) != null) {
                    if (entry.isDirectory()) {
                        continue;
                    }
                    // Takes only the helm binary file.
                    if (entry.getName().endsWith(binaryFileNameInTarBall)) {
                        Files.copy(tarIn, installPath, StandardCopyOption.REPLACE_EXISTING);
                        break;
                    }
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new HelmBinaryInstallationFailedException(e.getMessage());
        }
        FileUtils.deleteQuietly(temporaryFileForTarGz.toFile());
    }
}
