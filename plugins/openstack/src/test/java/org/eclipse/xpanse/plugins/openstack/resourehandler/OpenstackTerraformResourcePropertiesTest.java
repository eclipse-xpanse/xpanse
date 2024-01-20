package org.eclipse.xpanse.plugins.openstack.resourehandler;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.eclipse.xpanse.modules.models.service.deploy.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployResourceProperties;
import org.eclipse.xpanse.plugins.openstack.resourcehandler.OpenstackTerraformResourceProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class OpenstackTerraformResourcePropertiesTest {

    @Test
    void testGetDeployResourceProperties() {
        // Run the test
        final DeployResourceProperties vmResult =
                OpenstackTerraformResourceProperties.getDeployResourceProperties(
                        "openstack_compute_instance_v2");
        // Verify the results
        Assertions.assertNotNull(vmResult);
        Assertions.assertEquals(vmResult.getResourceKind(), DeployResourceKind.VM);
        Assertions.assertTrue(vmResult.getResourceProperties().size() >= 1);

        // Run the test
        final DeployResourceProperties volumeResult =
                OpenstackTerraformResourceProperties.getDeployResourceProperties(
                        "openstack_blockstorage_volume_v3");
        // Verify the results
        Assertions.assertNotNull(volumeResult);
        Assertions.assertEquals(volumeResult.getResourceKind(), DeployResourceKind.VOLUME);
        Assertions.assertTrue(volumeResult.getResourceProperties().size() >= 1);


        // Run the test
        final DeployResourceProperties subnetResult =
                OpenstackTerraformResourceProperties.getDeployResourceProperties(
                        "openstack_networking_subnet_v2");
        // Verify the results
        Assertions.assertNotNull(subnetResult);
        Assertions.assertEquals(subnetResult.getResourceKind(), DeployResourceKind.SUBNET);
        Assertions.assertTrue(subnetResult.getResourceProperties().size() >= 1);

        // Run the test
        final DeployResourceProperties vpcResult =
                OpenstackTerraformResourceProperties.getDeployResourceProperties(
                        "openstack_networking_network_v2");
        // Verify the results
        Assertions.assertNotNull(vpcResult);
        Assertions.assertEquals(vpcResult.getResourceKind(), DeployResourceKind.VPC);
        Assertions.assertTrue(vpcResult.getResourceProperties().size() >= 1);

        // Run the test
        final DeployResourceProperties publicIpResult =
                OpenstackTerraformResourceProperties.getDeployResourceProperties(
                        "openstack_networking_floatingip_v2");
        // Verify the results
        Assertions.assertNotNull(publicIpResult);
        Assertions.assertEquals(publicIpResult.getResourceKind(), DeployResourceKind.PUBLIC_IP);
        Assertions.assertTrue(publicIpResult.getResourceProperties().size() >= 1);

        // Run the test
        final DeployResourceProperties secGroupResult =
                OpenstackTerraformResourceProperties.getDeployResourceProperties(
                        "openstack_networking_secgroup_v2");
        // Verify the results
        Assertions.assertNotNull(secGroupResult);
        Assertions.assertEquals(secGroupResult.getResourceKind(),
                DeployResourceKind.SECURITY_GROUP);
        Assertions.assertTrue(secGroupResult.getResourceProperties().size() >= 1);

        // Run the test
        final DeployResourceProperties secGroupRuleResult =
                OpenstackTerraformResourceProperties.getDeployResourceProperties(
                        "openstack_networking_secgroup_rule_v2");
        // Verify the results
        Assertions.assertNotNull(secGroupRuleResult);
        Assertions.assertEquals(secGroupRuleResult.getResourceKind(),
                DeployResourceKind.SECURITY_GROUP_RULE);
        Assertions.assertTrue(secGroupRuleResult.getResourceProperties().size() >= 1);

        // Run the test
        final DeployResourceProperties keypairResult =
                OpenstackTerraformResourceProperties.getDeployResourceProperties(
                        "openstack_compute_keypair_v2");
        // Verify the results
        Assertions.assertNotNull(keypairResult);
        Assertions.assertEquals(keypairResult.getResourceKind(), DeployResourceKind.KEYPAIR);
        Assertions.assertTrue(keypairResult.getResourceProperties().size() >= 1);
    }

    @Test
    void testGetDeployResourceProperties_WithUnsupportedResourceType() {
        // Run the test
        final DeployResourceProperties result =
                OpenstackTerraformResourceProperties.getDeployResourceProperties(
                        "unsupported_type");
        // Verify the results
        Assertions.assertNotNull(result);
        Assertions.assertEquals(result.getResourceKind(), DeployResourceKind.UNKNOWN);
        Assertions.assertTrue(result.getResourceProperties().isEmpty());
    }

    @Test
    void testGetTerraformResourceTypes() {
        Set<String> exceptedTypes = Set.of("openstack_compute_instance_v2",
                "openstack_networking_subnet_v2",
                "openstack_networking_floatingip_v2",
                "openstack_compute_keypair_v2",
                "openstack_networking_network_v2",
                "openstack_networking_secgroup_rule_v2",
                "openstack_networking_secgroup_v2",
                "openstack_blockstorage_volume_v3");
        assertThat(OpenstackTerraformResourceProperties.getTerraformResourceTypes())
                .isEqualTo(exceptedTypes);
    }
}
