package com.lenyan.lenaiagent.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * 多数据源配置类
 * 同时支持MySQL和PostgreSQL数据源
 */
@Configuration
public class DataSourceConfig {
    // 绑定并创建 MySQL 数据源配置属性
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.mysql")
    public DataSourceProperties mysqlProperties() {
        return new DataSourceProperties();
    }

    // 使用 MySQL 配置属性创建 MySQL 数据源
    @Bean
    @Primary
    public DataSource mysqlDataSource() {
        return mysqlProperties()
        .initializeDataSourceBuilder()
        .build();
    }

    // 创建 MySQL 专用的 JdbcTemplate，供其他组件默认注入
    @Bean
    @Primary
    public JdbcTemplate mysqlJdbcTemplate(
        @Qualifier("mysqlDataSource") DataSource ds) {
        return new JdbcTemplate(ds);
    }

    // 绑定并创建 PostgreSQL 数据源配置属性
    @Bean
    @ConfigurationProperties("spring.datasource.postgres")
    public DataSourceProperties postgresProperties() {
        return new DataSourceProperties();
    }

    // 使用 PostgreSQL 配置属性创建 PostgreSQL 数据源
    @Bean
    public DataSource postgresDataSource() {
        return postgresProperties()
        .initializeDataSourceBuilder()
        .build();
    }

    // 创建 PostgreSQL 专用的 JdbcTemplate，供 PgVectorStore 使用
    @Bean
    public JdbcTemplate postgresJdbcTemplate(
        @Qualifier("postgresDataSource") DataSource ds) {
        return new JdbcTemplate(ds);
    }
}