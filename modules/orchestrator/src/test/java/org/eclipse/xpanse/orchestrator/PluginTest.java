/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator;

import lombok.extern.java.Log;
import org.eclipse.xpanse.modules.ocl.loader.data.models.Ocl;
import org.eclipse.xpanse.modules.ocl.loader.data.models.Oclv2;
import org.springframework.stereotype.Component;

@Log
@Component
public class PluginTest implements OrchestratorPlugin {

    private Ocl ocl;

    @Override
    public void registerManagedService(Ocl ocl) {
        log.info("Xpanse Test Plugin :: Registering managed service");
        this.ocl = ocl;
    }

    @Override
    public void registerManagedService(Oclv2 ocl) {
        log.info("Xpanse Test Plugin :: Registering managed service");
    }

    public void updateManagedService(String managedServiceName, Ocl ocl) {
        log.info("Xpanse Test Plugin :: Updating managed service " + managedServiceName);
    }

    @Override
    public void updateManagedService(String managedServiceName, Oclv2 ocl) {
        log.info("Xpanse Test Plugin :: Updating managed service " + managedServiceName);
    }

    @Override
    public void startManagedService(String managedServiceName) {
        log.info("Xpanse Test Plugin :: Starting managed service " + managedServiceName);
    }

    @Override
    public void stopManagedService(String managedServiceName) {
        log.info("Xpanse Test Plugin ::Stopping managed service " + managedServiceName);
    }

    @Override
    public void unregisterManagedService(String managedServiceName) {
        log.info("Xpanse Test Plugin :: Unregistering managed service " + managedServiceName);
        this.ocl = null;
    }

    public Ocl getOcl() {
        return this.ocl;
    }

}
