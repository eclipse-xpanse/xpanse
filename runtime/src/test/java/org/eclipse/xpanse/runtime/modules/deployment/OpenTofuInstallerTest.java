package org.eclipse.xpanse.runtime.modules.deployment;

import static org.eclipse.xpanse.modules.deployment.deployers.opentofu.opentofulocal.OpenTofuInstaller.OPEN_TOFU_VERSION_OUTPUT_PATTERN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.annotation.Resource;
import java.io.File;
import org.eclipse.xpanse.modules.deployment.deployers.deployertools.DeployerToolUtils;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.opentofulocal.OpenTofuInstaller;
import org.eclipse.xpanse.modules.models.common.exceptions.InvalidDeployerToolException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
        properties = {"http.request.retry.max.attempts=1", "spring.profiles.active=noauth,dev"})
class OpenTofuInstallerTest {

    @Resource private OpenTofuInstaller installer;
    @Resource private DeployerToolUtils deployerToolUtils;

    @Test
    void testGetExecutableTerraformByVersion() {

        String requiredVersion = "";
        String terraformPath = installer.getExecutorPathThatMatchesRequiredVersion(requiredVersion);
        assertEquals("tofu", terraformPath);

        String requiredVersion1 = "<= v1.7.0";
        String[] operatorAndNumber1 =
                deployerToolUtils.getOperatorAndNumberFromRequiredVersion(requiredVersion1);
        String terraformPath1 =
                installer.getExecutorPathThatMatchesRequiredVersion(requiredVersion1);
        assertTrue(
                deployerToolUtils.checkIfExecutorIsMatchedRequiredVersion(
                        new File(terraformPath1),
                        OPEN_TOFU_VERSION_OUTPUT_PATTERN,
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
                        OPEN_TOFU_VERSION_OUTPUT_PATTERN,
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
                        OPEN_TOFU_VERSION_OUTPUT_PATTERN,
                        operatorAndNumber3[0],
                        operatorAndNumber3[1]));

        String requiredVersion4 = ">= 100.0.0";
        assertThrows(
                InvalidDeployerToolException.class,
                () -> installer.getExecutorPathThatMatchesRequiredVersion(requiredVersion4));
    }
}
