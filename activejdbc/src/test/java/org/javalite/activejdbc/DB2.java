package org.javalite.activejdbc;

import org.junit.Test;

public class DB2 {

    @Test
    public void should(){


        Base.open("com.ibm.db2.jcc.DB2Driver", "jdbc:db2://0.0.0.0:50000/db2", "db2inst1", "p@ssw0rd");

    }

}
