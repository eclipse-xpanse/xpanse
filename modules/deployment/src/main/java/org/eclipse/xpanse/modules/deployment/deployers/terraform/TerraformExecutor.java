/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.exceptions.TerraformExecutorException;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.utils.SystemCmd;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.utils.SystemCmdResult;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployValidationResult;

/**
 * An executor for terraform.
 */
@Slf4j
public class TerraformExecutor {

    private final Map<String, String> env;
    private final Map<String, String> variables;
    private final String workspace;

    /**
     * Constructor for terraformExecutor.
     *
     * @param env       environment for the terraform command line.
     * @param variables variables for the terraform command line.
     * @param workspace workspace for the terraform command line.
     */
    TerraformExecutor(Map<String, String> env, Map<String, String> variables,
                      String workspace) {
        this.env = env;
        this.variables = variables;
        this.workspace = workspace;
    }

    /**
     * Executes terraform init command.
     *
     * @return Returns result of SystemCmd executes.
     */
    public SystemCmdResult tfInit() {
        return execute("terraform init -no-color");
    }

    /**
     * Executes terraform plan command.
     *
     * @return Returns result of SystemCmd executes.
     */
    public SystemCmdResult tfPlan() {
        return executeWithVariables(new StringBuilder("terraform plan -input=false -no-color "));
    }

    /**
     * Executes terraform apply command.
     *
     * @return Returns result of SystemCmd executes.
     */
    public SystemCmdResult tfApply() {
        return executeWithVariables(
                new StringBuilder("terraform apply -auto-approve -input=false -no-color "));
    }

    /**
     * Executes terraform destroy command.
     *
     * @return Returns result of SystemCmd executes.
     */
    public SystemCmdResult tfDestroy() {
        return executeWithVariables(
                new StringBuilder("terraform destroy -auto-approve -input=false -no-color "));
    }

    /**
     * Executes terraform commands with parameters.
     *
     * @return Returns result of SystemCmd executes.
     */
    private SystemCmdResult executeWithVariables(StringBuilder command) {
        for (Map.Entry<String, String> entry : this.variables.entrySet()) {
            if (Objects.nonNull(entry.getKey()) && Objects.nonNull(entry.getValue())) {
                command.append("-var=")
                        .append(entry.getKey())
                        .append("=")
                        .append(entry.getValue())
                        .append(" ");
            }
        }
        return execute(command.toString());
    }

    /**
     * Executes terraform commands.
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
     * Deploy source by terraform.
     */
    public void deploy() {
        SystemCmdResult initResult = tfInit();
        if (!initResult.isCommandSuccessful()) {
            log.error("TFExecutor.tfInit failed.");
            throw new TerraformExecutorException("TFExecutor.tfInit failed.",
                    initResult.getCommandStdError());
        }
        SystemCmdResult planResult = tfPlan();
        if (!planResult.isCommandSuccessful()) {
            log.error("TFExecutor.tfPlan failed.");
            throw new TerraformExecutorException("TFExecutor.tfPlan failed.",
                    planResult.getCommandStdError());
        }
        SystemCmdResult applyResult = tfApply();
        if (!applyResult.isCommandSuccessful()) {
            log.error("TFExecutor.tfApply failed.");
            throw new TerraformExecutorException("TFExecutor.tfApply failed.",
                    applyResult.getCommandStdError());
        }
    }

    /**
     * Destroy resource of the service.
     */
    public void destroy() {
        SystemCmdResult initResult = tfInit();
        if (!initResult.isCommandSuccessful()) {
            log.error("TFExecutor.tfInit failed.");
            throw new TerraformExecutorException("TFExecutor.tfInit failed.",
                    initResult.getCommandStdError());
        }
        SystemCmdResult planResult = tfPlan();
        if (!planResult.isCommandSuccessful()) {
            log.error("TFExecutor.tfPlan failed.");
            throw new TerraformExecutorException("TFExecutor.tfPlan failed.",
                    planResult.getCommandStdError());
        }
        SystemCmdResult destroyResult = tfDestroy();
        if (!destroyResult.isCommandSuccessful()) {
            log.error("TFExecutor.tfDestroy failed.");
            throw new TerraformExecutorException("TFExecutor.tfDestroy failed.",
                    destroyResult.getCommandStdError());
        }
    }

    /**
     * Reads the contents of the "terraform.tfstate" file from the terraform workspace.
     *
     * @return file contents as string.
     */
    public String getTerraformState() {
        File tfState = new File(workspace + File.separator + "terraform.tfstate");
        if (!tfState.exists()) {
            log.info("Terraform state file not exists.");
            return null;
        }
        try {
            return Files.readString(tfState.toPath());
        } catch (IOException ex) {
            throw new TerraformExecutorException("Read state file failed.", ex);
        }
    }

    /**
     * Executes terraform validate command.
     *
     * @return TfValidationResult.
     */
    public DeployValidationResult tfValidate() {
        SystemCmdResult initResult = tfInit();
        if (!initResult.isCommandSuccessful()) {
            log.error("TFExecutor.tfInit failed.");
            throw new TerraformExecutorException("TFExecutor.tfInit failed.",
                    initResult.getCommandStdError());
        }
        SystemCmdResult systemCmdResult = execute("terraform validate -json -no-color");
        try {
            return new ObjectMapper().readValue(systemCmdResult.getCommandStdOutput(),
                    DeployValidationResult.class);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Serialising string to object failed.", ex);
        }
    }


}
