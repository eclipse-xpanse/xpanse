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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.TerraformExecutorException;
import org.slf4j.MDC;

/**
 * Executes operating system commands.
 */
@Slf4j
public class SystemCmd {

    @Setter
    @Getter
    private Map<String, String> env;

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
            Process process = processBuilder.start();
            readProcessOutput(process, systemCmdResult);

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
            log.debug("stdout of the command: " + systemCmdResult.getCommandStdOutput());
            log.debug("stderr of the command: " + systemCmdResult.getCommandStdError());
        } catch (final IOException ex) {
            systemCmdResult.setCommandSuccessful(false);
        } catch (final InterruptedException ex) {
            log.error("SystemCmd process be interrupted.");
            Thread.currentThread().interrupt();
            systemCmdResult.setCommandSuccessful(false);
        } catch (ExecutionException e) {
            throw new TerraformExecutorException(e.getMessage());
        }
        return systemCmdResult;
    }


    private String readStream(BufferedReader bufferedReader, Map<String, String> contextMap) {
        //copying MDC context of the main deployment thread to the stream reader thread.
        MDC.setContextMap(contextMap);
        StringBuilder stringBuilder = new StringBuilder();
        bufferedReader.lines().forEach(line -> {
            log.info(line);
            // skip adding new line for the first line.
            if (stringBuilder.length() > 0) {
                stringBuilder.append(System.lineSeparator());
            }
            stringBuilder.append(line);
        });
        return stringBuilder.toString();
    }

    private void readProcessOutput(Process process, SystemCmdResult systemCmdResult)
            throws IOException, ExecutionException, InterruptedException {
        if (Objects.isNull(process)) {
            return;
        }
        final Map<String, String> contextMap = new HashMap<>(
                Objects.nonNull(MDC.getCopyOfContextMap()) ? MDC.getCopyOfContextMap()
                        : new HashMap<>());


        // Starting threads in parallel to read stdout and stderr. This is needed because in
        // some cases reading stdout first works and in some cases reading stderr first works.
        // we now let both stdout and stderr streams to be fully read in parallel and then read
        // the output after the buffers are fully read.
        BufferedReader stdoutReader =
                new BufferedReader(new InputStreamReader(process.getInputStream()));
        ExecutorService threadToReadStdout = Executors.newSingleThreadExecutor();
        Future<String> stdOutFuture =
                threadToReadStdout.submit(() -> readStream(stdoutReader, contextMap));

        BufferedReader stdErrorReader =
                new BufferedReader(new InputStreamReader(process.getErrorStream()));
        ExecutorService threadToReadStdErr = Executors.newSingleThreadExecutor();
        Future<String> stdErrFuture =
                threadToReadStdErr.submit(() -> readStream(stdErrorReader, contextMap));

        while (!stdOutFuture.isDone() || !stdErrFuture.isDone()) {
            log.debug("Command output and error streams are still being read.");
        }

        systemCmdResult.setCommandStdError(stdErrFuture.get());
        systemCmdResult.setCommandStdOutput(stdOutFuture.get());
        threadToReadStdout.shutdown();
        threadToReadStdErr.shutdown();
    }


}
