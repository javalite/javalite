package org.javalite.activeweb;

import org.javalite.test.SystemStreamUtil;
import org.javalite.test.jspec.JSpec;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class AppIntegrationSpecSpec extends AppIntegrationSpec {

    @BeforeClass
    public static  void before(){
        SystemStreamUtil.replaceOut();
    }

    @AfterClass
    public static  void after(){
        JSpec.the(SystemStreamUtil.getSystemOut()).shouldContain("Omitting destruction");
        SystemStreamUtil.restoreSystemOut();
    }

    @Test
    public void shouldNotCreateInjector(){
        controller("/aloha").get("index");
    }
}
