package org.eclipse.osc.orchestrator.plugin.openstack;

import org.eclipse.osc.modules.ocl.loader.Ocl;
import org.eclipse.osc.modules.ocl.loader.Provisioner;

import java.util.Base64;
import java.util.List;

public class UserDataHelper {

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
