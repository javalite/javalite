package app.controllers;

import org.javalite.activejdbc.Base;
import org.javalite.activeweb.DBControllerSpec;
import org.junit.jupiter.api.Test;

public class TomcatTestControllerSpec extends DBControllerSpec {

    @Test
    public void shouldFIndDBConnection(){

        //This test ensures that the DBCOnfig was properly setup and can access the test DB connection

        System.out.println("inside test: " + Base.connection());
    }
}
