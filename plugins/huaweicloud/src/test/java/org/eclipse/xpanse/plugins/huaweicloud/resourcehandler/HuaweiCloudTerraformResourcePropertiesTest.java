package org.eclipse.xpanse.plugins.huaweicloud.resourcehandler;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.eclipse.xpanse.modules.models.service.deploy.DeployResourceProperties;
import org.eclipse.xpanse.modules.models.service.deploy.enums.DeployResourceKind;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class HuaweiCloudTerraformResourcePropertiesTest {

    @Test
    void testGetDeployResourceProperties() {
        // Run the test
        final DeployResourceProperties vmResult =
                HuaweiCloudTerraformResourceProperties.getDeployResourceProperties(
                        "huaweicloud_compute_instance");
        // Verify the results
        Assertions.assertNotNull(vmResult);
        Assertions.assertEquals(vmResult.getResourceKind(), DeployResourceKind.VM);
        Assertions.assertTrue(vmResult.getResourceProperties().size() >= 1);

        // Run the test
        final DeployResourceProperties volumeResult =
                HuaweiCloudTerraformResourceProperties.getDeployResourceProperties(
                        "huaweicloud_evs_volume");
        // Verify the results
        Assertions.assertNotNull(volumeResult);
        Assertions.assertEquals(volumeResult.getResourceKind(), DeployResourceKind.VOLUME);
        Assertions.assertTrue(volumeResult.getResourceProperties().size() >= 1);


        // Run the test
        final DeployResourceProperties subnetResult =
                HuaweiCloudTerraformResourceProperties.getDeployResourceProperties(
                        "huaweicloud_vpc_subnet");
        // Verify the results
        Assertions.assertNotNull(subnetResult);
        Assertions.assertEquals(subnetResult.getResourceKind(), DeployResourceKind.SUBNET);
        Assertions.assertTrue(subnetResult.getResourceProperties().size() >= 1);

        // Run the test
        final DeployResourceProperties vpcResult =
                HuaweiCloudTerraformResourceProperties.getDeployResourceProperties(
                        "huaweicloud_vpc");
        // Verify the results
        Assertions.assertNotNull(vpcResult);
        Assertions.assertEquals(vpcResult.getResourceKind(), DeployResourceKind.VPC);
        Assertions.assertTrue(vpcResult.getResourceProperties().size() >= 1);

        // Run the test
        final DeployResourceProperties publicIpResult =
                HuaweiCloudTerraformResourceProperties.getDeployResourceProperties(
                        "huaweicloud_vpc_eip");
        // Verify the results
        Assertions.assertNotNull(publicIpResult);
        Assertions.assertEquals(publicIpResult.getResourceKind(), DeployResourceKind.PUBLIC_IP);
        Assertions.assertTrue(publicIpResult.getResourceProperties().size() >= 1);

        // Run the test
        final DeployResourceProperties secGroupResult =
                HuaweiCloudTerraformResourceProperties.getDeployResourceProperties(
                        "huaweicloud_networking_secgroup");
        // Verify the results
        Assertions.assertNotNull(secGroupResult);
        Assertions.assertEquals(secGroupResult.getResourceKind(),
                DeployResourceKind.SECURITY_GROUP);
        Assertions.assertTrue(secGroupResult.getResourceProperties().size() >= 1);

        // Run the test
        final DeployResourceProperties secGroupRuleResult =
                HuaweiCloudTerraformResourceProperties.getDeployResourceProperties(
                        "huaweicloud_networking_secgroup_rule");
        // Verify the results
        Assertions.assertNotNull(secGroupRuleResult);
        Assertions.assertEquals(secGroupRuleResult.getResourceKind(),
                DeployResourceKind.SECURITY_GROUP_RULE);
        Assertions.assertTrue(secGroupRuleResult.getResourceProperties().size() >= 1);

        // Run the test
        final DeployResourceProperties keypairResult =
                HuaweiCloudTerraformResourceProperties.getDeployResourceProperties(
                        "huaweicloud_kps_keypair");
        // Verify the results
        Assertions.assertNotNull(keypairResult);
        Assertions.assertEquals(keypairResult.getResourceKind(), DeployResourceKind.KEYPAIR);
        Assertions.assertTrue(keypairResult.getResourceProperties().size() >= 1);
    }

    @Test
    void testGetDeployResourceProperties_WithUnsupportedResourceType() {
        // Run the test
        final DeployResourceProperties result =
                HuaweiCloudTerraformResourceProperties.getDeployResourceProperties(
                        "unsupported_type");
        // Verify the results
        Assertions.assertNotNull(result);
        Assertions.assertEquals(result.getResourceKind(), DeployResourceKind.UNKNOWN);
        Assertions.assertTrue(result.getResourceProperties().isEmpty());
    }

    @Test
    void testGetTerraformResourceTypes() {
        Set<String> exceptedTypes = Set.of("huaweicloud_compute_instance",
                "huaweicloud_evs_volume",
                "huaweicloud_vpc_subnet",
                "huaweicloud_networking_secgroup_rule",
                "huaweicloud_kps_keypair",
                "huaweicloud_vpc",
                "huaweicloud_networking_secgroup",
                "huaweicloud_vpc_eip");
        assertThat(HuaweiCloudTerraformResourceProperties.getTerraformResourceTypes())
                .isEqualTo(exceptedTypes);
    }
}
