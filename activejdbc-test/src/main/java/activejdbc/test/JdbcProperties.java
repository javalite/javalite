/*
Copyright 2009-2010 Igor Polevoy 

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


package activejdbc.test;

import java.util.Properties;

/**
 * @author Igor Polevoy
 */
public class JdbcProperties {

    private static final JdbcProperties JDBC_PROPERTIES = new JdbcProperties();


    private static String driver, url, user, password;


    private JdbcProperties(){
        try{
            Properties jdbcProperties = new Properties();
            String active_env = System.getenv("ACTIVE_ENV");
            String resource = active_env == null? "/jdbc.properties" :"/jdbc." + active_env + ".properties";
            jdbcProperties.load(getClass().getResourceAsStream(resource));
            driver = jdbcProperties.getProperty("jdbc.driver");
            url = jdbcProperties.getProperty("jdbc.url");
            user = jdbcProperties.getProperty("jdbc.user");
            password = jdbcProperties.getProperty("jdbc.password");
        }
        catch(Exception e){
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
}
