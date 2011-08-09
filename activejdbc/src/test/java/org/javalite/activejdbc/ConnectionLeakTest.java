package org.javalite.activejdbc;

import org.javalite.test.jspec.JSpecSupport;
import org.junit.Test;

/**
 * @author Igor Polevoy
 */
public class ConnectionLeakTest extends JSpecSupport{

    @Test
    public void shouldThrowExceptionIfConnectionOpenedWithoutClosingPrevious(){
        //Commented by Kadvin, in transaction management mode, we won't open multiple connection
        //this case is deprecated
//        expect(new ExceptionExpectation(InitException.class) {
//            @Override
//            public void exec() {
//                Base.open("com.mysql.jdbc.Driver", "jdbc:mysql://localhost/activejdbc", "root", "p@ssw0rd");
//                Base.open("com.mysql.jdbc.Driver", "jdbc:mysql://localhost/activejdbc", "root", "p@ssw0rd");
//            }
//        });
//
//        Base.close();
    }
}
