package org.eclipse.xpanse.modules.deployment.deployers.opentofu.opentofulocal;

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
import org.eclipse.xpanse.modules.deployment.config.DeploymentProperties;
import org.eclipse.xpanse.modules.deployment.config.GitProperties;
import org.eclipse.xpanse.modules.deployment.config.OrderProperties;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.resources.TfState;
import org.eclipse.xpanse.modules.deployment.utils.DeploymentScriptsHelper;
import org.eclipse.xpanse.modules.deployment.utils.ScriptsGitRepoManage;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(
        classes = {
            ScriptsGitRepoManage.class,
            OrderProperties.class,
            DeploymentProperties.class,
            GitProperties.class,
            DeploymentScriptsHelper.class,
            RefreshAutoConfiguration.class
        })
@TestPropertySource(
        properties = {
            "xpanse.deployer.clean-workspace-after-deployment-enabled=true",
            "xpanse.deployer.opentofu-local.debug.enabled=false",
            "xpanse.deployer.opentofu-local.workspace.directory=xpanse_workspace",
            "xpanse.order.order-status.long-polling-seconds=10",
            "xpanse.order.order-status.polling-interval-seconds=5"
        })
@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OpenTofuLocalExecutorTest {
    private static String workspace = "";
    @Mock private Map<String, String> mockEnv;
    @Mock private Map<String, Object> mockVariables;
    private OpenTofuLocalExecutor openTofuLocalExecutor;
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
            String scriptPath = workspace + File.separator + entry.getKey();
            try (FileWriter scriptWriter = new FileWriter(scriptPath)) {
                scriptWriter.write(entry.getValue());
            }
        }
    }

    @BeforeEach
    void setUp() {
        openTofuLocalExecutor =
                new OpenTofuLocalExecutor("tofu", mockEnv, mockVariables, workspace);
    }

    @Test
    @Order(1)
    void testTfInit() {
        // Run the test
        final SystemCmdResult result = openTofuLocalExecutor.tfInit();
        // Verify the results
        assertTrue(result.isCommandSuccessful());
        assertEquals("", result.getCommandStdError());
        assertEquals("tofu init -no-color", result.getCommandExecuted());
    }

    @Test
    @Order(2)
    void testGetOpenTofuPlanAsJson() {
        assertNotNull(openTofuLocalExecutor.getOpenTofuPlanAsJson());
    }

    @Test
    @Order(3)
    void testTfPlan() {
        // Run the test
        final SystemCmdResult result = openTofuLocalExecutor.tfPlan();
        // Verify the results
        assertTrue(result.isCommandSuccessful());
        assertEquals("", result.getCommandStdError());
        assertEquals(
                "tofu plan -input=false -no-color  -var-file=variables.tfvars.json",
                result.getCommandExecuted());
    }

    @Test
    @Order(4)
    void testTfPlanWithOutput() {
        // Setup
        // Run the test
        final SystemCmdResult result = openTofuLocalExecutor.tfPlanWithOutput();
        // Verify the results
        assertTrue(result.isCommandSuccessful());
        assertEquals("", result.getCommandStdError());
        assertEquals(
                "tofu plan -input=false -no-color --out tfplan.binary"
                        + " -var-file=variables.tfvars.json",
                result.getCommandExecuted());
    }

    @Test
    @Order(5)
    void testTfApply() {
        // Run the test
        final SystemCmdResult result = openTofuLocalExecutor.tfApply();
        // Verify the results
        assertTrue(result.isCommandSuccessful());
        assertEquals("", result.getCommandStdError());
        assertEquals(
                "tofu apply -auto-approve -input=false -no-color  -var-file=variables.tfvars.json",
                result.getCommandExecuted());
    }

    @Test
    @Order(6)
    void testTfDestroy() {
        // Run the test
        final SystemCmdResult result = openTofuLocalExecutor.tfDestroy();
        // Verify the results
        assertTrue(result.isCommandSuccessful());
        assertEquals("", result.getCommandStdError());
        assertEquals(
                "tofu destroy -auto-approve -input=false -no-color "
                        + " -var-file=variables.tfvars.json",
                result.getCommandExecuted());
    }

    @Test
    @Order(7)
    void testDeploy() throws JsonProcessingException {
        // Setup
        // Run the test
        openTofuLocalExecutor.deploy();
        // Verify the results
        TfState tfState =
                new ObjectMapper()
                        .readValue(
                                deploymentScriptsHelper.getTaskTerraformState(workspace),
                                TfState.class);
        assertFalse(tfState.getOutputs().isEmpty());
    }

    @Test
    @Order(8)
    void testDestroy() throws JsonProcessingException {
        // Setup
        // Run the test
        openTofuLocalExecutor.destroy();
        // Verify the results
        TfState tfState =
                new ObjectMapper()
                        .readValue(
                                deploymentScriptsHelper.getTaskTerraformState(workspace),
                                TfState.class);
        assertTrue(tfState.getOutputs().isEmpty());
    }
}
