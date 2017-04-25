package org.javalite.activejdbc;

import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.javalite.activejdbc.test.JdbcProperties.*;
import static org.javalite.test.jspec.JSpec.a;

/**
 * @author  igor on 7/16/14.
 */
public class AttachDetachTest {

    @Test
    public void shouldAttachDetachConnection() throws SQLException, ClassNotFoundException {
        Class.forName(driver());
        Connection connection = DriverManager.getConnection(url(), user(), password());
        Base.attach(connection);
        a(Base.connection()).shouldNotBeNull();
        Connection c = Base.detach();
        a(c).shouldNotBeNull();
        c.close();

        try{
            Base.connection();
        }catch(Exception e){
            a(e.getMessage()).shouldBeEqual("there is no connection 'default' on this thread, are you sure you opened it?");
        }

    }
}
