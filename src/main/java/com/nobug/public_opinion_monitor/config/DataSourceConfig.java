package com.nobug.public_opinion_monitor.config;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * 数据源配置
 *
 * @date：2023/2/8
 * @author：nobug
 */
@Configuration
@Primary
public class DataSourceConfig {

    @Value("${spring.datasource.druid.url}")
    private String datasourceUrl;
    @Value("${spring.datasource.druid.driver-class-name}")
    private String driverClassName;
    @Value("${spring.datasource.druid.username}")
    private String username;
    @Value("${spring.datasource.druid.password}")
    private String password;

    //声明其为Bean实例
    @Bean
    public DataSource dataSource(){
        // 1、创建DataSource对象
        DruidDataSource dataSource = new DruidDataSource();
        // 2、设置属性
        dataSource.setUrl(datasourceUrl);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName(driverClassName);

        try{
            // 3、加载驱动
            Class.forName(driverClassName);
            String url01 = datasourceUrl.substring(0,datasourceUrl.indexOf("?"));

            String url02 = url01.substring(0,url01.lastIndexOf("/"))+datasourceUrl.substring(datasourceUrl.indexOf("?"));
            System.out.println(url02);

            String datasourceName = url01.substring(url01.lastIndexOf("/")+1);
            // 连接已经存在的数据库，如：mysql
            Connection connection = DriverManager.getConnection(url02, username, password);
            Statement statement = connection.createStatement();
            // 创建数据库
            statement.executeUpdate("create database if not exists `" + datasourceName + "` default character set utf8 COLLATE utf8_general_ci");

            statement.close();
            connection.close();
        }catch (Exception e){
            e.printStackTrace();
        }

        return dataSource;

    }

}
