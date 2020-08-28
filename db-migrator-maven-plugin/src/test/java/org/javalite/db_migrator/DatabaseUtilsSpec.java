package org.javalite.db_migrator;

import org.junit.Test;

import static org.javalite.test.jspec.JSpec.the;
import static org.junit.Assert.*;
import static org.javalite.db_migrator.DbUtils.*;

public class DatabaseUtilsSpec
{
    @Test
    public void shouldExtractDatabaseNames()    {
        the(extractDatabaseName("jdbc:mysql:dbname")).shouldEqual("dbname");
        the(extractDatabaseName("jdbc:mysql://localhost/dbname")).shouldBeEqual("dbname");
        the(extractDatabaseName("jdbc:mysql://127.0.0.1/dbname")).shouldBeEqual("dbname");
        the(extractDatabaseName("jdbc:mysql://pants/a_b_c")).shouldBeEqual("a_b_c");
        the(extractDatabaseName("jdbc:mysql://pants/a-b-c")).shouldBeEqual("a-b-c");
        the(extractDatabaseName("jdbc:mysql://localhost:3306/dbname")).shouldBeEqual("dbname");
        the(extractDatabaseName("jdbc:mysql://127.0.0.1:3306/dbname")).shouldBeEqual("dbname");
        the(extractDatabaseName("jdbc:mysql://localhost/dbname;OPTION1=A;OPTION2=B")).shouldBeEqual("dbname");
        the(extractDatabaseName("jdbc:mysql://127.0.0.1:3306/dbname;OPTION1=A;OPTION2=B")).shouldBeEqual("dbname");
        the(extractDatabaseName("jdbc:mysql://localhost/dbname?OPTION1=A&OPTION2=B")).shouldBeEqual("dbname");
        the(extractDatabaseName("jdbc:mysql://127.0.0.1:3306/dbname?OPTION1=A&OPTION2=B")).shouldBeEqual("dbname");
        the(extractDatabaseName("jdbc:cassandra:///javalite?config_file=src/application.conf")).shouldBeEqual("javalite");
        //
    }

    @Test
    public void shouldExtractServerUrl()
    {
        the(extractServerUrl("jdbc:mysql:dbname")).shouldBeEqual("jdbc:mysql");
        the(extractServerUrl("jdbc:mysql://localhost/dbname")).shouldBeEqual("jdbc:mysql://localhost");
        the(extractServerUrl("jdbc:mysql://pants/a_b_c")).shouldBeEqual("jdbc:mysql://pants");
        the(extractServerUrl("jdbc:mysql://pants/a-b-c")).shouldBeEqual("jdbc:mysql://pants");
        the(extractServerUrl("jdbc:mysql://localhost/dbname")).shouldBeEqual("jdbc:mysql://localhost");
        the(extractServerUrl("jdbc:mysql://localhost:3306/dbname")).shouldBeEqual("jdbc:mysql://localhost:3306");
        the(extractServerUrl("jdbc:mysql://127.0.0.1:3306/dbname")).shouldBeEqual("jdbc:mysql://127.0.0.1:3306");
        the(extractServerUrl("jdbc:mysql://localhost/dbname;OPTION1=A;OPTION2=B")).shouldBeEqual("jdbc:mysql://localhost;OPTION1=A;OPTION2=B");
        the(extractServerUrl("jdbc:mysql://127.0.0.1:3306/dbname;OPTION1=A;OPTION2=B")).shouldBeEqual("jdbc:mysql://127.0.0.1:3306;OPTION1=A;OPTION2=B");
        the(extractServerUrl("jdbc:mysql://localhost/dbname?OPTION1=A&OPTION2=B")).shouldBeEqual("jdbc:mysql://localhost?OPTION1=A&OPTION2=B");
        the(extractServerUrl("jdbc:mysql://127.0.0.1:3306/dbname?OPTION1=A&OPTION2=B")).shouldBeEqual("jdbc:mysql://127.0.0.1:3306?OPTION1=A&OPTION2=B");
    }


}
