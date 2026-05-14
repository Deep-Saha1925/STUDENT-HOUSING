package com.deep.studenthousing.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

//    MYSQL
    @Bean(name = "localDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.local")
    public DataSource localDataSource(){
        return DataSourceBuilder.create().build();
    }

//    CLOUD POSTGRES
    @Bean(name = "cloudDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.cloud")
    public DataSource cloudDataSource(){
        return DataSourceBuilder.create().build();
    }

}
