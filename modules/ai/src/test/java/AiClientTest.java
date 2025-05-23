import org.eclipse.xpanse.ai.docker.DockerImageManage;
import org.eclipse.xpanse.ai.generate.AiClientConfiguration;
import org.eclipse.xpanse.ai.generate.AiClientForCodeGeneration;
import org.eclipse.xpanse.ai.generate.ApplicationGenerationManager;
import org.eclipse.xpanse.modules.models.ai.enums.AiApplicationType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = {
            AiClientForCodeGeneration.class,
            AiClientConfiguration.class,
            ApplicationGenerationManager.class,
            DockerImageManage.class
        })
@TestPropertySource(
        properties = {
            "llm.endpoint.url=http://localhost:11434",
            "llm.model.name=deepseek-coder-v2:16b",
            "ai.docker.registry.url=ghcr.io",
            "ai.docker.registry.username=#REPLACE-CORRECT",
            "ai.docker.registry.password=#REPLACE-CORRECT"
        })
@ActiveProfiles("ai")
public class AiClientTest {

    @Autowired ApplicationGenerationManager applicationGenerationManager;

    @Test
    @Disabled
    public void testPrompt() {
        Assertions.assertDoesNotThrow(
                () ->
                        applicationGenerationManager.generateApplicationServerImage(
                                AiApplicationType.GAUSSDB_MCP));
    }
}
