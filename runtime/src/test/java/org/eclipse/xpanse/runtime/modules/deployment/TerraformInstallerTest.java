package org.eclipse.xpanse.runtime.modules.deployment;

import static org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformlocal.TerraformInstaller.TERRAFORM_VERSION_OUTPUT_PATTERN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.annotation.Resource;
import java.io.File;
import java.util.Set;
import org.eclipse.xpanse.modules.deployment.deployers.deployertools.DeployerToolUtils;
import org.eclipse.xpanse.modules.deployment.deployers.deployertools.DeployerToolVersionsCache;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformlocal.TerraformInstaller;
import org.eclipse.xpanse.modules.models.common.exceptions.InvalidDeployerToolException;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.eclipse.xpanse.runtime.cache.redis.AbstractRedisIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
        properties = {
            "http.request.retry.max.attempts=1",
            "enable.redis.distributed.cache=true",
            "support.default.deployment.tool.versions.only=false",
            "spring.profiles.active=noauth,dev"
        })
class TerraformInstallerTest extends AbstractRedisIntegrationTest {

    @Value("${deployer.terraform.default.supported.versions:1.6.0,1.7.0,1.8.0,1.9.0}")
    private String terraformDefaultVersions;

    @Resource private TerraformInstaller installer;
    @Resource private DeployerToolUtils deployerToolUtils;
    @Resource private DeployerToolVersionsCache deployerToolVersionsCache;

    @Test
    void testGetExecutableTerraformByVersion() {

        Set<String> defaultVersions = Set.of(terraformDefaultVersions.split(","));
        Set<String> cachedVersions =
                deployerToolVersionsCache.getVersionsCacheOfDeployerTool(DeployerKind.TERRAFORM);
        assertTrue(cachedVersions.containsAll(defaultVersions));
        assertTrue(cachedVersions.size() >= defaultVersions.size());

        String requiredVersion = "";
        String terraformPath = installer.getExecutorPathThatMatchesRequiredVersion(requiredVersion);
        assertEquals("terraform", terraformPath);

        String requiredVersion1 = "<= v1.7.0";
        String[] operatorAndNumber1 =
                deployerToolUtils.getOperatorAndNumberFromRequiredVersion(requiredVersion1);
        String terraformPath1 =
                installer.getExecutorPathThatMatchesRequiredVersion(requiredVersion1);
        assertTrue(
                deployerToolUtils.checkIfExecutorIsMatchedRequiredVersion(
                        new File(terraformPath1),
                        TERRAFORM_VERSION_OUTPUT_PATTERN,
                        operatorAndNumber1[0],
                        operatorAndNumber1[1]));

        String requiredVersion2 = "= 1.6.0";
        String[] operatorAndNumber2 =
                deployerToolUtils.getOperatorAndNumberFromRequiredVersion(requiredVersion2);
        String terraformPath2 =
                installer.getExecutorPathThatMatchesRequiredVersion(requiredVersion2);
        assertTrue(
                deployerToolUtils.checkIfExecutorIsMatchedRequiredVersion(
                        new File(terraformPath2),
                        TERRAFORM_VERSION_OUTPUT_PATTERN,
                        operatorAndNumber2[0],
                        operatorAndNumber2[1]));

        String requiredVersion3 = ">= v1.8.0";
        String[] operatorAndNumber3 =
                deployerToolUtils.getOperatorAndNumberFromRequiredVersion(requiredVersion3);
        String terraformPath3 =
                installer.getExecutorPathThatMatchesRequiredVersion(requiredVersion3);
        assertTrue(
                deployerToolUtils.checkIfExecutorIsMatchedRequiredVersion(
                        new File(terraformPath3),
                        TERRAFORM_VERSION_OUTPUT_PATTERN,
                        operatorAndNumber3[0],
                        operatorAndNumber3[1]));

        String requiredVersion4 = ">= 100.0.0";
        assertThrows(
                InvalidDeployerToolException.class,
                () -> installer.getExecutorPathThatMatchesRequiredVersion(requiredVersion4));
    }
}
