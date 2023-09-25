package org.eclipse.xpanse.modules.nflow.config;

import io.nflow.engine.config.NFlow;
import io.nflow.engine.config.db.H2DatabaseConfiguration;
import javax.sql.DataSource;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class NflowDatabaseConfig extends H2DatabaseConfiguration {

    @Override
    @Bean
    @NFlow
    public DataSource nflowDatasource(Environment env, BeanFactory appCtx) {
        return appCtx.getBean("hikariDatasource", DataSource.class);
    }
}
