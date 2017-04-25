/*
Copyright 2009-2016 Igor Polevoy

Licensed under the Apache License, Version 2.0 (the "License"); 
you may not use this file except in compliance with the License. 
You may obtain a copy of the License at 

http://www.apache.org/licenses/LICENSE-2.0 

Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS, 
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
See the License for the specific language governing permissions and 
limitations under the License. 
*/


package org.javalite.activejdbc.test;

import java.io.IOException;
import java.util.Properties;

/**
 * @author Igor Polevoy
 */
public class JdbcProperties {

    private static final JdbcProperties JDBC_PROPERTIES = new JdbcProperties();


    private static String driver, url, user, password, db;


    private JdbcProperties() {
        try {
            Properties jdbcProperties = new Properties();
            jdbcProperties.load(getClass().getResourceAsStream("/jdbc.properties"));
            driver = jdbcProperties.getProperty("jdbc.driver");
            url = jdbcProperties.getProperty("jdbc.url");
            user = jdbcProperties.getProperty("jdbc.user");
            password = jdbcProperties.getProperty("jdbc.password");
            db = jdbcProperties.getProperty("db");
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String driver() {
        return driver;
    }

    public static String url() {
        return url;
    }

    public static String user() {
        return user;
    }

    public static String password() {
        return password;
    }
    public static String db(){
        return db;
    }
}
