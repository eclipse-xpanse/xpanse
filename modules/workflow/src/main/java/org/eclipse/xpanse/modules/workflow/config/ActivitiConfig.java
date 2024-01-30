/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.workflow.config;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
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

    private final DataSource dataSource;

    private final TransactionManager transactionManager;

    private final ApplicationContext applicationContext;

    @Value("${spring.activiti.history-level}")
    private HistoryLevel historyLevel;

    /**
     * constructor for ActivitiConfig bean.
     */
    @Autowired
    public ActivitiConfig(DataSource dataSource, TransactionManager transactionManager,
                          ApplicationContext applicationContext) {
        this.dataSource = dataSource;
        this.transactionManager = transactionManager;
        this.applicationContext = applicationContext;
    }

    /**
     * Create ProcessEngineConfiguration object into SpringIoc.
     */
    @Bean
    public ProcessEngineConfiguration processEngineConfiguration() throws IOException {
        SpringProcessEngineConfiguration springProcessEngineConfiguration =
                new SpringProcessEngineConfiguration();
        springProcessEngineConfiguration.setIdGenerator(strongUuidGenerator());
        springProcessEngineConfiguration.setDataSource(dataSource);
        springProcessEngineConfiguration.setTransactionManager(
                (PlatformTransactionManager) transactionManager);
        springProcessEngineConfiguration.setDatabaseSchemaUpdate("true");
        springProcessEngineConfiguration.setDeploymentMode("single-resource");
        Resource[] resources = new PathMatchingResourcePatternResolver().getResources(
                ResourceLoader.CLASSPATH_URL_PREFIX + "processes/**.bpmn20.xml");
        springProcessEngineConfiguration.setDeploymentResources(resources);
        springProcessEngineConfiguration.setHistoryLevel(historyLevel);
        springProcessEngineConfiguration.setApplicationContext(this.applicationContext);
        return springProcessEngineConfiguration;
    }

    @Bean
    public StrongUuidGenerator strongUuidGenerator() {
        return new StrongUuidGenerator();
    }

    /**
     * Creates ProcessEngineFactoryBean which automatically joins the spring-boot
     * application context to activiti.
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

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

}
