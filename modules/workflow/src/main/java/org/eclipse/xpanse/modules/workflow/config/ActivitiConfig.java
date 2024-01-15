/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.workflow.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import javax.sql.DataSource;
import org.activiti.engine.DynamicBpmnService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.persistence.StrongUuidGenerator;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionManager;

/**
 * Activiti configuration class.
 */
@Configuration
public class ActivitiConfig {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private TransactionManager transactionManager;

    /**
     * Create ProcessEngineConfiguration object into SpringIoc.
     */
    @Bean
    public ProcessEngineConfiguration processEngineConfiguration() throws IOException {
        SpringProcessEngineConfiguration processEngineConfiguration =
                new SpringProcessEngineConfiguration();
        processEngineConfiguration.setIdGenerator(strongUuidGenerator());
        processEngineConfiguration.setDataSource(dataSource);
        processEngineConfiguration.setTransactionManager(
                (PlatformTransactionManager) transactionManager);
        processEngineConfiguration.setDatabaseSchemaUpdate("true");
        processEngineConfiguration.setDeploymentMode("single-resource");
        Resource[] resources = new PathMatchingResourcePatternResolver().getResources(
                ResourceLoader.CLASSPATH_URL_PREFIX + "processes/**.bpmn");
        processEngineConfiguration.setDeploymentResources(resources);
        processEngineConfiguration.setHistoryLevel(HistoryLevel.NONE);
        return processEngineConfiguration;
    }

    @Bean
    public StrongUuidGenerator strongUuidGenerator() {
        return new StrongUuidGenerator();
    }

    @Bean
    public ProcessEngine processEngine() throws IOException {
        return processEngineConfiguration().buildProcessEngine();
    }

    @Bean
    public RepositoryService repositoryService() throws IOException {
        return processEngine().getRepositoryService();
    }

    @Bean
    public RuntimeService runtimeService() throws IOException {
        return processEngine().getRuntimeService();
    }

    @Bean
    public TaskService taskService() throws IOException {
        return processEngine().getTaskService();
    }

    @Bean
    public HistoryService historyService() throws IOException {
        return processEngine().getHistoryService();
    }

    @Bean
    public ManagementService managementService() throws IOException {
        return processEngine().getManagementService();
    }

    @Bean
    public DynamicBpmnService dynamicBpmnService() throws IOException {
        return processEngine().getDynamicBpmnService();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }


}
