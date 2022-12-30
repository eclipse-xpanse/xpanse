package org.eclipse.osc.services.api;

import org.apache.karaf.minho.boot.Minho;
import org.apache.karaf.minho.boot.service.ConfigService;
import org.apache.karaf.minho.boot.service.LifeCycleService;
import org.apache.karaf.minho.rest.jersey.JerseyRestService;
import org.apache.karaf.minho.web.jetty.JettyWebContainerService;
import org.eclipse.osc.orchestrator.OrchestratorService;
import org.eclipse.osc.orchestrator.plugin.huaweicloud.HuaweiCloudOrchestratorPlugin;
import org.eclipse.osc.services.ocl.loader.OclLoader;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static java.lang.Thread.sleep;

public class OscServerStarter {
    public static void main(String[] args) throws InterruptedException {
        ConfigService configService = new ConfigService();
        Map<String, String> properties = new HashMap<>();
        properties.put("rest.packages", "org.eclipse.osc.services.api");
        properties.put("rest.path", "/osc/*");
        properties.put("orchestrator.store.filename", "target/test-classes/orchestrator.properties");
        configService.setProperties(properties);

        OrchestratorService orchestratorService = new OrchestratorService();

        Minho minho = Minho.builder().loader(() -> Stream.of(
                configService,
                new LifeCycleService(),
                new OclLoader(),
                orchestratorService,
                new HuaweiCloudOrchestratorPlugin(),
                new JettyWebContainerService(),
                new JerseyRestService()
        )).build().start();

        while (true){
            sleep(10);
        }
    }
}
