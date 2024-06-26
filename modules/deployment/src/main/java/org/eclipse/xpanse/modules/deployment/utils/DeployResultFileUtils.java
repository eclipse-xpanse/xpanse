/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.utils;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionTimeoutException;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.FileLockedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Tool class for files after deployment is completed.
 */
@Slf4j
@Component
public class DeployResultFileUtils {

    private static final String MODE = "rw";
    @Value("${wait.time.for.deploy.result.file.lock.in.seconds}")
    private int awaitAtMost;
    @Value("${polling.interval.for.deploy.result.file.lock.check.in.seconds}")
    private int awaitPollingInterval;

    /**
     * Method to await for the tfstate file to be unlocked.
     */
    public void waitUntilFileIsNotLocked(String path) {
        try {
            Awaitility.await()
                    .atMost(Duration.ofSeconds(awaitAtMost))
                    .pollInterval(Duration.ofSeconds(awaitPollingInterval))
                    .pollDelay(0, TimeUnit.SECONDS)
                    .until(() -> !isTfStateFileLocked(path));
        } catch (ConditionTimeoutException e) {
            String errorMsg = String.format(
                    "Timeout waiting for file to be unlocked, %s", e.getMessage());
            log.info(errorMsg);
            throw new FileLockedException(errorMsg);
        }
    }

    private boolean isTfStateFileLocked(String filePath) {
        try (RandomAccessFile file = new RandomAccessFile(filePath, MODE);
                FileChannel channel = file.getChannel()) {
            try (FileLock lock = channel.tryLock()) {
                if (lock != null) {
                    lock.release();
                    return false;
                }
            }
        } catch (IOException e) {
            return true;
        }
        return true;
    }

}
