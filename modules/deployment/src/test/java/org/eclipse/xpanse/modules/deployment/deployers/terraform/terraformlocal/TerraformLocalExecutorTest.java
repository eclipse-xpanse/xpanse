/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformlocal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformlocal.TerraformLocalDeployment.SCRIPT_FILE_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.UUID;
import org.eclipse.xpanse.common.systemcmd.SystemCmdResult;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.resources.TfState;
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
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TerraformLocalExecutorTest {
    private static final String workspace =
            System.getProperty("java.io.tmpdir") + "/terraform_workspace/" + UUID.randomUUID();
    @Mock
    private Map<String, String> mockEnv;
    @Mock
    private Map<String, Object> mockVariables;
    private TerraformLocalExecutor terraformLocalExecutorUnderTest;

    @BeforeAll
    static void initWorkSpace() throws Exception {
        OclLoader oclLoader = new OclLoader();
        Ocl ocl = oclLoader.getOcl(
                URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        String script = ocl.getDeployment().getDeployer();
        File ws = new File(workspace);
        ws.mkdirs();
        String scriptPath = workspace + File.separator + SCRIPT_FILE_NAME;
        try (FileWriter scriptWriter = new FileWriter(scriptPath)) {
            scriptWriter.write(script);
        }
    }

    @BeforeEach
    void setUp() {
        terraformLocalExecutorUnderTest = new TerraformLocalExecutor(
                mockEnv, mockVariables, workspace, null);
    }

    @Test
    @Order(1)
    void testTfValidate() {
        // Setup
        final DeploymentScriptValidationResult
                expectedResult = new DeploymentScriptValidationResult();
        expectedResult.setValid(true);
        expectedResult.setDiagnostics(Collections.emptyList());
        // Run the test
        final DeploymentScriptValidationResult result =
                terraformLocalExecutorUnderTest.tfValidate();
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }


    @Test
    @Order(2)
    void testTfInit() {
        // Run the test
        final SystemCmdResult result = terraformLocalExecutorUnderTest.tfInit();
        // Verify the results
        assertTrue(result.isCommandSuccessful());
        assertEquals(result.getCommandStdError(), "");
        assertEquals(result.getCommandExecuted(), "terraform init -no-color");
    }

    @Test
    @Order(3)
    void testGetTerraformPlanAsJson() {
        assertNotNull(terraformLocalExecutorUnderTest.getTerraformPlanAsJson());
    }

    @Test
    @Order(4)
    void testTfPlan() {
        // Run the test
        final SystemCmdResult result = terraformLocalExecutorUnderTest.tfPlan();
        // Verify the results
        assertTrue(result.isCommandSuccessful());
        assertEquals(result.getCommandStdError(), "");
        assertEquals(result.getCommandExecuted(),
                "terraform plan -input=false -no-color  -var-file=variables.tfvars.json");
    }

    @Test
    @Order(5)
    void testTfPlanWithOutput() {
        // Setup
        // Run the test
        final SystemCmdResult result = terraformLocalExecutorUnderTest.tfPlanWithOutput();
        // Verify the results
        assertTrue(result.isCommandSuccessful());
        assertEquals(result.getCommandStdError(), "");
        assertEquals(result.getCommandExecuted(),
                "terraform plan -input=false -no-color --out tfplan.binary -var-file=variables.tfvars.json");
    }

    @Test
    @Order(6)
    void testTfApply() {
        // Run the test
        final SystemCmdResult result = terraformLocalExecutorUnderTest.tfApply();
        // Verify the results
        assertTrue(result.isCommandSuccessful());
        assertEquals(result.getCommandStdError(), "");
        assertEquals(result.getCommandExecuted(),
                "terraform apply -auto-approve -input=false -no-color  -var-file=variables.tfvars.json");
    }

    @Test
    @Order(7)
    void testTfDestroy() {
        // Run the test
        final SystemCmdResult result = terraformLocalExecutorUnderTest.tfDestroy();
        // Verify the results
        assertTrue(result.isCommandSuccessful());
        assertEquals(result.getCommandStdError(), "");
        assertEquals(result.getCommandExecuted(),
                "terraform destroy -auto-approve -input=false -no-color  -var-file=variables.tfvars.json");

    }

    @Test
    @Order(8)
    void testDeploy() throws JsonProcessingException {
        // Setup
        // Run the test
        terraformLocalExecutorUnderTest.deploy();
        // Verify the results
        TfState tfState =
                new ObjectMapper().readValue(terraformLocalExecutorUnderTest.getTerraformState(),
                        TfState.class);
        assertFalse(tfState.getOutputs().isEmpty());
    }

    @Test
    @Order(9)
    void testDestroy() throws JsonProcessingException {
        // Setup
        // Run the test
        terraformLocalExecutorUnderTest.destroy();
        // Verify the results
        TfState tfState =
                new ObjectMapper().readValue(terraformLocalExecutorUnderTest.getTerraformState(),
                        TfState.class);
        assertTrue(tfState.getOutputs().isEmpty());
    }

    @Test
    @Order(10)
    void testGetImportantFilesContent() throws Exception {
        // Setup
        // Run the test
        final Map<String, String> result =
                terraformLocalExecutorUnderTest.getImportantFilesContent();
        // Verify the results
        assertTrue(result.containsKey("terraform.tfstate.backup"));

        deleteWorkspace();
    }

    void deleteWorkspace() throws Exception {
        Path path = Paths.get(workspace);
        Files.walk(path).sorted(Comparator.reverseOrder()).map(Path::toFile)
                .forEach(File::delete);
    }
}
