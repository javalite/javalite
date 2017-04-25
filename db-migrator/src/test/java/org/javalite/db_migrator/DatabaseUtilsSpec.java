package org.javalite.db_migrator;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.javalite.db_migrator.DbUtils.*;

public class DatabaseUtilsSpec
{
    @Test
    public void shouldExtractDatabaseNames()
    {
        assertEquals(extractDatabaseName("jdbc:mysql:dbname"), "dbname");
        assertEquals(extractDatabaseName("jdbc:mysql://localhost/dbname"), "dbname");
        assertEquals(extractDatabaseName("jdbc:mysql://127.0.0.1/dbname"), "dbname");
        assertEquals(extractDatabaseName("jdbc:mysql://pants/a_b_c"), "a_b_c");
        assertEquals(extractDatabaseName("jdbc:mysql://pants/a-b-c"), "a-b-c");
        assertEquals(extractDatabaseName("jdbc:mysql://localhost:3306/dbname"), "dbname");
        assertEquals(extractDatabaseName("jdbc:mysql://127.0.0.1:3306/dbname"), "dbname");
        assertEquals(extractDatabaseName("jdbc:mysql://localhost/dbname;OPTION1=A;OPTION2=B"), "dbname");
        assertEquals(extractDatabaseName("jdbc:mysql://127.0.0.1:3306/dbname;OPTION1=A;OPTION2=B"), "dbname");
        assertEquals(extractDatabaseName("jdbc:mysql://localhost/dbname?OPTION1=A&OPTION2=B"), "dbname");
        assertEquals(extractDatabaseName("jdbc:mysql://127.0.0.1:3306/dbname?OPTION1=A&OPTION2=B"), "dbname");
    }

    @Test
    public void shouldExtractServerUrl()
    {
        assertEquals(extractServerUrl("jdbc:mysql:dbname"), "jdbc:mysql");
        assertEquals(extractServerUrl("jdbc:mysql://localhost/dbname"), "jdbc:mysql://localhost");
        assertEquals(extractServerUrl("jdbc:mysql://pants/a_b_c"), "jdbc:mysql://pants");
        assertEquals(extractServerUrl("jdbc:mysql://pants/a-b-c"), "jdbc:mysql://pants");
        assertEquals(extractServerUrl("jdbc:mysql://localhost/dbname"), "jdbc:mysql://localhost");
        assertEquals(extractServerUrl("jdbc:mysql://localhost:3306/dbname"), "jdbc:mysql://localhost:3306");
        assertEquals(extractServerUrl("jdbc:mysql://127.0.0.1:3306/dbname"), "jdbc:mysql://127.0.0.1:3306");
        assertEquals(extractServerUrl("jdbc:mysql://localhost/dbname;OPTION1=A;OPTION2=B"), "jdbc:mysql://localhost;OPTION1=A;OPTION2=B");
        assertEquals(extractServerUrl("jdbc:mysql://127.0.0.1:3306/dbname;OPTION1=A;OPTION2=B"), "jdbc:mysql://127.0.0.1:3306;OPTION1=A;OPTION2=B");
        assertEquals(extractServerUrl("jdbc:mysql://localhost/dbname?OPTION1=A&OPTION2=B"), "jdbc:mysql://localhost?OPTION1=A&OPTION2=B");
        assertEquals(extractServerUrl("jdbc:mysql://127.0.0.1:3306/dbname?OPTION1=A&OPTION2=B"), "jdbc:mysql://127.0.0.1:3306?OPTION1=A&OPTION2=B");
    }
}
