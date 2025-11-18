/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformlocal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import org.eclipse.xpanse.common.systemcmd.SystemCmdResult;
import org.eclipse.xpanse.modules.deployment.config.DeploymentProperties;
import org.eclipse.xpanse.modules.deployment.config.GitProperties;
import org.eclipse.xpanse.modules.deployment.config.OrderProperties;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.resources.TfState;
import org.eclipse.xpanse.modules.deployment.utils.DeploymentScriptsHelper;
import org.eclipse.xpanse.modules.deployment.utils.ScriptsGitRepoManage;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeploymentScriptValidationResult;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(
        classes = {
            ScriptsGitRepoManage.class,
            OrderProperties.class,
            DeploymentProperties.class,
            GitProperties.class,
            DeploymentScriptsHelper.class
        })
@TestPropertySource(
        properties = {
            "xpanse.deployer.clean-workspace-after-deployment-enabled=true",
            "xpanse.deployer.terraform-local.debug.enabled=false",
            "xpanse.deployer.terraform-local.workspace.directory=xpanse_workspace",
            "xpanse.order.order-status.long-polling-seconds=10",
            "xpanse.order.order-status.polling-interval-seconds=5"
        })
@Import(RefreshAutoConfiguration.class)
@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TerraformLocalExecutorTest {

    private static String workspace = "";
    @Mock private Map<String, String> mockEnv;
    @Mock private Map<String, Object> mockVariables;

    private TerraformLocalExecutor terraformLocalExecutor;
    @Autowired private DeploymentScriptsHelper deploymentScriptsHelper;

    @BeforeAll
    static void initTaskWorkspace() throws Exception {
        File baseWorkspace = new File(System.getProperty("java.io.tmpdir"), "ws-test");
        File taskWorkspace = new File(baseWorkspace, UUID.randomUUID().toString());
        if (!taskWorkspace.exists() && !taskWorkspace.mkdirs()) {
            return;
        }
        workspace = taskWorkspace.getAbsolutePath();
        OclLoader oclLoader = new OclLoader();
        Ocl ocl =
                oclLoader.getOcl(
                        URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        Map<String, String> scriptsMap =
                ocl.getDeployment().getTerraformDeployment().getScriptFiles();
        for (Map.Entry<String, String> entry : scriptsMap.entrySet()) {
            String script = entry.getValue();
            String scriptPath = taskWorkspace + File.separator + entry.getKey();
            try (FileWriter scriptWriter = new FileWriter(scriptPath)) {
                scriptWriter.write(script);
            }
        }
    }

    @BeforeEach
    void setUp() {
        terraformLocalExecutor =
                new TerraformLocalExecutor("terraform", mockEnv, mockVariables, workspace);
    }

    @Test
    @Order(1)
    void testTfValidate() {
        // Setup
        final DeploymentScriptValidationResult expectedResult =
                new DeploymentScriptValidationResult();
        expectedResult.setValid(true);
        expectedResult.setDiagnostics(Collections.emptyList());
        // Run the test
        final DeploymentScriptValidationResult result = terraformLocalExecutor.tfValidate();
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    @Order(2)
    void testTfInit() {
        // Run the test
        final SystemCmdResult result = terraformLocalExecutor.tfInit();
        // Verify the results
        assertTrue(result.isCommandSuccessful());
        assertEquals("", result.getCommandStdError());
        assertEquals("terraform init -no-color", result.getCommandExecuted());
    }

    @Test
    @Order(3)
    void testGetTerraformPlanAsJson() {
        assertNotNull(terraformLocalExecutor.getTerraformPlanAsJson());
    }

    @Test
    @Order(4)
    void testTfPlan() {
        // Run the test
        final SystemCmdResult result = terraformLocalExecutor.tfPlan();
        // Verify the results
        assertTrue(result.isCommandSuccessful());
        assertEquals("", result.getCommandStdError());
        assertEquals(
                "terraform plan -input=false -no-color  -var-file=variables.tfvars.json",
                result.getCommandExecuted());
    }

    @Test
    @Order(5)
    void testTfPlanWithOutput() {
        // Setup
        // Run the test
        final SystemCmdResult result = terraformLocalExecutor.tfPlanWithOutput();
        // Verify the results
        assertTrue(result.isCommandSuccessful());
        assertEquals("", result.getCommandStdError());
        assertEquals(
                "terraform plan -input=false -no-color --out tfplan.binary"
                        + " -var-file=variables.tfvars.json",
                result.getCommandExecuted());
    }

    @Test
    @Order(6)
    void testTfApply() {
        // Run the test
        final SystemCmdResult result = terraformLocalExecutor.tfApply();
        // Verify the results
        assertTrue(result.isCommandSuccessful());
        assertEquals("", result.getCommandStdError());
        assertEquals(
                "terraform apply -auto-approve -input=false -no-color "
                        + " -var-file=variables.tfvars.json",
                result.getCommandExecuted());
    }

    @Test
    @Order(7)
    void testTfDestroy() {
        // Run the test
        final SystemCmdResult result = terraformLocalExecutor.tfDestroy();
        // Verify the results
        assertTrue(result.isCommandSuccessful());
        assertEquals("", result.getCommandStdError());
        assertEquals(
                "terraform destroy -auto-approve -input=false -no-color "
                        + " -var-file=variables.tfvars.json",
                result.getCommandExecuted());
    }

    @Test
    @Order(8)
    void testDeploy() throws JsonProcessingException {
        // Setup
        // Run the test
        terraformLocalExecutor.deploy();
        // Verify the results
        TfState tfState =
                new ObjectMapper()
                        .readValue(
                                deploymentScriptsHelper.getTaskTerraformState(workspace),
                                TfState.class);
        assertFalse(tfState.getOutputs().isEmpty());
    }

    @Test
    @Order(9)
    void testDestroy() throws JsonProcessingException {
        // Setup
        // Run the test
        terraformLocalExecutor.destroy();
        // Verify the results
        TfState tfState =
                new ObjectMapper()
                        .readValue(
                                deploymentScriptsHelper.getTaskTerraformState(workspace),
                                TfState.class);
        assertTrue(tfState.getOutputs().isEmpty());
    }
}
