/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.workflow.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.util.Objects;
import javax.sql.DataSource;
import org.activiti.engine.DynamicBpmnService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.persistence.StrongUuidGenerator;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

/** Activiti configuration class. */
@RefreshScope
@Configuration
public class ActivitiConfig {

    private final ApplicationContext applicationContext;

    private final ActivitiProperties activitiProperties;

    /** constructor for ActivitiConfig bean. */
    @Autowired
    public ActivitiConfig(
            ApplicationContext applicationContext, ActivitiProperties activitiProperties) {
        this.applicationContext = applicationContext;
        this.activitiProperties = activitiProperties;
    }

    @Bean(name = "activitiDataSource")
    @ConfigurationProperties(prefix = "spring.activiti.datasource")
    public DataSource activitiDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "activitiTransactionManager")
    public PlatformTransactionManager activitiTransactionManager() {
        return new DataSourceTransactionManager(activitiDataSource());
    }

    /** Create ProcessEngineConfiguration object into SpringIoc. */
    @Bean
    public ProcessEngineConfiguration processEngineConfiguration() throws IOException {
        SpringProcessEngineConfiguration springProcessEngineConfiguration =
                new SpringProcessEngineConfiguration();
        springProcessEngineConfiguration.setIdGenerator(strongUuidGenerator());
        springProcessEngineConfiguration.setDataSource(activitiDataSource());
        springProcessEngineConfiguration.setTransactionManager(activitiTransactionManager());
        springProcessEngineConfiguration.setDatabaseSchemaUpdate("true");
        springProcessEngineConfiguration.setDeploymentMode("single-resource");
        Resource[] resources =
                new PathMatchingResourcePatternResolver()
                        .getResources(
                                ResourceLoader.CLASSPATH_URL_PREFIX + "processes/**.bpmn20.xml");
        springProcessEngineConfiguration.setDeploymentResources(resources);
        springProcessEngineConfiguration.setHistoryLevel(
                HistoryLevel.getHistoryLevelForKey(activitiProperties.getHistoryLevel()));
        springProcessEngineConfiguration.setApplicationContext(this.applicationContext);
        springProcessEngineConfiguration.setSerializePOJOsInVariablesToJson(true);
        return springProcessEngineConfiguration;
    }

    @Bean
    public StrongUuidGenerator strongUuidGenerator() {
        return new StrongUuidGenerator();
    }

    /**
     * Creates ProcessEngineFactoryBean which automatically joins the spring-boot application
     * context to activiti.
     */
    @Bean
    public ProcessEngineFactoryBean processEngine() throws IOException {
        ProcessEngineFactoryBean factoryBean = new ProcessEngineFactoryBean();
        factoryBean.setProcessEngineConfiguration(
                (SpringProcessEngineConfiguration) processEngineConfiguration());
        return factoryBean;
    }

    @Bean
    public RepositoryService repositoryService() throws Exception {
        return Objects.requireNonNull(processEngine().getObject()).getRepositoryService();
    }

    @Bean
    public RuntimeService runtimeService() throws Exception {
        return Objects.requireNonNull(processEngine().getObject()).getRuntimeService();
    }

    @Bean
    public TaskService taskService() throws Exception {
        return Objects.requireNonNull(processEngine().getObject()).getTaskService();
    }

    @Bean
    public HistoryService historyService() throws Exception {
        return Objects.requireNonNull(processEngine().getObject()).getHistoryService();
    }

    @Bean
    public ManagementService managementService() throws Exception {
        return Objects.requireNonNull(processEngine().getObject()).getManagementService();
    }

    @Bean
    public DynamicBpmnService dynamicBpmnService() throws Exception {
        return Objects.requireNonNull(processEngine().getObject()).getDynamicBpmnService();
    }

    /** Assembling the ObjectMapper object. */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }
}
