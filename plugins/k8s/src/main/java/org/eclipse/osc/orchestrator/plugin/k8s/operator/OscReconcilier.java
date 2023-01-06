package org.eclipse.osc.orchestrator.plugin.k8s.operator;

import io.javaoperatorsdk.operator.api.reconciler.*;

@ControllerConfiguration
public class OscReconcilier implements Reconciler<OscCustomResource> {

    @Override
    public UpdateControl<OscCustomResource> reconcile(OscCustomResource resource, Context context) {
        System.out.println("OSC managed service " + resource.getSpec().getName()
                + " updated (OCL location is " + resource.getSpec().getOcl() + ")");
        return UpdateControl.updateResource(resource);
    }

}
