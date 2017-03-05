package org.javalite.app_config;

import org.javalite.test.jspec.JSpecSupport;
import org.junit.Test;

import static org.javalite.app_config.AppConfig.p;


public class AppConfigTest extends JSpecSupport {

    @Test
    public void shouldGetNameInDevelopmentEnv() {
        the(p("first.name")).shouldBeEqual("John");
    }

    @Test
    public void shouldReadAsMapFromDevelopmentFile() {
        the(new AppConfig().get("first.name")).shouldBeEqual("John");
    }
}
