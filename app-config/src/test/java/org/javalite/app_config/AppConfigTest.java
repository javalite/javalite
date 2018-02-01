package org.javalite.app_config;

import org.javalite.test.jspec.JSpecSupport;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import static org.javalite.app_config.AppConfig.p;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AppConfigTest implements JSpecSupport {

    @Test
    public void should_A_GetNameInDevelopmentEnv() {
        the(p("first.name")).shouldBeEqual("John");
    }

    @Test
    public void should_B_ReadAsMapFromDevelopmentFile() {
        the(new AppConfig().get("first.name")).shouldBeEqual("John");
    }

    @Test
    public void should_C_FindPropertiesWithPrefix(){

        the(AppConfig.getProperties("prop")).shouldContain("one");
        the(AppConfig.getProperties("prop")).shouldContain("two");
        the(AppConfig.getProperties("prop")).shouldNotContain("John");
    }

    @Test
    public void should_D_FindKeysWithPrefix(){

        the(AppConfig.getKeys("prop")).shouldContain("prop.1");
        the(AppConfig.getKeys("prop")).shouldContain("prop.1");
        the(AppConfig.getKeys("prop")).shouldNotContain("first.name");
    }

    @Test
    public void should_E_OverridePropertyFromFile(){
        the(AppConfig.getKeys("prop")).shouldContain("prop.1");
        the(AppConfig.getKeys("prop")).shouldContain("prop.1");
        the(AppConfig.getKeys("prop")).shouldNotContain("first.name");
    }

    @Test
    public void should_F_OverrideFromFile(){
        System.setProperty("app_config.properties", "target/test-classes/production.properties");
        AppConfig.reload();
        the(p("first.name")).shouldBeEqual("Larry");
    }

    @Test
    public void shouldOverrideFromCode(){
        AppConfig.reload();
        AppConfig.setProperty("first.name", "Mike");
        a(p("first.name")).shouldBeEqual("Mike");

        AppConfig.reload();
        a(p("first.name")).shouldBeEqual("John");
    }


    @Test
    public void shouldMergeOnePropertyIntoAnother(){
        AppConfig.reload();
        the(p("phrase")).shouldBeEqual("And the name is John");
    }
}
