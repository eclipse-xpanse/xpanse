package org.eclipse.osc.orchestrator.plugin.k8s.operator;

import io.javaoperatorsdk.operator.Operator;
import lombok.extern.slf4j.Slf4j;
import org.apache.karaf.minho.boot.service.LifeCycleService;
import org.apache.karaf.minho.boot.service.ServiceRegistry;
import org.apache.karaf.minho.boot.spi.Service;

@Slf4j
public class OscOperator implements Service {

    private Operator operator;

    @Override
    public String name() {
        return "osc-kubernetes-operator";
    }

    @Override
    public void onRegister(ServiceRegistry serviceRegistry) {
        log.info("Registering OSC Kubernetes Operator");
        operator = new Operator();
        operator.register(new OscReconcilier());

        LifeCycleService lifeCycleService = serviceRegistry.get(LifeCycleService.class);
        lifeCycleService.onStart(() -> {
            log.info("Starting OSC Kubernetes Operator");
            operator.start();
        });
        lifeCycleService.onShutdown(() -> {
            log.info("Stopping OSC Kubernetes Operator");
            operator.stop();
        });
    }

}
