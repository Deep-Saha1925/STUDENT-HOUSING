package com.deep.studenthousing.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

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

    @Bean(name = "routingDataSource")
    @Primary
    public DataSource routeDataSource(
            @Qualifier("localDataSource") DataSource local,
            @Qualifier("cloudDataSource") DataSource cloud
    ){
        return new AbstractRoutingDataSource() {

            @Override
            protected Object determineCurrentLookupKey() {
                return null; // not used, we override getConnection
            }

            @Override
            public Connection getConnection() throws SQLException {
                // Spring JPA uses this — returns local connection
                // Cloud write happens via DualWriteAspect below
                return local.getConnection();
            }

            @Override
            public Connection getConnection(String u, String p) throws SQLException {
                return local.getConnection(u, p);
            }
        };
    }

}
