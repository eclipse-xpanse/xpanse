/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.deployers.opentofu.opentofulocal;

import static org.eclipse.xpanse.modules.deployment.utils.DeploymentScriptsHelper.TF_VARS_FILE_NAME;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.common.systemcmd.SystemCmd;
import org.eclipse.xpanse.common.systemcmd.SystemCmdResult;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.exceptions.OpenTofuExecutorException;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeploymentScriptValidationResult;

/** An executor for OpenTofu. */
@Slf4j
public class OpenTofuLocalExecutor {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Getter private final String executorPath;
    @Getter private final String taskWorkspace;
    private final Map<String, String> env;
    private final Map<String, Object> variables;

    /**
     * Constructor for openTofuExecutor.
     *
     * @param executorPath path of the open tofu executor.
     * @param env environment for the open tofu command line.
     * @param variables variables for the open tofu command line.
     * @param taskWorkspace workspace with scripts for the open tofu command line.
     */
    OpenTofuLocalExecutor(
            String executorPath,
            Map<String, String> env,
            Map<String, Object> variables,
            String taskWorkspace) {
        this.executorPath = executorPath;
        this.env = env;
        this.variables = variables;
        this.taskWorkspace = taskWorkspace;
        log.info(
                "Created OpenTofuLocalExecutor with executorPath: {} and taskWorkspace: {}",
                executorPath,
                taskWorkspace);
    }

    /**
     * Executes open tofu init command.
     *
     * @return Returns result of SystemCmd executed.
     */
    public SystemCmdResult tfInit() {
        return execute(this.executorPath + " init -no-color");
    }

    /**
     * Executes open tofu plan command.
     *
     * @return Returns result of SystemCmd executed.
     */
    public SystemCmdResult tfPlan() {
        return executeWithVariables(
                new StringBuilder(this.executorPath + " plan -input=false -no-color "));
    }

    /**
     * Executes open tofu plan command and output.
     *
     * @return Returns result of SystemCmd executed.
     */
    public SystemCmdResult tfPlanWithOutput() {
        return executeWithVariables(
                new StringBuilder(
                        this.executorPath + " plan -input=false -no-color --out tfplan.binary"));
    }

    /**
     * Executes open tofu apply command.
     *
     * @return Returns result of SystemCmd executed.
     */
    public SystemCmdResult tfApply() {
        return executeWithVariables(
                new StringBuilder(
                        this.executorPath + " apply -auto-approve -input=false -no-color "));
    }

    /**
     * Executes open tofu destroy command.
     *
     * @return Returns result of SystemCmd executed.
     */
    public SystemCmdResult tfDestroy() {
        return executeWithVariables(
                new StringBuilder(
                        this.executorPath + " destroy -auto-approve -input=false -no-color "));
    }

    /**
     * Executes open tofu commands with parameters.
     *
     * @return Returns result of SystemCmd executed.
     */
    private SystemCmdResult executeWithVariables(StringBuilder command) {
        createVariablesFile(this.taskWorkspace, variables);
        command.append(" -var-file=");
        command.append(TF_VARS_FILE_NAME);
        SystemCmdResult systemCmdResult = execute(command.toString());
        cleanUpVariablesFile(this.taskWorkspace);
        return systemCmdResult;
    }

    private void createVariablesFile(String taskWorkspace, Map<String, Object> variables) {
        try {
            File variablesFile = new File(taskWorkspace, TF_VARS_FILE_NAME);
            log.info("creating variables file");
            OBJECT_MAPPER.writeValue(variablesFile, variables);
        } catch (IOException ioException) {
            throw new OpenTofuExecutorException("Creating variables file failed", ioException);
        }
    }

    private void cleanUpVariablesFile(String taskWorkspace) {
        try {
            File file = new File(taskWorkspace, TF_VARS_FILE_NAME);
            log.info("cleaning up variables file");
            Files.deleteIfExists(file.toPath());
        } catch (IOException ioException) {
            log.error("Cleanup of variables file failed", ioException);
        }
    }

    /**
     * Executes open tofu commands.
     *
     * @return SystemCmdResult
     */
    private SystemCmdResult execute(String cmd) {
        SystemCmd systemCmd = new SystemCmd();
        systemCmd.setEnv(env);
        systemCmd.setWorkDir(taskWorkspace);
        return systemCmd.execute(cmd);
    }

