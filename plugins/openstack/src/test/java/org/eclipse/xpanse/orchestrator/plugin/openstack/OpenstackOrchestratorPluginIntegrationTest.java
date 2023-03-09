/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator.plugin.openstack;

import org.eclipse.xpanse.modules.models.utils.OclLoader;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {OpenstackOrchestratorPlugin.class, OclLoader.class})
@TestPropertySource(locations = "classpath:application-test.properties")
@Disabled("Needs a working openstack instance")
@ActiveProfiles(value = {"openstack", "test"})
public class OpenstackOrchestratorPluginIntegrationTest {

    @Autowired
    OpenstackOrchestratorPlugin openstackOrchestratorPlugin;

    @Autowired
    OclLoader oclLoader;

    @Test
    public void onRegisterTest() throws Exception {
    }
}
