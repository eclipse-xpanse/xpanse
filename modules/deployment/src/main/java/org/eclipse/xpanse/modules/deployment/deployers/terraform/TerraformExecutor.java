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
import org.eclipse.xpanse.modules.deployment.deployers.terraform.resource.TfValidationResult;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.utils.SystemCmd;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.utils.SystemCmdResult;

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
     * @return true if initialization of terraform is successful. else false.
     */
    public boolean tfInit() {
        SystemCmdResult systemCmdResult = execute("terraform init");
        log.info(systemCmdResult.getCommandOutput());
        return systemCmdResult.isCommandSuccessful();
    }

    /**
     * Executes terraform plan command.
     *
     * @return true if terraform plan creation is successful. else false.
     */
    public boolean tfPlan() {
        return executeWithVariables(new StringBuilder("terraform plan -input=false "));
    }

    /**
     * Executes terraform apply command.
     *
     * @return true if changes are successfully applied. else false.
     */
    public boolean tfApply() {
        return executeWithVariables(
                new StringBuilder("terraform apply -auto-approve -input=false "));
    }

    /**
     * Executes terraform destroy command.
     *
     * @return true if all resources are successfully destroyed on the target infrastructure. else
     * false.
     */
    public boolean tfDestroy() {
        return executeWithVariables(
                new StringBuilder("terraform destroy -auto-approve -input=false "));
    }

    /**
     * Executes terraform commands with parameters.
     *
     * @return true if finished without exceptions, else false.
     */
    private boolean executeWithVariables(StringBuilder command) {
        for (Map.Entry<String, String> entry : this.variables.entrySet()) {
            if (Objects.nonNull(entry.getKey()) && Objects.nonNull(entry.getValue())) {
                command.append("-var=\"")
                        .append(entry.getKey())
                        .append("=")
                        .append(entry.getValue())
                        .append("\" ");
            }
        }
        SystemCmdResult systemCmdResult = execute(command.toString());
        log.info(systemCmdResult.getCommandOutput());
        return systemCmdResult.isCommandSuccessful();
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
        if (!tfInit()) {
            log.error("TFExecutor.tfInit failed.");
            throw new TerraformExecutorException("TFExecutor.tfInit failed.");
        }
        if (!tfPlan()) {
            log.error("TFExecutor.tfPlan failed.");
            throw new TerraformExecutorException("TFExecutor.tfPlan failed.");
        }
        if (!tfApply()) {
            log.error("TFExecutor.tfApply failed.");
            throw new TerraformExecutorException("TFExecutor.tfApply failed.");
        }
    }

    /**
     * Destroy resource of the service.
     */
    public void destroy() {
        if (!tfInit()) {
            log.error("TFExecutor.tfInit failed.");
            throw new TerraformExecutorException("TFExecutor.tfInit failed.");
        }
        if (!tfPlan()) {
            log.error("TFExecutor.tfPlan failed.");
            throw new TerraformExecutorException("TFExecutor.tfPlan failed.");
        }
        if (!tfDestroy()) {
            log.error("TFExecutor.tfDestroy failed.");
            throw new TerraformExecutorException("TFExecutor.tfDestroy failed.");
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
    public TfValidationResult tfValidate() {
        if (!tfInit()) {
            log.error("TFExecutor.tfInit failed.");
            throw new TerraformExecutorException("TFExecutor.tfInit failed.");
        }
        SystemCmdResult systemCmdResult = execute("terraform validate -json");
        log.info(systemCmdResult.getCommandOutput());
        try {
            return new ObjectMapper().readValue(systemCmdResult.getCommandOutput(),
                    TfValidationResult.class);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Serialising string to object failed.", ex);
        }
    }

}
