/*
Copyright 2009-2019 Igor Polevoy

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


package org.javalite.activejdbc;

import static org.javalite.activejdbc.test.JdbcProperties.*;

import org.javalite.activejdbc.connection_config.ConnectionJdbcConfig;
import org.javalite.activejdbc.connection_config.ConnectionJndiConfig;
import org.javalite.activejdbc.connection_config.ConnectionConfig;
import org.javalite.activejdbc.connection_config.DbConfiguration;
import org.junit.Test;

import javax.naming.NamingException;
import java.sql.SQLException;
import java.util.Properties;
import static org.javalite.test.jspec.JSpec.a;


/**
 * @author Igor Polevoy
 */
public class ConnectionConfigSpec {

    @Test
    public void testJdbc() {
        ConnectionJdbcConfig spec = new ConnectionJdbcConfig(driver(), url(), user(), password());
        jdbcWithSpec(spec);
    }
    @Test
    public void testJdbcWithProperties() {

        Properties p = new Properties();
        p.setProperty("user", user());
        p.setProperty("password", password());
        ConnectionJdbcConfig spec = new ConnectionJdbcConfig(driver(), url(), p);
        jdbcWithSpec(spec);
    }

    private void jdbcWithSpec(ConnectionConfig spec){
    
        DB db = new DB("default");
        db.open(spec);

        a(db.connection()).shouldNotBeNull();

        db.close();
        DBException e = null;
        try {
            db.connection();
        }
        catch (DBException ex) {
            e = ex;
        }
        a(e).shouldNotBeNull();
    }

    @Test
    public void testJndi()  {

        ConnectionJndiConfig spec = new ConnectionJndiConfig("java/jdbc/DefaultDS");
        DB db = new DB("default");
        db.open(spec);

        a(db.connection()).shouldNotBeNull();

        db.close();
        DBException e = null;
        try {
            db.connection();
        }
        catch (DBException ex) {
            e = ex;
        }
        a(e).shouldNotBeNull();
    }

    /**
     * This test should only be executed under mysql Maven profile because
     * the database.properties file contains
     */
    @Test
    public void shouldOpenConnectionFromPropertiesFile(){
        DbConfiguration.loadConfiguration("/database.properties");
        DB db = new DB("default");
        db.open();
        a(db.connection()).shouldNotBeNull();
        db.close();
    }
}