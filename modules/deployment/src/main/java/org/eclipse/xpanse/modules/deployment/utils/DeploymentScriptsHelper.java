/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.utils;

import jakarta.annotation.Nullable;
import jakarta.annotation.Resource;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionTimeoutException;
import org.eclipse.xpanse.modules.deployment.exceptions.DeploymentScriptsCreationFailedException;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.FileLockedException;
import org.eclipse.xpanse.modules.models.servicetemplate.Deployment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Bean to manage deployment scripts.
 */
@Slf4j
@Component
public class DeploymentScriptsHelper {

    public static final String TF_SCRIPT_FILE_EXTENSION = ".tf";
    public static final String TF_SCRIPT_FILE_NAME = "resources.tf";
    public static final String TF_VARS_FILE_NAME = "variables.tfvars.json";
    public static final String TF_STATE_FILE_NAME = "terraform.tfstate";
    private static final List<String> EXCLUDED_FILE_SUFFIX_LIST =
            Arrays.asList(".tf", ".tfstate", ".binary", ".hcl");
    private static final String MODE = "rw";

    @Value("${wait.time.for.deploy.result.file.lock.in.seconds}")
    private int awaitAtMost;
    @Value("${polling.interval.for.deploy.result.file.lock.check.in.seconds}")
    private int awaitPollingInterval;
    @Value("${clean.workspace.after.deployment.enabled:true}")
    private boolean cleanWorkspaceAfterDeploymentEnabled;
    @Resource
    private ScriptsGitRepoManage scriptsGitRepoManage;

    /**
     * Create workspace directory for a deployment task.
     *
     * @param configWorkspaceDir config workspace directory.
     * @param orderId            id of the deployment order task.
     * @return absolute path of the taskWorkspace.
     */
    public String createWorkspaceForTask(String configWorkspaceDir, UUID orderId) {
        File baseWorkspace = new File(System.getProperty("java.io.tmpdir"), configWorkspaceDir);
        File taskWorkspace = new File(baseWorkspace, orderId.toString());
        if (!taskWorkspace.exists() && !taskWorkspace.mkdirs()) {
            throw new DeploymentScriptsCreationFailedException(
                    "Create workspace for task failed, File path not created: "
                            + taskWorkspace.getAbsolutePath());
        }
        return taskWorkspace.getAbsolutePath();
    }


    /**
     * Prepare the deployment script files for a deployment task.
     *
     * @param taskWorkspace workspace directory for the task.
     * @param deployment    deployment object containing the scripts to be used for deployment.
     * @param tfState       tf state file content.
     * @return list of files created for deployment.
     */
    public List<File> prepareDeploymentScripts(String taskWorkspace, Deployment deployment,
                                               String tfState) {
        File ws = new File(taskWorkspace);
        if (!ws.exists() && !ws.mkdirs()) {
            throw new DeploymentScriptsCreationFailedException(
                    "Create workspace for task failed, File path not created: " + taskWorkspace);
        }
        List<File> files = new ArrayList<>();
        if (Objects.nonNull(deployment.getDeployer())) {
            File scriptFile = createScriptFile(taskWorkspace, deployment.getDeployer());
            files.add(scriptFile);
            if (StringUtils.isNotBlank(tfState)) {
                File stateFile = createServiceStateFile(taskWorkspace, tfState);
                files.add(stateFile);
            }

        } else if (Objects.nonNull(deployment.getScriptsRepo())) {
            List<File> scriptFiles = scriptsGitRepoManage.checkoutScripts(taskWorkspace,
                    deployment.getScriptsRepo());
            files.addAll(scriptFiles);
            if (StringUtils.isNotBlank(tfState)) {
                String scriptPath = taskWorkspace + File.separator
                        + deployment.getScriptsRepo().getScriptsPath();
                File stateFile = createServiceStateFile(scriptPath, tfState);
                files.add(stateFile);
            }
        }
        return files;
    }


    /**
     * Reads the contents of the "terraform.tfstate" file in the workspace for the task.
     *
     * @return file contents as string.
     */
    public String getTaskTerraformState(String taskWorkspace) {
        String state = null;
        String path = new File(taskWorkspace, TF_STATE_FILE_NAME).getAbsolutePath();
        try {
            waitUntilFileIsNotLocked(path);
            File tfState = new File(path);
            if (tfState.exists()) {
                state = Files.readString(tfState.toPath());
            }
        } catch (IOException ex) {
            log.error("Read terraform state file failed.", ex);
        }
        return state;
    }

