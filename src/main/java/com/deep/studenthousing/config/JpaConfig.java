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

    // LOCAL EntityManager (MySQL)
    @Bean(name = "localEntityManagerFactory")
    @Primary
    public LocalContainerEntityManagerFactoryBean localEntityManagerFactory(
            @Qualifier("localDataSource") DataSource dataSource) {

        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.deep.studenthousing.entity");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        Properties props = new Properties();
        props.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        props.put("hibernate.hbm2ddl.auto", "update");
        props.put("hibernate.show_sql", "true");
        em.setJpaProperties(props);

        return em;
    }

    // CLOUD EntityManager (PostgreSQL - Neon)
    @Bean(name = "cloudEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean cloudEntityManagerFactory(
            @Qualifier("cloudDataSource") DataSource dataSource) {

        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.deep.studenthousing.entity");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        Properties props = new Properties();
        props.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        props.put("hibernate.hbm2ddl.auto", "update");
        props.put("hibernate.show_sql", "true");
        em.setJpaProperties(props);

        return em;
    }

    // LOCAL Transaction Manager
    @Bean(name = "localTransactionManager")
    @Primary
    public PlatformTransactionManager localTransactionManager(
            @Qualifier("localEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

    // CLOUD Transaction Manager
    @Bean(name = "cloudTransactionManager")
    public PlatformTransactionManager cloudTransactionManager(
            @Qualifier("cloudEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}