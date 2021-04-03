package com.tradebot.bitmex.config;

import com.tradebot.bitmex.restapi.account.transaction.BitmexTransactionDataProviderService;
import com.tradebot.bitmex.restapi.instrument.BitmexInstrumentDataProviderService;
import com.tradebot.core.account.transaction.TransactionDataProvider;
import com.tradebot.core.instrument.InstrumentService;
import com.tradebot.core.model.OperationResultCallback;
import com.tradebot.core.model.OperationResultContext;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class TestDatasourceConfig {

    private final InstrumentService instrumentService;

    public TestDatasourceConfig(@Lazy InstrumentService instrumentService) {
        this.instrumentService = instrumentService;
    }

    @Bean
    public DataSource dataSource() {
        EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
        return builder
            .setType(EmbeddedDatabaseType.H2)
            .generateUniqueName(true)
            .addScript("schema.sql")
            .addScript("data.sql")
            .build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean entityManagerFactory = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactory.setDataSource(dataSource);
        entityManagerFactory.setPackagesToScan("com.tradebot.bitmex.model");
        JpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
        entityManagerFactory.setJpaVendorAdapter(jpaVendorAdapter);
        return entityManagerFactory;
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        return transactionManager;
    }

    @Bean
    public InstrumentService instrumentService() {
        return new InstrumentService(new BitmexInstrumentDataProviderService(), operationResultContext -> {

        });
    }

    @Bean
    public TransactionDataProvider<String, Long> transactionDataProvider() {
        return new BitmexTransactionDataProviderService(instrumentService);
    }
}
