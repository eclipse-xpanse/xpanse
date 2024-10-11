package org.eclipse.xpanse.modules.deployment.deployers.opentofu.opentofulocal;

import static org.eclipse.xpanse.modules.deployment.deployers.opentofu.opentofulocal.OpenTofuLocalDeployment.SCRIPT_FILE_NAME;
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
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.resources.TfState;
import org.eclipse.xpanse.modules.deployment.utils.DeployResultFileUtils;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
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
class OpenTofuLocalExecutorTest {
    private static final String workspace =
            System.getProperty("java.io.tmpdir") + "/opentofu_workspace/" + UUID.randomUUID();
    @Mock
    private Map<String, String> mockEnv;
    @Mock
    private Map<String, Object> mockVariables;
    private OpenTofuLocalExecutor openTofuLocalExecutorUnderTest;
    @Mock
    private DeployResultFileUtils deployResultFileUtilsTest;

    @BeforeAll
    static void initWorkSpace() throws Exception {
        OclLoader oclLoader = new OclLoader();
        Ocl ocl = oclLoader.getOcl(
                URI.create("file:src/test/resources/ocl_terraform_test.yml").toURL());
        ocl.getDeployment().getDeployerTool().setKind(DeployerKind.OPEN_TOFU);
        String script = ocl.getDeployment().getDeployer();
        File ws = new File(workspace + "/" + UUID.randomUUID());
        ws.mkdirs();
        String scriptPath = workspace + File.separator + SCRIPT_FILE_NAME;
        try (FileWriter scriptWriter = new FileWriter(scriptPath)) {
            scriptWriter.write(script);
        }
    }

    @BeforeEach
    void setUp() {
        openTofuLocalExecutorUnderTest = new OpenTofuLocalExecutor(
                "tofu", mockEnv, mockVariables, workspace, null, deployResultFileUtilsTest);
    }

    @Test
    @Order(1)
    void testTfInit() {
        // Run the test
        final SystemCmdResult result = openTofuLocalExecutorUnderTest.tfInit();
        // Verify the results
        assertTrue(result.isCommandSuccessful());
        assertEquals(result.getCommandStdError(), "");
        assertEquals(result.getCommandExecuted(), "tofu init -no-color");
    }

    @Test
    @Order(2)
    void testGetOpenTofuPlanAsJson() {
        assertNotNull(openTofuLocalExecutorUnderTest.getOpenTofuPlanAsJson());
    }

    @Test
    @Order(3)
    void testTfPlan() {
        // Run the test
        final SystemCmdResult result = openTofuLocalExecutorUnderTest.tfPlan();
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
        final SystemCmdResult result = openTofuLocalExecutorUnderTest.tfPlanWithOutput();
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
        final SystemCmdResult result = openTofuLocalExecutorUnderTest.tfApply();
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
        final SystemCmdResult result = openTofuLocalExecutorUnderTest.tfDestroy();
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
        openTofuLocalExecutorUnderTest.deploy();
        // Verify the results
        TfState tfState =
                new ObjectMapper().readValue(openTofuLocalExecutorUnderTest.getTerraformState(),
                        TfState.class);
        assertFalse(tfState.getOutputs().isEmpty());
    }

    @Test
    @Order(8)
    void testDestroy() throws JsonProcessingException {
        // Setup
        // Run the test
        openTofuLocalExecutorUnderTest.destroy();
        // Verify the results
        TfState tfState =
                new ObjectMapper().readValue(openTofuLocalExecutorUnderTest.getTerraformState(),
                        TfState.class);
        assertTrue(tfState.getOutputs().isEmpty());
    }

    @Test
    @Order(9)
    void testGetImportantFilesContent() {
        // Setup
        // Run the test
        final Map<String, String> result =
                openTofuLocalExecutorUnderTest.getImportantFilesContent();
        // Verify the results
        assertTrue(result.containsKey("terraform.tfstate.backup"));
    }

}
