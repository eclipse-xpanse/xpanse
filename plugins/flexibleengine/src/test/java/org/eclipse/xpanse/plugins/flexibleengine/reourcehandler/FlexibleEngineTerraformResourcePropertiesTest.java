package org.eclipse.xpanse.plugins.flexibleengine.reourcehandler;

import java.util.Set;
import org.eclipse.xpanse.modules.models.service.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployResourceProperties;
import org.eclipse.xpanse.plugins.flexibleengine.resourcehandler.FlexibleEngineTerraformResourceProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FlexibleEngineTerraformResourcePropertiesTest {

    @Test
    void testGetDeployResourceProperties() {
        // Run the test
        final DeployResourceProperties vmResult =
                FlexibleEngineTerraformResourceProperties.getDeployResourceProperties(
                        "flexibleengine_compute_instance_v2");
        // Verify the results
        Assertions.assertNotNull(vmResult);
        Assertions.assertEquals(vmResult.getResourceKind(), DeployResourceKind.VM);
        Assertions.assertFalse(vmResult.getResourceProperties().isEmpty());

        // Run the test
        final DeployResourceProperties volumeResult =
                FlexibleEngineTerraformResourceProperties.getDeployResourceProperties(
                        "flexibleengine_blockstorage_volume_v2");
        // Verify the results
        Assertions.assertNotNull(volumeResult);
        Assertions.assertEquals(volumeResult.getResourceKind(), DeployResourceKind.VOLUME);
        Assertions.assertFalse(volumeResult.getResourceProperties().isEmpty());

        // Run the test
        final DeployResourceProperties subnetResult =
                FlexibleEngineTerraformResourceProperties.getDeployResourceProperties(
                        "flexibleengine_vpc_subnet_v1");
        // Verify the results
        Assertions.assertNotNull(subnetResult);
        Assertions.assertEquals(subnetResult.getResourceKind(), DeployResourceKind.SUBNET);
        Assertions.assertFalse(subnetResult.getResourceProperties().isEmpty());

        // Run the test
        final DeployResourceProperties vpcResult =
                FlexibleEngineTerraformResourceProperties.getDeployResourceProperties(
                        "flexibleengine_vpc_v1");
        // Verify the results
        Assertions.assertNotNull(vpcResult);
        Assertions.assertEquals(vpcResult.getResourceKind(), DeployResourceKind.VPC);
        Assertions.assertFalse(vpcResult.getResourceProperties().isEmpty());

        // Run the test
        final DeployResourceProperties publicIpResult =
                FlexibleEngineTerraformResourceProperties.getDeployResourceProperties(
                        "flexibleengine_vpc_eip");
        // Verify the results
        Assertions.assertNotNull(publicIpResult);
        Assertions.assertEquals(publicIpResult.getResourceKind(), DeployResourceKind.PUBLIC_IP);
        Assertions.assertFalse(publicIpResult.getResourceProperties().isEmpty());

        // Run the test
        final DeployResourceProperties secGroupResult =
                FlexibleEngineTerraformResourceProperties.getDeployResourceProperties(
                        "flexibleengine_networking_secgroup_v2");
        // Verify the results
        Assertions.assertNotNull(secGroupResult);
        Assertions.assertEquals(
                secGroupResult.getResourceKind(), DeployResourceKind.SECURITY_GROUP);
        Assertions.assertFalse(secGroupResult.getResourceProperties().isEmpty());

        // Run the test
        final DeployResourceProperties secGroupRuleResult =
                FlexibleEngineTerraformResourceProperties.getDeployResourceProperties(
                        "flexibleengine_networking_secgroup_rule_v2");
        // Verify the results
        Assertions.assertNotNull(secGroupRuleResult);
        Assertions.assertEquals(
                secGroupRuleResult.getResourceKind(), DeployResourceKind.SECURITY_GROUP_RULE);
        Assertions.assertFalse(secGroupRuleResult.getResourceProperties().isEmpty());

        // Run the test
        final DeployResourceProperties keypairResult =
                FlexibleEngineTerraformResourceProperties.getDeployResourceProperties(
                        "flexibleengine_compute_keypair_v2");
        // Verify the results
        Assertions.assertNotNull(keypairResult);
        Assertions.assertEquals(keypairResult.getResourceKind(), DeployResourceKind.KEYPAIR);
        Assertions.assertFalse(keypairResult.getResourceProperties().isEmpty());
    }

    @Test
    void testGetDeployResourceProperties_WithUnsupportedResourceType() {
        // Run the test
        final DeployResourceProperties result =
                FlexibleEngineTerraformResourceProperties.getDeployResourceProperties(
                        "unsupported_type");
        // Verify the results
        Assertions.assertNotNull(result);
        Assertions.assertEquals(result.getResourceKind(), DeployResourceKind.UNKNOWN);
        Assertions.assertTrue(result.getResourceProperties().isEmpty());
    }

    @Test
    void testGetTerraformResourceTypes() {
        Set<String> exceptedTypes =
                Set.of(
                        "flexibleengine_compute_instance_v2",
                        "flexibleengine_networking_secgroup_v2",
                        "flexibleengine_blockstorage_volume_v2",
                        "flexibleengine_vpc_v1",
                        "flexibleengine_vpc_subnet_v1",
                        "flexibleengine_vpc_eip",
                        "flexibleengine_compute_keypair_v2",
                        "flexibleengine_networking_secgroup_rule_v2");
        Assertions.assertEquals(
                exceptedTypes,
                FlexibleEngineTerraformResourceProperties.getTerraformResourceTypes());
    }
}
