package org.javalite.app_config;

import org.javalite.test.jspec.JSpecSupport;
import org.junit.Test;

import static org.javalite.app_config.AppConfig.p;


public class AppConfigTest implements JSpecSupport {

    @Test
    public void shouldGetNameInDevelopmentEnv() {
        the(p("first.name")).shouldBeEqual("John");
    }

    @Test
    public void shouldReadAsMapFromDevelopmentFile() {
        the(new AppConfig().get("first.name")).shouldBeEqual("John");
    }

    @Test
    public void shouldFindPropertiesWithPrefix(){

        the(AppConfig.getProperties("prop")).shouldContain("one");
        the(AppConfig.getProperties("prop")).shouldContain("two");
        the(AppConfig.getProperties("prop")).shouldNotContain("John");
    }

    @Test
    public void shouldFindKeysWithPrefix(){

        the(AppConfig.getKeys("prop")).shouldContain("prop.1");
        the(AppConfig.getKeys("prop")).shouldContain("prop.1");
        the(AppConfig.getKeys("prop")).shouldNotContain("first.name");
    }

}
