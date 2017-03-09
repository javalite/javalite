package org.javalite.activejdbc;

import org.javalite.test.jspec.JSpecSupport;
import org.junit.Test;

import static org.javalite.activejdbc.test.JdbcProperties.*;

/**
 * @author Igor Polevoy
 */
public class CloseConnectionsTest implements JSpecSupport {

    @Test
    public void shouldCloseAllConnections(){

    	
        new DB("conection1").open(driver(), url(), user(), password());
        new DB("conection2").open(driver(), url(), user(), password());

        a(DB.getCurrrentConnectionNames().size()).shouldBeEqual(2);

        DB.closeAllConnections();

        a(DB.getCurrrentConnectionNames().size()).shouldBeEqual(0);
    }

    @Test
    public void shouldTryDBWithResources(){
        try(DB db = new DB().open()){
            a(DB.getCurrrentConnectionNames().size()).shouldBeEqual(1);
        }
        a(DB.getCurrrentConnectionNames().size()).shouldBeEqual(0);
    }

    @Test
    public void shouldTryBaseWithResources(){
        try(DB db = Base.open()){
            a(DB.getCurrrentConnectionNames().size()).shouldBeEqual(1);
        }
        a(DB.getCurrrentConnectionNames().size()).shouldBeEqual(0);
    }
}
