/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.osc.orchestrator.plugin.huaweicloud.builders.utils;

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

    public boolean execute(String cmd, StringBuilder stdErrOut) {
        return execute(cmd, stdErrOut, 0);
    }

    /**
     * Executes operating system command.
     *
     * @param cmd        command to be executed.
     * @param stdErrOut  object to which the output of the command is appended to.
     * @param waitSecond time to wait for the command to be completed.
     * @return returns true if command was successfully executed else returns false.
     */
    public boolean execute(String cmd, StringBuilder stdErrOut, long waitSecond) {
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
            while ((line = outputReader.readLine()) != null) {
                stdErrOut.append(line).append("\n");
                log.info(line);
            }

            if (waitSecond == 0) {
                process.waitFor();
            } else {
                if (!process.waitFor(waitSecond, TimeUnit.SECONDS)) {
                    log.error("SystemCmd wait process failed. {}", String.join(" ", cmd));
                    throw new IllegalStateException("SystemCmd wait process failed. \nCmd:\n" + cmd
                            + "\nOutput:\n" + stdErrOut.toString());
                }
            }
            if (process.exitValue() != 0) {
                log.error("SystemCmd process finished with abnormal value.");
                throw new IllegalStateException(
                        "SystemCmd process finished with abnormal value. \nCmd:\n" + cmd
                                + "\nOutput:" + stdErrOut.toString());
            }
        } catch (final IOException ex) {
            throw new IllegalStateException(cmd + stdErrOut.toString(), ex);
        } catch (final InterruptedException ex) {
            log.error("SystemCmd process be interrupted.");
            Thread.currentThread().interrupt();
        }

        return true;
    }
}
