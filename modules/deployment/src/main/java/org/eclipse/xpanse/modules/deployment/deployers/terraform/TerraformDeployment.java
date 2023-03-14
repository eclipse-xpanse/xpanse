/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.deployment.Deployment;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.exceptions.TerraformExecutorException;
import org.eclipse.xpanse.modules.models.enums.Csp;
import org.eclipse.xpanse.modules.models.enums.DeployVariableKind;
import org.eclipse.xpanse.modules.models.enums.DeployerKind;
import org.eclipse.xpanse.modules.models.enums.TerraformExecState;
import org.eclipse.xpanse.modules.models.resource.DeployVariable;
import org.eclipse.xpanse.modules.models.resource.Flavor;
import org.eclipse.xpanse.modules.models.service.DeployResult;
import org.springframework.stereotype.Component;

/**
 * Implementation of th deployment with terraform.
 */
@Slf4j
@Component
public class TerraformDeployment implements Deployment {

    public static final String VERSION_FILE_NAME = "version.tf";
    public static final String SCRIPT_FILE_NAME = "resources.tf";

    private static final String WORKSPACE = "xpanse_deploy_ws";

    /**
     * Deploy the DeployTask.
     *
     * @param task the task for the deployment.
     */
    @Override
    public DeployResult deploy(DeployTask task) {
        String workspace = getWorkspacePath(task.getId().toString());
        // Create the workspace.
        buildWorkspace(workspace);
        createScriptFile(task.getCreateRequest().getCsp(), workspace,
                task.getOcl().getDeployment().getDeployer());
        // Execute the terraform command.
        TerraformExecutor executor =
                new TerraformExecutor(getEnv(task), getVariables(task), workspace);
        executor.deploy();
        String tfState = executor.getTerraformState();

        DeployResult deployResult = new DeployResult();
        if (StringUtils.isBlank(tfState)) {
            deployResult.setState(TerraformExecState.DEPLOY_FAILED);
        } else {
            deployResult.setState(TerraformExecState.DEPLOY_SUCCESS);
            deployResult.getProperty().put("stateFile", tfState);
        }

        if (task.getDeployResourceHandler() != null) {
            task.getDeployResourceHandler().handler(deployResult);
        }
        return deployResult;
    }


    @Override
    public DeployResult destroy(DeployTask task) {
        String workspace = getWorkspacePath(task.getId().toString());
        TerraformExecutor executor =
                new TerraformExecutor(getEnv(task), getVariables(task), workspace);
        DeployResult result = new DeployResult();
        result.setId(task.getId());
        executor.destroy();
        result.setState(TerraformExecState.DESTROY_SUCCESS);
        return result;
    }

    /**
     * Create terraform script.
     *
     * @param csp       the cloud service provider.
     * @param workspace the workspace for the terraform.
     * @param script    the terraform scripts of the task.
     */
    private void createScriptFile(Csp csp, String workspace, String script) {
        log.info("start create terraform script");
        String verScriptPath = workspace + File.separator + VERSION_FILE_NAME;
        String scriptPath = workspace + File.separator + SCRIPT_FILE_NAME;
        try {
            try (FileWriter verWriter = new FileWriter(verScriptPath);
                    FileWriter scriptWriter = new FileWriter(scriptPath)) {
                verWriter.write(TerraformProviders.getProvider(csp).getProvider());
                scriptWriter.write(script);
            }
            log.info("terraform script create success");
        } catch (IOException ex) {
            log.error("create version file failed.", ex);
            throw new TerraformExecutorException("create version file failed.", ex);
        }
    }

    /**
     * Build workspace of the `terraform`.
     *
     * @param workspace The workspace of the task.
     */
    private void buildWorkspace(String workspace) {
        log.info("start create workspace");
        File ws = new File(workspace);
        if (!ws.exists() && !ws.mkdirs()) {
            throw new TerraformExecutorException(
                    "Create workspace failed, File path not created: " + ws.getAbsolutePath());
        }
        log.info("workspace create success,Working directory is " + ws.getAbsolutePath());
    }

    /**
     * Get the workspace path for terraform.
     *
     * @param taskId The id of the task.
     */
    private String getWorkspacePath(String taskId) {
        return WORKSPACE + File.separator + File.separator + taskId;
    }

    /**
     * Get environment variable for terraform.
     *
     * @param task the context of the task.
     */
    public Map<String, String> getEnv(DeployTask task) {
        Map<String, String> variables = new HashMap<>();
        Map<String, String> request = task.getCreateRequest().getProperty();
        for (DeployVariable variable : task.getOcl().getDeployment().getContext()) {
            if (variable.getKind() == DeployVariableKind.ENV) {
                if (request.containsKey(variable.getName())) {
                    variables.put(variable.getName(), request.get(variable.getName()));
                } else {
                    variables.put(variable.getName(), System.getenv(variable.getName()));
                }
            }

            if (variable.getKind() == DeployVariableKind.FIX_ENV) {
                variables.put(variable.getName(), request.get(variable.getValue()));
            }
        }

        variables.putAll(getFlavorVariables(task));

        return variables;
    }

    private Map<String, String> getFlavorVariables(DeployTask task) {
        Map<String, String> variables = new HashMap<>();

        variables.put("TF_VAR_flavor", task.getCreateRequest().getFlavor());
        for (Flavor flavor : task.getOcl().getFlavors()) {
            if (flavor.getName().equals(task.getCreateRequest().getFlavor())) {
                for (Map.Entry<String, String> entry : flavor.getProperty().entrySet()) {
                    variables.put(("TF_VAR_flavor_" + entry.getKey()), entry.getValue());
                }
                return variables;
            }
        }

        throw new RuntimeException("Can not get an available flavor.");
    }

    /**
     * Get terraform variables.
     *
     * @param task the DeployTask.
     */
    public Map<String, String> getVariables(DeployTask task) {
        Map<String, String> variables = new HashMap<>();
        Map<String, String> request = task.getCreateRequest().getProperty();
        for (DeployVariable variable : task.getOcl().getDeployment().getContext()) {
            if (variable.getKind() == DeployVariableKind.VARIABLE
                    && request.containsKey(variable.getName())) {
                variables.put(variable.getName(), request.get(variable.getName()));
            }

            if (variable.getKind() == DeployVariableKind.FIX_VARIABLE
                    && request.containsKey(variable.getName())) {
                variables.put(variable.getName(), request.get(variable.getValue()));
            }
        }

        return variables;
    }

    /**
     * Get the deployer kind.
     */
    @Override
    public DeployerKind getDeployerKind() {
        return DeployerKind.TERRAFORM;
    }
}
