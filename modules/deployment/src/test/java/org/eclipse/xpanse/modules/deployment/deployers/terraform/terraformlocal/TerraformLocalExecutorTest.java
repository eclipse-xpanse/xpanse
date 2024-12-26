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
import org.eclipse.xpanse.modules.deployment.deployers.terraform.resources.TfState;
import org.eclipse.xpanse.modules.deployment.utils.DeploymentScriptsHelper;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TerraformLocalExecutorTest {

    private static String workspace = "";
    @Mock private Map<String, String> mockEnv;
    @Mock private Map<String, Object> mockVariables;
    @InjectMocks private TerraformLocalExecutor terraformLocalExecutor;
    @InjectMocks private DeploymentScriptsHelper scriptsHelper;

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
        Map<String, String> scriptsMap = ocl.getDeployment().getScriptFiles();
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
        ReflectionTestUtils.setField(scriptsHelper, "awaitAtMost", 60);
        ReflectionTestUtils.setField(scriptsHelper, "awaitPollingInterval", 1);
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
        assertEquals(result.getCommandStdError(), "");
        assertEquals(result.getCommandExecuted(), "terraform init -no-color");
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
        assertEquals(result.getCommandStdError(), "");
        assertEquals(
                result.getCommandExecuted(),
                "terraform plan -input=false -no-color  -var-file=variables.tfvars.json");
    }

    @Test
    @Order(5)
    void testTfPlanWithOutput() {
        // Setup
        // Run the test
        final SystemCmdResult result = terraformLocalExecutor.tfPlanWithOutput();
        // Verify the results
        assertTrue(result.isCommandSuccessful());
        assertEquals(result.getCommandStdError(), "");
        assertEquals(
                result.getCommandExecuted(),
                "terraform plan -input=false -no-color --out tfplan.binary"
                        + " -var-file=variables.tfvars.json");
    }

    @Test
    @Order(6)
    void testTfApply() {
        // Run the test
        final SystemCmdResult result = terraformLocalExecutor.tfApply();
        // Verify the results
        assertTrue(result.isCommandSuccessful());
        assertEquals(result.getCommandStdError(), "");
        assertEquals(
                result.getCommandExecuted(),
                "terraform apply -auto-approve -input=false -no-color "
                        + " -var-file=variables.tfvars.json");
    }

    @Test
    @Order(7)
    void testTfDestroy() {
        // Run the test
        final SystemCmdResult result = terraformLocalExecutor.tfDestroy();
        // Verify the results
        assertTrue(result.isCommandSuccessful());
        assertEquals(result.getCommandStdError(), "");
        assertEquals(
                result.getCommandExecuted(),
                "terraform destroy -auto-approve -input=false -no-color "
                        + " -var-file=variables.tfvars.json");
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
                        .readValue(scriptsHelper.getTaskTerraformState(workspace), TfState.class);
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
                        .readValue(scriptsHelper.getTaskTerraformState(workspace), TfState.class);
        assertTrue(tfState.getOutputs().isEmpty());
    }
}
