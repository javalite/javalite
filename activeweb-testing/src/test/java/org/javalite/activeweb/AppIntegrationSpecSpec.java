package org.javalite.activeweb;

import org.javalite.test.SystemStreamUtil;
import org.javalite.test.jspec.JSpec;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class AppIntegrationSpecSpec extends AppIntegrationSpec {

    @BeforeClass
    public static  void before(){
        SystemStreamUtil.replaceError();
    }

    @AfterClass
    public static  void after(){
        JSpec.the(SystemStreamUtil.getSystemErr()).shouldContain("Omitting destruction");
        SystemStreamUtil.restoreSystemErr();
    }

    @Test
    public void shouldNotCreateInjector(){
        controller("/aloha").get("index");
    }
}