    /** Deploy source by OpenTofu. */
    public void deploy() {
        SystemCmdResult initResult = tfInit();
        if (!initResult.isCommandSuccessful()) {
            log.error("OpenTofuExecutor.tfInit failed.");
            throw new OpenTofuExecutorException(
                    "OpenTofuExecutor.tfInit failed.", initResult.getCommandStdError());
        }
        SystemCmdResult planResult = tfPlan();
        if (!planResult.isCommandSuccessful()) {
            log.error("OpenTofuExecutor.tfPlan failed.");
            throw new OpenTofuExecutorException(
                    "OpenTofuExecutor.tfPlan failed.", planResult.getCommandStdError());
        }
        SystemCmdResult applyResult = tfApply();
        if (!applyResult.isCommandSuccessful()) {
            log.error("OpenTofuExecutor.tfApply failed.");
            throw new OpenTofuExecutorException(
                    "OpenTofuExecutor.tfApply failed.", applyResult.getCommandStdError());
        }
    }

    /** Destroy resource of the service. */
    public void destroy() {
        SystemCmdResult initResult = tfInit();
        if (!initResult.isCommandSuccessful()) {
            log.error("OpenTofuExecutor.tfInit failed.");
            throw new OpenTofuExecutorException(
                    "OpenTofuExecutor.tfInit failed.", initResult.getCommandStdError());
        }
        SystemCmdResult planResult = tfPlan();
        if (!planResult.isCommandSuccessful()) {
            log.error("OpenTofuExecutor.tfPlan failed.");
            throw new OpenTofuExecutorException(
                    "OpenTofuExecutor.tfPlan failed.", planResult.getCommandStdError());
        }
        SystemCmdResult destroyResult = tfDestroy();
        if (!destroyResult.isCommandSuccessful()) {
            log.error("OpenTofuExecutor.tfDestroy failed.");
            throw new OpenTofuExecutorException(
                    "OpenTofuExecutor.tfDestroy failed.", destroyResult.getCommandStdError());
        }
    }

    /** Method to execute open tofu plan and get the plan as a json string. */
    public String getOpenTofuPlanAsJson() {
        SystemCmdResult initResult = tfInit();
        if (!initResult.isCommandSuccessful()) {
            log.error("OpenTofuExecutor.tfInit failed.");
            throw new OpenTofuExecutorException(
                    "OpenTofuExecutor.tfInit failed.", initResult.getCommandStdError());
        }
        SystemCmdResult tfPlanResult = tfPlanWithOutput();
        if (!tfPlanResult.isCommandSuccessful()) {
            log.error("OpenTofuExecutor.tfPlan failed.");
            throw new OpenTofuExecutorException(
                    "OpenTofuExecutor.tfPlan failed.", tfPlanResult.getCommandStdError());
        }
        SystemCmdResult planJsonResult = execute(this.executorPath + " show -json tfplan.binary");
        if (!planJsonResult.isCommandSuccessful()) {
            log.error("Reading OpenTofu plan as JSON failed.");
            throw new OpenTofuExecutorException(
                    "Reading OpenTofu plan as JSON failed.", planJsonResult.getCommandStdError());
        }
        return planJsonResult.getCommandStdOutput();
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
            throw new OpenTofuExecutorException(
                    "OpenTofuExecutor.tfInit failed.", initResult.getCommandStdError());
        }
        SystemCmdResult validateResult = execute(this.executorPath + " validate -json -no-color");

        if (!validateResult.isCommandSuccessful()) {
            log.error("OpenTofuExecutor get validate json failed.");
            throw new OpenTofuExecutorException(
                    "OpenTofuExecutor get validate json failed.",
                    validateResult.getCommandStdError());
        }
        try {
            String commandStdOutput = validateResult.getCommandStdOutput();
            String cleanedJson = commandStdOutput.substring(commandStdOutput.indexOf('{'));
            return new ObjectMapper()
                    .readValue(cleanedJson, DeploymentScriptValidationResult.class);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException(
                    "Serialising command output to validate result object failed.", ex);
        }
    }
}
