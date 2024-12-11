/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformlocal;

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
import org.eclipse.xpanse.modules.deployment.deployers.terraform.exceptions.TerraformExecutorException;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeploymentScriptValidationResult;

/** An executor for terraform. */
@Slf4j
public class TerraformLocalExecutor {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Getter private final String executorPath;
    @Getter private final String taskWorkspace;
    private final Map<String, String> env;
    private final Map<String, Object> variables;

    /**
     * Constructor for terraformExecutor.
     *
     * @param executorPath path of the terraform executor.
     * @param env environment for the terraform command line.
     * @param variables variables for the terraform command line.
     * @param taskWorkspace taskWorkspace for the terraform command line.
     */
    TerraformLocalExecutor(
            String executorPath,
            Map<String, String> env,
            Map<String, Object> variables,
            String taskWorkspace) {
        this.executorPath = executorPath;
        this.env = env;
        this.variables = variables;
        this.taskWorkspace = taskWorkspace;
        log.info(
                "Created TerraformLocalExecutor with executorPath: {} and taskWorkspace: {}",
                executorPath,
                taskWorkspace);
    }

    /**
     * Executes terraform init command.
     *
     * @return Returns result of SystemCmd executed.
     */
    public SystemCmdResult tfInit() {
        return execute(this.executorPath + " init -no-color");
    }

    /**
     * Executes terraform plan command.
     *
     * @return Returns result of SystemCmd executed.
     */
    public SystemCmdResult tfPlan() {
        return executeWithVariables(
                new StringBuilder(this.executorPath + " plan -input=false -no-color "));
    }

    /**
     * Executes terraform plan command and output.
     *
     * @return Returns result of SystemCmd executed.
     */
    public SystemCmdResult tfPlanWithOutput() {
        return executeWithVariables(
                new StringBuilder(
                        this.executorPath + " plan -input=false -no-color --out tfplan.binary"));
    }

    /**
     * Executes terraform apply command.
     *
     * @return Returns result of SystemCmd executed.
     */
    public SystemCmdResult tfApply() {
        return executeWithVariables(
                new StringBuilder(
                        this.executorPath + " apply -auto-approve -input=false -no-color "));
    }

    /**
     * Executes terraform destroy command.
     *
     * @return Returns result of SystemCmd executed.
     */
    public SystemCmdResult tfDestroy() {
        return executeWithVariables(
                new StringBuilder(
                        this.executorPath + " destroy -auto-approve -input=false -no-color "));
    }

    /**
     * Executes terraform commands with parameters.
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
            File variablesFile = new File(taskWorkspace, TF_VARS_FILE_NAME);
            log.info("cleaning up variables file");
            Files.deleteIfExists(variablesFile.toPath());
        } catch (IOException ioException) {
            log.error("Cleanup of variables file failed", ioException);
        }
    }

    /**
     * Executes terraform commands.
     *
     * @return SystemCmdResult
     */
    private SystemCmdResult execute(String cmd) {
        SystemCmd systemCmd = new SystemCmd();
        systemCmd.setEnv(env);
        systemCmd.setWorkDir(taskWorkspace);
        return systemCmd.execute(cmd);
    }

    /** Deploy source by terraform. */
    public void deploy() {
        SystemCmdResult initResult = tfInit();
        if (!initResult.isCommandSuccessful()) {
            log.error("TFExecutor.tfInit failed.");
            throw new TerraformExecutorException(
                    "TFExecutor.tfInit failed.", initResult.getCommandStdError());
        }
        SystemCmdResult planResult = tfPlan();
        if (!planResult.isCommandSuccessful()) {
            log.error("TFExecutor.tfPlan failed.");
            throw new TerraformExecutorException(
                    "TFExecutor.tfPlan failed.", planResult.getCommandStdError());
        }
        SystemCmdResult applyResult = tfApply();
        if (!applyResult.isCommandSuccessful()) {
            log.error("TFExecutor.tfApply failed.");
            throw new TerraformExecutorException(
                    "TFExecutor.tfApply failed.", applyResult.getCommandStdError());
        }
    }

    /** Destroy resource of the service. */
    public void destroy() {
        SystemCmdResult initResult = tfInit();
        if (!initResult.isCommandSuccessful()) {
            log.error("TFExecutor.tfInit failed.");
            throw new TerraformExecutorException(
                    "TFExecutor.tfInit failed.", initResult.getCommandStdError());
        }
        SystemCmdResult planResult = tfPlan();
        if (!planResult.isCommandSuccessful()) {
            log.error("TFExecutor.tfPlan failed.");
            throw new TerraformExecutorException(
                    "TFExecutor.tfPlan failed.", planResult.getCommandStdError());
        }
        SystemCmdResult destroyResult = tfDestroy();
        if (!destroyResult.isCommandSuccessful()) {
            log.error("TFExecutor.tfDestroy failed.");
            throw new TerraformExecutorException(
                    "TFExecutor.tfDestroy failed.", destroyResult.getCommandStdError());
        }
    }

    /** Method to execute terraform plan and get the plan as a json string. */
    public String getTerraformPlanAsJson() {
        SystemCmdResult initResult = tfInit();
        if (!initResult.isCommandSuccessful()) {
            log.error("TFExecutor.tfInit failed.");
            throw new TerraformExecutorException(
                    "TFExecutor.tfInit failed.", initResult.getCommandStdError());
        }
        SystemCmdResult tfPlanResult = tfPlanWithOutput();
        if (!tfPlanResult.isCommandSuccessful()) {
            log.error("TFExecutor.tfPlan failed.");
            throw new TerraformExecutorException(
                    "TFExecutor.tfPlan failed.", tfPlanResult.getCommandStdError());
        }
        SystemCmdResult planJsonResult = execute(this.executorPath + " show -json tfplan.binary");
        if (!planJsonResult.isCommandSuccessful()) {
            log.error("Reading Terraform plan as JSON failed.");
            throw new TerraformExecutorException(
                    "Reading Terraform plan as JSON failed.", planJsonResult.getCommandStdError());
        }
        return planJsonResult.getCommandStdOutput();
    }

    /**
     * Executes terraform validate command.
     *
     * @return TfValidationResult.
     */
    public DeploymentScriptValidationResult tfValidate() {
        SystemCmdResult initResult = tfInit();
        if (!initResult.isCommandSuccessful()) {
            log.error("TFExecutor.tfInit failed.");
            throw new TerraformExecutorException(
                    "TFExecutor.tfInit failed.", initResult.getCommandStdError());
        }
        SystemCmdResult systemCmdResult = execute(this.executorPath + " validate -json -no-color");
        try {
            return new ObjectMapper()
                    .readValue(
                            systemCmdResult.getCommandStdOutput(),
                            DeploymentScriptValidationResult.class);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Serialising string to object failed.", ex);
        }
    }
}
