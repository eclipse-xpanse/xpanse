package org.eclipse.osc.modules.api;

import org.apache.karaf.minho.boot.Minho;
import org.apache.karaf.minho.boot.service.ConfigService;
import org.apache.karaf.minho.boot.service.LifeCycleService;
import org.apache.karaf.minho.rest.jersey.JerseyRestService;
import org.apache.karaf.minho.web.jetty.JettyWebContainerService;
import org.eclipse.osc.orchestrator.OrchestratorService;
import org.eclipse.osc.modules.ocl.loader.Ocl;
import org.eclipse.osc.modules.ocl.loader.OclLoader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class OrchestratorApiTest {

    @Test
    public void healthOK() throws Exception {
        URL url = new URL("http://localhost:8080/osc/health");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setDoOutput(true);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line = reader.readLine();
            Assertions.assertEquals(200, connection.getResponseCode());
            Assertions.assertEquals("ready", line);
        }
    }

    @Test
    public void register() throws Exception {
        URL url = new URL("http://localhost:8080/osc/register");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        connection.setDoInput(true);

        String ocl = "{ \"name\": \"test\" }";

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()))) {
            writer.write(ocl);
            writer.flush();
        }

        Assertions.assertEquals("OK", connection.getResponseMessage());
        Assertions.assertEquals(200, connection.getResponseCode());
    }

    @Test
    public void registerFetch() throws Exception {
        URL url = new URL("http://localhost:8080/osc/register/fetch");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("ocl", new File("target/test-classes/test.json").toURI().toURL().toString());
        connection.setDoOutput(true);

        Assertions.assertEquals("OK", connection.getResponseMessage());
        Assertions.assertEquals(200, connection.getResponseCode());
    }

    @Test
    public void start() throws Exception {
        OrchestratorService orchestratorService = Minho.getInstance().getServiceRegistry().get(OrchestratorService.class);

        Ocl ocl = new Ocl();
        ocl.setName("test");
        orchestratorService.registerManagedService(ocl);

        URL url = new URL("http://localhost:8080/osc/start");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("managedServiceName", "test");

        Assertions.assertEquals("OK", connection.getResponseMessage());
        Assertions.assertEquals(200, connection.getResponseCode());
    }

    @Test
    public void stop() throws Exception {
        OrchestratorService orchestratorService = Minho.getInstance().getServiceRegistry().get(OrchestratorService.class);

        Ocl ocl = new Ocl();
        ocl.setName("test");
        orchestratorService.registerManagedService(ocl);

        URL url = new URL("http://localhost:8080/osc/stop");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("managedServiceName", "test");

        Assertions.assertEquals("OK", connection.getResponseMessage());
        Assertions.assertEquals(200, connection.getResponseCode());
    }

    @Test
    public void state() throws Exception {
        OrchestratorService orchestratorService = Minho.getInstance().getServiceRegistry()
            .get(OrchestratorService.class);

        Ocl ocl = new Ocl();
        ocl.setName("test");
        orchestratorService.registerManagedService(ocl);

        URL url = new URL("http://localhost:8080/osc/services/state/test");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        Assertions.assertEquals("OK", connection.getResponseMessage());
        Assertions.assertEquals(200, connection.getResponseCode());
    }

    @BeforeEach
    private void setup() throws Exception {
        ConfigService configService = new ConfigService();
        Map<String, String> properties = new HashMap<>();
        properties.put("rest.packages", "org.eclipse.osc.modules.api;io.swagger.v3.jaxrs2.integration.resources");
        properties.put("rest.path", "/osc/*");
        properties.put("orchestrator.store.filename", "target/test-classes/orchestrator.properties");
        configService.setProperties(properties);

        OrchestratorService orchestratorService = new OrchestratorService();

        Minho minho = Minho.builder().loader(() -> Stream.of(
                configService,
                new LifeCycleService(),
                new OclLoader(),
                orchestratorService,
                new JettyWebContainerService(),
                new JerseyRestService()
        )).build().start();
    }

    @AfterEach
    private void teardown() throws Exception {
        Minho.getInstance().close();
    }

}