    /**
     * Reads the contents of the other generated file in the workspace for the task.
     *
     * @return Map fileName as key, contents as value.
     */
    public Map<String, String> getGeneratedFileContents(String taskWorkspace,
                                                        List<File> preparedFiles) {
        Map<String, String> fileContentMap = new HashMap<>();
        File workPath = new File(taskWorkspace);
        if (workPath.isDirectory() && workPath.exists()) {
            File[] files = workPath.listFiles();
            if (Objects.nonNull(files)) {
                Arrays.stream(files).forEach(file -> {
                    if (file.isFile() && !isExcludedFile(file.getName())
                            && !preparedFiles.contains(file)) {
                        String content = readFileContent(file);
                        fileContentMap.put(file.getName(), content);
                    }
                });
            }
        }
        return fileContentMap;
    }


    /**
     * Delete all files in the workspace for the task.
     *
     * @param taskWorkspace workspace directory for the task.
     */
    public void deleteTaskWorkspace(String taskWorkspace) {
        if (cleanWorkspaceAfterDeploymentEnabled) {
            Path path = Paths.get(taskWorkspace);
            try (Stream<Path> pathStream = Files.walk(path)) {
                pathStream.sorted(Comparator.reverseOrder()).map(Path::toFile)
                        .forEach(file -> {
                            if (!file.delete()) {
                                log.error("Failed to delete file: {}", file.getAbsolutePath());
                            }
                        });
            } catch (Exception e) {
                log.error("Delete workspace:{} for the task error.", taskWorkspace, e);
            }
        }
    }

    /**
     * Get the scripts location in the workspace for the deployment task.
     *
     * @param taskWorkspace workspace directory for the deployment task.
     * @param deployment    deployment object.
     * @return scripts location path in the workspace for the deployment task.
     */
    @Nullable
    public String getScriptsLocationInWorkspace(String taskWorkspace, Deployment deployment) {
        if (Objects.nonNull(deployment.getScriptsRepo())) {
            log.info("Deployment scripts are from git repo. Scripts path:{}",
                    deployment.getScriptsRepo().getScriptsPath());
            return taskWorkspace + File.separator + deployment.getScriptsRepo().getScriptsPath();
        }
        return taskWorkspace;
    }

    private File createScriptFile(String taskWorkspace, String scriptContent) {
        String scriptPath = taskWorkspace + File.separator + TF_SCRIPT_FILE_NAME;
        try (FileWriter scriptWriter = new FileWriter(scriptPath)) {
            scriptWriter.write(scriptContent);
            log.info("Created deployment script files successful");
            return new File(scriptPath);
        } catch (IOException ex) {
            log.error("Created deployment script files failed.", ex);
            throw new DeploymentScriptsCreationFailedException(
                    "Created deployment script files failed.");
        }
    }

    private File createServiceStateFile(String taskWorkspace, String tfStateContext) {
        String tfStateFileName = taskWorkspace + File.separator + TF_STATE_FILE_NAME;
        try (FileWriter scriptWriter = new FileWriter(tfStateFileName)) {
            scriptWriter.write(tfStateContext);
            log.info("Create service state file success.");
            return new File(tfStateFileName);
        } catch (IOException e) {
            log.error("Create service state file failed.", e);
            throw new DeploymentScriptsCreationFailedException("Create service state file failed.");
        }
    }

    private boolean isExcludedFile(String fileName) {
        String fileSuffix = fileName.substring(fileName.lastIndexOf("."));
        return EXCLUDED_FILE_SUFFIX_LIST.contains(fileSuffix);
    }

    private String readFileContent(File file) {
        try {
            return Files.readString(file.toPath());
        } catch (IOException e) {
            log.error("Read file content with name:{} error.", file.getName(), e);
        }
        return null;
    }


    /**
     * Method to await for the tfstate file to be unlocked.
     *
     * @param tfStateFilePath tfstate file path.
     */
    private void waitUntilFileIsNotLocked(String tfStateFilePath) {
        try {
            Awaitility.await()
                    .atMost(Duration.ofSeconds(awaitAtMost))
                    .pollInterval(Duration.ofSeconds(awaitPollingInterval))
                    .pollDelay(0, TimeUnit.SECONDS)
                    .until(() -> !isTfStateFileLocked(tfStateFilePath));
        } catch (ConditionTimeoutException e) {
            String errorMsg = String.format(
                    "Timeout waiting for file to be unlocked, %s", e.getMessage());
            log.info(errorMsg);
            throw new FileLockedException(errorMsg);
        }
    }

    private boolean isTfStateFileLocked(String tfStateFilePath) {
        try (RandomAccessFile file = new RandomAccessFile(tfStateFilePath, MODE);
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
