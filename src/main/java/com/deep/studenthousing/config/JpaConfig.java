package com.deep.studenthousing.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
public class JpaConfig {

//    LOCAL EntityManager
    @Bean(name = "localEntityManagerFactory")
    @Primary
    public LocalContainerEntityManagerFactoryBean localEntityManagerFactory(
            @Qualifier("localDataSource")DataSource dataSource
    ){
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.deep.studenthousing.entity");

        HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(adapter);

        Properties properties = new Properties();
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("hibernate.show_sql", "true");
        em.setJpaProperties(properties);

        return em;
    }

//    CLOUD EntityManager
    @Bean(name = "cloudEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean cloudEntityManagerFactory(
            @Qualifier("cloudDataSource")DataSource dataSource
    ){
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.deep.studenthousing.entity");

        HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(adapter);

        Properties props = new Properties();
        props.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        props.put("hibernate.hbm2ddl.auto", "update");
        props.put("hibernate.show_sql", "true");
        em.setJpaProperties(props);

        return em;
    }

//    Transaction Managers
    @Bean(name = "localTransactionManager")
    @Primary
    public PlatformTransactionManager localTransactionManager(
            @Qualifier("localEntityManagerFactory")EntityManagerFactory emf
    ){
        return new JpaTransactionManager(emf);
    }

}
