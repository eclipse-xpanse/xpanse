package org.eclipse.osc.orchestrator.plugin.openstack;

import lombok.extern.slf4j.Slf4j;
import org.apache.karaf.minho.boot.Minho;
import org.apache.karaf.minho.boot.service.ConfigService;
import org.apache.karaf.minho.boot.service.LifeCycleService;
import org.eclipse.osc.modules.ocl.loader.Ocl;
import org.eclipse.osc.modules.ocl.loader.OclLoader;
import org.eclipse.osc.orchestrator.OrchestratorService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
public class OpenstackOrchestratorPluginTest {

    @Test
    @Disabled("Needs a working openstack instance")
    public void onRegisterTest() throws Exception {

        OpenstackOrchestratorPlugin plugin = new OpenstackOrchestratorPlugin();
        ConfigService configService = new ConfigService();
        Map<String, String> properties = new HashMap<>();
        properties.put("openstack.endpoint", "https://119.8.97.198:5000/v3");
        properties.put("openstack.secret", "openstack");
        properties.put("openstack.domainName", "Default");
        properties.put("openstack.enableSslCertificateValidation", "false");
        configService.setProperties(properties);
        Minho minho = Minho.builder()
                .loader(() -> Stream.of(configService, new LifeCycleService(), new OclLoader(), plugin, new SwiftOrchestratorStorage(), new OrchestratorService()))
                .build()
                .start();
        log.info("started");
        minho.getServiceRegistry().get(OrchestratorService.class).onRegister(minho.getServiceRegistry());
        OclLoader oclLoader = minho.getServiceRegistry().get(OclLoader.class);
        Ocl ocl = oclLoader.getOcl(new File("target/test-classes/kafka-test.json").toURI().toURL());
        plugin.registerManagedService(ocl);
    }
}
