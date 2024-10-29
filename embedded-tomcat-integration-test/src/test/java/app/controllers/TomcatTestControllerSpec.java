package app.controllers;

import org.javalite.activejdbc.Base;
import org.javalite.activeweb.DBControllerSpec;
import org.junit.jupiter.api.Test;

public class TomcatTestControllerSpec extends DBControllerSpec {

    @Test
    public void shouldTest(){
        System.out.println("inside test: " + Base.connection());
    }
}
