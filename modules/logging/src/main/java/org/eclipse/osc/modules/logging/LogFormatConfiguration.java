package org.eclipse.osc.modules.logging;

import lombok.extern.slf4j.Slf4j;
import org.apache.karaf.minho.boot.service.ConfigService;
import org.apache.karaf.minho.boot.service.ServiceRegistry;
import org.apache.karaf.minho.boot.spi.Service;

import java.io.IOException;
import java.util.Objects;
import java.util.logging.LogManager;

@Slf4j
public class LogFormatConfiguration implements Service {

    private static final String LOG_FORMAT_CONFIG_NAME = "osc.log.format";
    private static final String JAVA_UTIL_LOGGING_FORMAT_CONFIG = "java.util.logging.SimpleFormatter.format";

    @Override
    public String name() {
        return "osc-logging-configuration";
    }

    @Override
    public int priority() {
        return 1;
    }

    @Override
    public void onRegister(ServiceRegistry serviceRegistry) throws IOException {
        log.info("Registering log configuration");
        if (Objects.isNull(serviceRegistry)) {
            throw new IllegalStateException("ServiceRegistry is null");
        }
        ConfigService configService = serviceRegistry.get(ConfigService.class);
        if (Objects.isNull(configService)) {
            throw new IllegalStateException("Config service is not present in the registry");
        }
        if (Objects.nonNull(configService.getProperty(LOG_FORMAT_CONFIG_NAME))) {
            // Since the logger is already initialized by Minho, it is necessary to reset the previous configurations
            LogManager.getLogManager().reset();
            System.setProperty(JAVA_UTIL_LOGGING_FORMAT_CONFIG, configService.getProperty(LOG_FORMAT_CONFIG_NAME));
            LogManager.getLogManager().readConfiguration();
        } else {
            log.warn("No logging config found. Using the Minho default config.");
        }
    }
}
