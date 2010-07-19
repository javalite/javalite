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

import java.sql.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import activejdbc.Base;
import javalite.test.jspec.JSpecSupport;

import org.junit.After;
import org.junit.Before;

import static activejdbc.test.JdbcProperties.*;


public abstract class  ActiveJDBCTest extends JSpecSupport {


    @Before
    public void before() throws Exception {
        Base.open(driver(), url(), user(), password());
        resetDB();
    }

    @After
    public void after(){
        Base.close();
    }

    protected void resetDB() throws SQLException, ClassNotFoundException {
        Connection connection = Base.connection();
        String[] statements = getStatements(";");
        for (String statement: statements) {
            Statement st = connection.createStatement();
            st.executeUpdate(statement);
            st.close();
        }
    }


     /**
     * Returns array of strings where text was separated by semi-colons.
     * @return array of strings where text was separated by semi-colons.
     */
    public String[] getStatements(String delimiter){
        try{
            InputStreamReader  isr = new InputStreamReader(ActiveJDBCTest.class.getClassLoader().getResourceAsStream("populate_schema.sql"));

            BufferedReader reader = new BufferedReader(isr);
            StringBuffer  text = new  StringBuffer();
            String t;

            while((t = reader.readLine()) != null){
                if( t.startsWith("--")) continue;
                text.append(t);
            }
            return text.toString().split(delimiter);
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }
}
