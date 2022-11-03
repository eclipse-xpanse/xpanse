package org.eclipse.osc.orchestrator.plugin.openstack;

import lombok.extern.java.Log;
import org.apache.karaf.minho.boot.service.ConfigService;
import org.apache.karaf.minho.boot.service.ServiceRegistry;
import org.apache.karaf.minho.boot.spi.Service;
import org.eclipse.osc.orchestrator.OrchestratorPlugin;
import org.eclipse.osc.services.ocl.loader.Ocl;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

@Log
public class OpenstackOrchestratorPlugin implements OrchestratorPlugin, Service {

    private String authenticationUrl;
    private String authenticationUsername;
    private String authenticationPassword;
    private String authenticationDomain;
    private String authenticationProject;

    @Override
    public String name() {
        return "osc-orchestrator-openstack";
    }

    @Override
    public void onRegister(ServiceRegistry serviceRegistry) {
        ConfigService configService = serviceRegistry.get(ConfigService.class);
        if (configService == null) {
            throw new IllegalStateException("Config service is not present in the registry");
        }
        // typically authentication.url should look like https://foo/v3/auth/tokens
        if (configService.getProperty("authentication.url") == null) {
            throw new IllegalStateException("authentication.url is not present in config service");
        }
        authenticationUrl = configService.getProperty("authentication.url");
        if (configService.getProperty("authentication.username") == null) {
            throw new IllegalStateException("authentication.username is not present in config service");
        }
        authenticationUsername = configService.getProperty("authentication.username");
        if (configService.getProperty("authentication.password") == null) {
            throw new IllegalStateException("authentication.password is not present in config service");
        }
        authenticationPassword = configService.getProperty("authentication.password");
        if (configService.getProperty("authentication.domain") == null) {
            throw new IllegalStateException("authentication.domain is not present in config service");
        }
        authenticationDomain = configService.getProperty("authentication.domain");
        if (configService.getProperty("authentication.project") == null) {
            throw new IllegalStateException("authentication.project is not present in config service");
        }
        authenticationProject = configService.getProperty("authentication.project");
    }

    /**
     * Authenticate on the openstack URL and get the token
     *
     * @return the token
     * @throws Exception if the authentcation fails
     */
    private String authenticate() throws Exception {
        log.info("Authenticate on " + authenticationUrl);
        // TODO support proxy
        HttpURLConnection connection = (HttpURLConnection) new URL(authenticationUrl).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoInput(true);
        connection.setDoOutput(true);
        String authJson = "{ \"auth\": { \"identity\": { \"methods\": [\"password\"], \"password\": { \"user\": { \"name\": \""
                + authenticationUsername + "\", \"domain\": { \"name\": \""
                + authenticationDomain + "\" }, \"password\": \""
                + authenticationPassword + "\" }}}, \"scope\": { \"project\": { \"id\": \""
                + authenticationProject + "\" }}}}";
        log.fine("Authentication JSON request:");
        log.fine(authJson);
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()))) {
            writer.write(authJson);
            writer.flush();
        }
        if (connection.getResponseCode() != 201) {
            throw new IllegalStateException("Can't get token (" + connection.getResponseCode() + "): " + connection.getResponseMessage());
        }
        return connection.getHeaderField("X-Subject-Token");
    }

    @Override
    public void registerManagedService(Ocl ocl) {
        log.info("Register managed service, creating openstack resource");
    }

    @Override
    public void updateManagedService(String managedServiceName, Ocl ocl) {
        log.info("Updating managed service " + managedServiceName + " on openstack");
    }

    @Override
    public void startManagedService(String managedServiceName) {
        log.info("Start managed service " + managedServiceName + " on openstack");
    }

    @Override
    public void stopManagedService(String managedServiceName) {
        log.info("Stop managed service " + managedServiceName + " on openstack");
    }

    @Override
    public void unregisterManagedService(String managedServiceName) {
        log.info("Destroy managed service " + managedServiceName + " from openstack");
    }
}
