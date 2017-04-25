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

package org.javalite.activejdbc;

import org.javalite.activejdbc.test_models.Person;
import com.mchange.v2.c3p0.DataSources;
import org.junit.Test;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;
import java.sql.SQLException;

import static org.javalite.test.jspec.JSpec.a;
import static org.javalite.activejdbc.test.JdbcProperties.*;

/**
 * Example of using c3p0 Pool 
 *
 * @author Igor Polevoy
 */
public class C3P0PoolTest  {

    @Test
    public void shouldUseConnectionFromPool() throws PropertyVetoException, SQLException, ClassNotFoundException {
        Class.forName(driver());
        DataSource dataSourceUnpooled = DataSources.unpooledDataSource(url(), user(), password());
        DataSource dataSourcePooled = DataSources.pooledDataSource(dataSourceUnpooled); //init the connection pool
        Base.open(dataSourcePooled); //get connection from pool
        Person.deleteAll(); //clean DB before test
        Person.createIt("name", "Matt", "last_name", "Diamont", "dob", "1962-01-01");
        a(Person.findAll().size()).shouldBeEqual(1);

        Person.deleteAll();//clean DB after test
        Base.close();// really connection goes back to pool
        DataSources.destroy(dataSourcePooled);//shut down the pool
    }
}
