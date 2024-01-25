package org.eclipse.xpanse.modules.deployment.deployers.opentofu;

import static org.eclipse.xpanse.modules.deployment.deployers.opentofu.OpenTofuDeployment.SCRIPT_FILE_NAME;
import static org.eclipse.xpanse.modules.deployment.deployers.opentofu.OpenTofuDeployment.VERSION_FILE_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.util.Map;
import java.util.UUID;
import org.eclipse.xpanse.common.systemcmd.SystemCmdResult;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.resource.TfState;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
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
class OpenTofuExecutorTest {
    private static final String workspace =
            System.getProperty("java.io.tmpdir") + "/opentofu_workspace/" + UUID.randomUUID();
    private static final String version = "~> 1.51.0";
    @Mock
    private Map<String, String> mockEnv;
    @Mock
    private Map<String, Object> mockVariables;
    private OpenTofuExecutor openTofuExecutorUnderTest;

    @BeforeAll
    static void initWorkSpace() throws Exception {
        OclLoader oclLoader = new OclLoader();
        Ocl ocl =
                oclLoader.getOcl(URI.create("file:src/test/resources/opentofu_test.yaml").toURL());
        String script = ocl.getDeployment().getDeployer();
        File ws = new File(workspace + "/" + UUID.randomUUID());
        ws.mkdirs();
        String verScript = String.format("""
                terraform {
                  required_providers {
                    huaweicloud = {
                      source = "huaweicloud/huaweicloud"
                      version = "%s"
                    }
                  }
                }
                            
                provider "huaweicloud" {
                  region = "%s"
                }
                """, version, ocl.getCloudServiceProvider().getRegions().getFirst().getName());
        String verScriptPath = workspace + File.separator + VERSION_FILE_NAME;
        String scriptPath = workspace + File.separator + SCRIPT_FILE_NAME;
        try (FileWriter verWriter = new FileWriter(verScriptPath);
             FileWriter scriptWriter = new FileWriter(scriptPath)) {
            verWriter.write(verScript);
            scriptWriter.write(script);
        }
    }

    @BeforeEach
    void setUp() {
        openTofuExecutorUnderTest = new OpenTofuExecutor(mockEnv, mockVariables, workspace);
    }

    @Test
    @Order(1)
    void testTfInit() {
        // Run the test
        final SystemCmdResult result = openTofuExecutorUnderTest.tfInit();
        // Verify the results
        assertTrue(result.isCommandSuccessful());
        assertEquals(result.getCommandStdError(), "");
        assertEquals(result.getCommandExecuted(), "tofu init -no-color");
    }

    @Test
    @Order(2)
    void testGetOpenTofuPlanAsJson() {
        assertNotNull(openTofuExecutorUnderTest.getOpenTofuPlanAsJson());
    }

    @Test
    @Order(3)
    void testTfPlan() {
        // Run the test
        final SystemCmdResult result = openTofuExecutorUnderTest.tfPlan();
        // Verify the results
        assertTrue(result.isCommandSuccessful());
        assertEquals(result.getCommandStdError(), "");
        assertEquals(result.getCommandExecuted(),
                "tofu plan -input=false -no-color  -var-file=variables.tfvars.json");
    }

    @Test
    @Order(4)
    void testTfPlanWithOutput() {
        // Setup
        // Run the test
        final SystemCmdResult result = openTofuExecutorUnderTest.tfPlanWithOutput();
        // Verify the results
        assertTrue(result.isCommandSuccessful());
        assertEquals(result.getCommandStdError(), "");
        assertEquals(result.getCommandExecuted(),
                "tofu plan -input=false -no-color --out tfplan.binary -var-file=variables.tfvars.json");
    }

    @Test
    @Order(5)
    void testTfApply() {
        // Run the test
        final SystemCmdResult result = openTofuExecutorUnderTest.tfApply();
        // Verify the results
        assertTrue(result.isCommandSuccessful());
        assertEquals(result.getCommandStdError(), "");
        assertEquals(result.getCommandExecuted(),
                "tofu apply -auto-approve -input=false -no-color  -var-file=variables.tfvars.json");
    }

    @Test
    @Order(6)
    void testTfDestroy() {
        // Run the test
        final SystemCmdResult result = openTofuExecutorUnderTest.tfDestroy();
        // Verify the results
        assertTrue(result.isCommandSuccessful());
        assertEquals(result.getCommandStdError(), "");
        assertEquals(result.getCommandExecuted(),
                "tofu destroy -auto-approve -input=false -no-color  -var-file=variables.tfvars.json");

    }

    @Test
    @Order(7)
    void testDeploy() throws JsonProcessingException {
        // Setup
        // Run the test
        openTofuExecutorUnderTest.deploy();
        // Verify the results
        TfState tfState =
                new ObjectMapper().readValue(openTofuExecutorUnderTest.getTerraformState(),
                        TfState.class);
        assertFalse(tfState.getOutputs().isEmpty());
    }

    @Test
    @Order(8)
    void testDestroy() throws JsonProcessingException {
        // Setup
        // Run the test
        openTofuExecutorUnderTest.destroy();
        // Verify the results
        TfState tfState =
                new ObjectMapper().readValue(openTofuExecutorUnderTest.getTerraformState(),
                        TfState.class);
        assertTrue(tfState.getOutputs().isEmpty());
    }

    @Test
    @Order(9)
    void testGetImportantFilesContent() {
        // Setup
        // Run the test
        final Map<String, String> result = openTofuExecutorUnderTest.getImportantFilesContent();
        // Verify the results
        assertTrue(result.containsKey("terraform.tfstate.backup"));
    }

}
