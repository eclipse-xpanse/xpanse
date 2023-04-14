/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Executes operating system commands.
 */
@Slf4j
public class SystemCmd {

    @Setter
    @Getter
    private Map<String, String> env = null;

    @Setter
    @Getter
    private String workDir = "";

    public SystemCmdResult execute(String cmd) {
        return execute(cmd, 0);
    }

    /**
     * Executes operating system command.
     *
     * @param cmd        command to be executed.
     * @param waitSecond time to wait for the command to be completed.
     * @return returns SystemCmdResult object which has all the execution details.
     */
    public SystemCmdResult execute(String cmd, int waitSecond) {
        SystemCmdResult systemCmdResult = new SystemCmdResult();
        systemCmdResult.setCommandExecuted(cmd);
        log.info("SystemCmd executing cmd: " + String.join(" ", cmd));
        try {
            String[] safeCmd = cmd.split(" +");
            ProcessBuilder processBuilder = new ProcessBuilder(safeCmd);
            if (this.env != null) {
                processBuilder.environment().putAll(this.env);
            }
            if (!Objects.equals(workDir, "")) {
                processBuilder.directory(new File(workDir));
            }
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            BufferedReader outputReader =
                    new BufferedReader(new InputStreamReader((process.getInputStream())));
            String line;
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = outputReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            systemCmdResult.setCommandOutput(stringBuilder.toString());

            if (waitSecond <= 0) {
                process.waitFor();
            } else {
                if (!process.waitFor(waitSecond, TimeUnit.SECONDS)) {
                    log.error("SystemCmd wait process failed");
                    systemCmdResult.setCommandSuccessful(false);
                }
            }
            if (process.exitValue() != 0) {
                log.error("SystemCmd process finished with abnormal value.");
                systemCmdResult.setCommandSuccessful(false);
            } else {
                systemCmdResult.setCommandSuccessful(true);
            }
        } catch (final IOException ex) {
            systemCmdResult.setCommandSuccessful(false);
        } catch (final InterruptedException ex) {
            log.error("SystemCmd process be interrupted.");
            Thread.currentThread().interrupt();
            systemCmdResult.setCommandSuccessful(false);
        }
        return systemCmdResult;
    }
}
