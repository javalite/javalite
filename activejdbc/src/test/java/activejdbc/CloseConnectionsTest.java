package activejdbc;

import static activejdbc.test.JdbcProperties.driver;
import static activejdbc.test.JdbcProperties.password;
import static activejdbc.test.JdbcProperties.url;
import static activejdbc.test.JdbcProperties.user;
import javalite.test.jspec.JSpecSupport;
import org.junit.Test;

/**
 * @author Igor Polevoy
 */
public class CloseConnectionsTest extends JSpecSupport {

    @Test
    public void shouldCloseAllConnections(){

    	
        new DB("conection1").open(driver(), url(), user(), password());
        new DB("conection2").open(driver(), url(), user(), password());

        a(DB.getCurrrentConnectionNames().size()).shouldBeEqual(2);

        DB.closeAllConnections();

        a(DB.getCurrrentConnectionNames().size()).shouldBeEqual(0);

    }
}
