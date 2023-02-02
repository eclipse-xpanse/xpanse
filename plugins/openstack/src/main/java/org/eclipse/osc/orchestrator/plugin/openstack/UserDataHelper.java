/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.osc.orchestrator.plugin.openstack;

import java.util.Base64;
import java.util.List;
import org.eclipse.osc.modules.ocl.loader.data.models.Ocl;
import org.eclipse.osc.modules.ocl.loader.data.models.Provisioner;

/**
 * Helper class to handle UserData scripts.
 */
public class UserDataHelper {

    /**
     * Method to build a bash script to be executed as part of cloud-init using the
     * provisioning commands described in the Ocl.
     *
     * @param provisionersToBeExecuted provisioners in the managed service that must be used to
     *                                 generate the cloud-init script.
     * @param ocl                      Full description of the managed service.
     * @return cloud-init bash script as a string.
     */
    public static String getUserData(List<String> provisionersToBeExecuted, Ocl ocl) {
        StringBuilder shellScript = new StringBuilder();
        for (Provisioner provisioner : ocl.getImage().getProvisioners()) {
            if (provisionersToBeExecuted.contains(provisioner.getName())) {
                if (provisioner.getType().equalsIgnoreCase("shell")) {
                    shellScript = new StringBuilder("#!/bin/sh\n");
                    for (String scriptLine : provisioner.getInline()) {
                        shellScript.append(scriptLine).append("\n");
                    }
                }
            }
        }
        return Base64.getEncoder().encodeToString(shellScript.toString().getBytes());
    }
}
