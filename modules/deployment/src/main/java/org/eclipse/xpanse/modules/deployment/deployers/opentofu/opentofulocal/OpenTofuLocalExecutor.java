/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.deployers.opentofu.opentofulocal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.common.systemcmd.SystemCmd;
import org.eclipse.xpanse.common.systemcmd.SystemCmdResult;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.exceptions.OpenTofuExecutorException;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeploymentScriptValidationResult;

/**
 * An executor for OpenTofu.
 */
@Slf4j
public class OpenTofuLocalExecutor {

    private static final String VARS_FILE_NAME = "variables.tfvars.json";
    private static final String STATE_FILE_NAME = "terraform.tfstate";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final List<String> EXCLUDED_FILE_SUFFIX_LIST =
            Arrays.asList(".tf", ".tfstate", ".binary", ".hcl");

    static {
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    private final Map<String, String> env;
    private final Map<String, Object> variables;
    private final String workspace;

    /**
     * Constructor for openTofuExecutor.
     *
     * @param env       environment for the open tofu command line.
     * @param variables variables for the open tofu command line.
     * @param workspace workspace for the open tofu command line.
     */
    OpenTofuLocalExecutor(Map<String, String> env, 
                           Map<String, Object> variables,
                           String workspace,
                           @Nullable String subDirectory) {
        this.env = env;
        this.variables = variables;
        this.workspace =
                Objects.nonNull(subDirectory)
                        ? workspace + File.separator + subDirectory
                        : workspace;
    }

    /**
     * Executes open tofu init command.
     *
     * @return Returns result of SystemCmd executed.
     */
    public SystemCmdResult tfInit() {
        return execute("tofu init -no-color");
    }

    /**
     * Executes open tofu plan command.
     *
     * @return Returns result of SystemCmd executed.
     */
    public SystemCmdResult tfPlan() {
        return executeWithVariables(new StringBuilder("tofu plan -input=false -no-color "));
    }

    /**
     * Executes open tofu plan command and output.
     *
     * @return Returns result of SystemCmd executed.
     */
    public SystemCmdResult tfPlanWithOutput() {
        return executeWithVariables(new StringBuilder(
                "tofu plan -input=false -no-color --out tfplan.binary"));
    }

    /**
     * Executes open tofu apply command.
     *
     * @return Returns result of SystemCmd executed.
     */
    public SystemCmdResult tfApply() {
        return executeWithVariables(
                new StringBuilder("tofu apply -auto-approve -input=false -no-color "));
    }

    /**
     * Executes open tofu destroy command.
     *
     * @return Returns result of SystemCmd executed.
     */
    public SystemCmdResult tfDestroy() {
        return executeWithVariables(
                new StringBuilder("tofu destroy -auto-approve -input=false -no-color "));
    }

    /**
     * Executes open tofu commands with parameters.
     *
     * @return Returns result of SystemCmd executed.
     */
    private SystemCmdResult executeWithVariables(StringBuilder command) {
        createVariablesFile();
        command.append(" -var-file=");
        command.append(VARS_FILE_NAME);
        SystemCmdResult systemCmdResult = execute(command.toString());
        cleanUpVariablesFile();
        return systemCmdResult;
    }

    /**
     * Executes open tofu commands.
     *
     * @return SystemCmdResult
     */
    private SystemCmdResult execute(String cmd) {
        SystemCmd systemCmd = new SystemCmd();
        systemCmd.setEnv(env);
        systemCmd.setWorkDir(workspace);
        return systemCmd.execute(cmd);
    }

    /**
     * Deploy source by OpenTofu.
     */
    public void deploy() {
        SystemCmdResult initResult = tfInit();
        if (!initResult.isCommandSuccessful()) {
            log.error("OpenTofuExecutor.tfInit failed.");
            throw new OpenTofuExecutorException("OpenTofuExecutor.tfInit failed.",
                    initResult.getCommandStdError());
        }
        SystemCmdResult planResult = tfPlan();
        if (!planResult.isCommandSuccessful()) {
            log.error("OpenTofuExecutor.tfPlan failed.");
            throw new OpenTofuExecutorException("OpenTofuExecutor.tfPlan failed.",
                    planResult.getCommandStdError());
        }
        SystemCmdResult applyResult = tfApply();
        if (!applyResult.isCommandSuccessful()) {
            log.error("OpenTofuExecutor.tfApply failed.");
            throw new OpenTofuExecutorException("OpenTofuExecutor.tfApply failed.",
                    applyResult.getCommandStdError());
        }
    }

    /**
     * Destroy resource of the service.
     */
    public void destroy() {
        SystemCmdResult initResult = tfInit();
        if (!initResult.isCommandSuccessful()) {
            log.error("OpenTofuExecutor.tfInit failed.");
            throw new OpenTofuExecutorException("OpenTofuExecutor.tfInit failed.",
                    initResult.getCommandStdError());
        }
        SystemCmdResult planResult = tfPlan();
        if (!planResult.isCommandSuccessful()) {
            log.error("OpenTofuExecutor.tfPlan failed.");
            throw new OpenTofuExecutorException("OpenTofuExecutor.tfPlan failed.",
                    planResult.getCommandStdError());
        }
        SystemCmdResult destroyResult = tfDestroy();
        if (!destroyResult.isCommandSuccessful()) {
            log.error("OpenTofuExecutor.tfDestroy failed.");
            throw new OpenTofuExecutorException("OpenTofuExecutor.tfDestroy failed.",
                    destroyResult.getCommandStdError());
        }
    }

    /**
     * Reads the contents of the "terraform.tfstate" file from the OpenTofu workspace.
     *
     * @return file contents as string.
     */
    public String getTerraformState() {
        String state = null;
        try {
            File tfState = new File(workspace + File.separator + STATE_FILE_NAME);
            if (tfState.exists()) {
                state = Files.readString(tfState.toPath());
            }
        } catch (IOException ex) {
            log.error("OpenTofuExecutor read state file failed.", ex);
        }
        return state;
    }

    /**
     * Reads the contents of the other important file from the OpenTofu workspace.
     *
     * @return Map fileName as key, contents as value.
     */
    public Map<String, String> getImportantFilesContent() {
        Map<String, String> fileContentMap = new HashMap<>();
        File workPath = new File(workspace);
        if (workPath.isDirectory() && workPath.exists()) {
            File[] files = workPath.listFiles();
            if (Objects.nonNull(files)) {
                List<File> importantFiles = Arrays.stream(files)
                        .filter(file -> file.isFile() && !isExcludedFile(file.getName())).toList();
                for (File importantFile : importantFiles) {
                    try {
                        String content = readFile(importantFile);
                        fileContentMap.put(importantFile.getName(), content);
                    } catch (IOException e) {
                        log.error("Read content of file with name:{} error.",
                                importantFile.getName(), e);
                    }
                }
            }
        }
        return fileContentMap;
    }

    /**
     * Method to execute open tofu plan and get the plan as a json string.
     */
    public String getOpenTofuPlanAsJson() {
        SystemCmdResult initResult = tfInit();
        if (!initResult.isCommandSuccessful()) {
            log.error("OpenTofuExecutor.tfInit failed.");
            throw new OpenTofuExecutorException("OpenTofuExecutor.tfInit failed.",
                    initResult.getCommandStdError());
        }
        SystemCmdResult tfPlanResult = tfPlanWithOutput();
        if (!tfPlanResult.isCommandSuccessful()) {
            log.error("OpenTofuExecutor.tfPlan failed.");
            throw new OpenTofuExecutorException("OpenTofuExecutor.tfPlan failed.",
                    tfPlanResult.getCommandStdError());
        }
        SystemCmdResult planJsonResult = execute("tofu show -json tfplan.binary");
        if (!planJsonResult.isCommandSuccessful()) {
            log.error("Reading OpenTofu plan as JSON failed.");
            throw new OpenTofuExecutorException("Reading OpenTofu plan as JSON failed.",
                    planJsonResult.getCommandStdError());
        }
        return planJsonResult.getCommandStdOutput();
    }

    private boolean isExcludedFile(String fileName) {
        String fileSuffix = fileName.substring(fileName.lastIndexOf("."));
        return EXCLUDED_FILE_SUFFIX_LIST.contains(fileSuffix);
    }

    private String readFile(File file) throws IOException {
        return Files.readString(file.toPath());
    }


    /**
     * Executes open tofu validate command.
     *
     * @return TfValidationResult.
     */
    public DeploymentScriptValidationResult tfValidate() {
        SystemCmdResult initResult = tfInit();
        if (!initResult.isCommandSuccessful()) {
            log.error("OpenTofuExecutor.tfInit failed.");
            throw new OpenTofuExecutorException("OpenTofuExecutor.tfInit failed.",
                    initResult.getCommandStdError());
        }
        SystemCmdResult systemCmdResult = execute("tofu validate -json -no-color");
        try {
            return new ObjectMapper().readValue(systemCmdResult.getCommandStdOutput(),
                    DeploymentScriptValidationResult.class);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Serialising string to object failed.", ex);
        }
    }

    private void createVariablesFile() {
        try {
            log.info("creating variables file");
            OBJECT_MAPPER.writeValue(new File(getVariablesFilePath()), variables);
        } catch (IOException ioException) {
            throw new OpenTofuExecutorException("Creating variables file failed", ioException);
        }
    }

    private void cleanUpVariablesFile() {
        File file = new File(getVariablesFilePath());
        try {
            log.info("cleaning up variables file");
            Files.deleteIfExists(file.toPath());
        } catch (IOException ioException) {
            log.error("Cleanup of variables file failed", ioException);
        }
    }

    private String getVariablesFilePath() {
        return this.workspace + File.separator + VARS_FILE_NAME;
    }

}
