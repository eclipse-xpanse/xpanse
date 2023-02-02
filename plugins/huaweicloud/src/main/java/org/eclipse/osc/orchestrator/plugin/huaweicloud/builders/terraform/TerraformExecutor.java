/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.osc.orchestrator.plugin.huaweicloud.builders.terraform;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.osc.orchestrator.plugin.huaweicloud.builders.utils.SystemCmd;
import org.eclipse.osc.orchestrator.plugin.huaweicloud.exceptions.TerraformExecutorException;

/**
 * Class to encapsulate all Terraform executions.
 */
@Slf4j
public class TerraformExecutor {

    private final Map<String, String> env;
    private String tfPath;
    private String workPath;

    public TerraformExecutor(Map<String, String> env) {
        this.env = env;
    }

    /**
     * Creates terraform workspace.
     *
     * @param name name of the folder in terraform workspace.
     */
    public void createWorkspace(String name) {
        File ws =
                new File("terraform_ws" + FileSystems.getDefault().getSeparator() + name);

        if (!ws.exists() && !ws.mkdirs()) {
            throw new TerraformExecutorException(
                    "Create workspace for TFExecutor failed, File path not created: "
                            + ws.getAbsolutePath());
        }

        tfPath = ws.getAbsolutePath() + FileSystems.getDefault().getSeparator() + "resources.tf";
        String verPath =
                ws.getAbsolutePath() + FileSystems.getDefault().getSeparator() + "version.tf";
        workPath = ws.getAbsolutePath();
        log.info("Terraform working directory is " + workPath);

        try {
            try (FileWriter versionFile = new FileWriter(verPath)) {
                versionFile.write(""
                        + "terraform {\n"
                        + "  required_providers {\n"
                        + "    huaweicloud = {\n"
                        + "      source = \"huaweicloud/huaweicloud\"\n"
                        + "      version = \">= 1.20.0\"\n"
                        + "    }\n"
                        + "  }\n"
                        + "}");
            }
        } catch (IOException ex) {
            log.error("Create terraform version file failed.", ex);
            throw new TerraformExecutorException("Create terraform version file failed.", ex);
        }
    }

    /**
     * Creates terraform script on the file system.
     *
     * @param script content of the script file as string.
     */
    public void createTerraformScript(String script) {
        try {
            try (FileWriter tfFile = new FileWriter(tfPath)) {
                tfFile.write(script);
            }
        } catch (IOException ex) {
            throw new TerraformExecutorException("TFExecutor createTFScript failed.", ex);
        }
    }

    private boolean execute(String cmd, StringBuilder stdOut) {
        log.info("Will executing cmd: " + String.join(" ", cmd));
        SystemCmd systemCmd = new SystemCmd();
        systemCmd.setEnv(this.env);
        systemCmd.setWorkDir(workPath);
        return systemCmd.execute(cmd, stdOut);
    }

    /**
     * Executes terraform init command.
     *
     * @return true if initialization of terraform is successful. else false.
     */
    public boolean tfInit() {
        StringBuilder out = new StringBuilder();
        boolean exeRet = execute("terraform init", out);
        log.info(out.toString());
        return exeRet;
    }

    /**
     * Executes terraform plan command.
     *
     * @return true if terraform plan creation is successful. else false.
     */
    public boolean tfPlan() {
        // TODO: Dynamic variables need to be supported.
        StringBuilder out = new StringBuilder();
        boolean exeRet = execute("terraform plan", out);
        log.info(out.toString());
        return exeRet;
    }

    /**
     * Executes terraform apply command.
     *
     * @return true if changes are successfully applied. else false.
     */
    public boolean tfApply() {
        // TODO: Dynamic variables need to be supported.
        StringBuilder out = new StringBuilder();
        boolean exeRet = execute("terraform apply -auto-approve", out);
        log.info(out.toString());
        return exeRet;
    }

    /**
     * Executes terraform destroy command.
     *
     * @return true if all resources are successfully destroyed on the target infrastructure.
     * else false.
     */
    public boolean tfDestroy() {
        // TODO: Dynamic variables need to be supported.
        StringBuilder out = new StringBuilder();
        boolean exeRet = execute("terraform destroy -auto-approve", out);
        log.info(out.toString());
        return exeRet;
    }

    /**
     * Reads the contents of the "terraform.tfstate" file from the terraform workspace
     *
     * @return file contents as string.
     */
    public String getTerraformState() {
        File tfState = new File(workPath + FileSystems.getDefault().getSeparator()
                + "terraform.tfstate");
        if (!tfState.exists()) {
            log.info("Terraform state file not exists.");
            return "";
        }

        try {
            return Files.readString(tfState.toPath());
        } catch (IOException ex) {
            throw new TerraformExecutorException("Read state file failed.", ex);
        }
    }
}
