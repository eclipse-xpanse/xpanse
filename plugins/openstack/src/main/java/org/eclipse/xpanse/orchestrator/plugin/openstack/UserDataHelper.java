/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator.plugin.openstack;

import java.util.Base64;
import org.eclipse.xpanse.modules.ocl.loader.data.models.UserData;
import org.eclipse.xpanse.modules.ocl.loader.data.models.enums.UserDataType;

/**
 * Helper class to handle UserData scripts.
 */
public class UserDataHelper {

    /**
     * Method to build a bash script to be executed as part of cloud-init using the
     * provisioning commands described in the Ocl.
     *
     * @param userData Object holding the userdata information for the VM.
     * @return cloud-init bash script as a string.
     */
    public static String getUserData(UserData userData) {
        StringBuilder shellScript = new StringBuilder();
        if (userData.getType() == UserDataType.SHELL) {
            shellScript = new StringBuilder("#!/bin/sh\n");
            for (String command : userData.getCommands()) {
                shellScript.append(command).append("\n");
            }
        }
        return Base64.getEncoder().encodeToString(shellScript.toString().getBytes());
    }
}
