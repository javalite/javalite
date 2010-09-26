package activejdbc;

import javalite.test.jspec.JSpecSupport;
import org.junit.Test;

/**
 * @author Igor Polevoy
 */
public class CloseConnectionsTest extends JSpecSupport {

    @Test
    public void shouldCloseAllConnections(){


        new DB("conection1").open("com.mysql.jdbc.Driver", "jdbc:mysql://localhost/test", "root", "p@ssw0rd");
        new DB("conection2").open("com.mysql.jdbc.Driver", "jdbc:mysql://localhost/test", "root", "p@ssw0rd");

        a(DB.getCurrrentConnectionNames().size()).shouldBeEqual(2);

        DB.closeAllConnections();

        a(DB.getCurrrentConnectionNames().size()).shouldBeEqual(0);

    }
}
