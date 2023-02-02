/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.osc.orchestrator;

import lombok.extern.java.Log;
import org.eclipse.osc.modules.ocl.loader.data.models.Ocl;
import org.springframework.stereotype.Component;

@Log
@Component
public class PluginTest implements OrchestratorPlugin {

    private Ocl ocl;

    @Override
    public void registerManagedService(Ocl ocl) {
        log.info("OSC Test Plugin :: Registering managed service");
        this.ocl = ocl;
    }

    public void updateManagedService(String managedServiceName, Ocl ocl) {
        log.info("OSC Test Plugin :: Updating managed service " + managedServiceName);
    }

    @Override
    public void startManagedService(String managedServiceName) {
        log.info("OSC Test Plugin :: Starting managed service " + managedServiceName);
    }

    @Override
    public void stopManagedService(String managedServiceName) {
        log.info("OSC Test Plugin ::Stopping managed service " + managedServiceName);
    }

    @Override
    public void unregisterManagedService(String managedServiceName) {
        log.info("OSC Test Plugin :: Unregistering managed service " + managedServiceName);
        this.ocl = null;
    }

    public Ocl getOcl() {
        return this.ocl;
    }

}
