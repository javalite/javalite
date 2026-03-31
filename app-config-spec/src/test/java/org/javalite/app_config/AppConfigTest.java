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
        AppConfig.setProperty("first.name", "Mike");
        a(p("first.name")).shouldBeEqual("Mike");

        AppConfig.reload();
        a(p("first.name")).shouldBeEqual("John");
    }


    @Test
    public void shouldMergeOnePropertyIntoAnother(){
        the(p("phrase")).shouldBeEqual("And the name is John");
    }

    @Test
    public void shouldShouldDetectRunningInTestMode(){
        the(AppConfig.isInTestMode()).shouldBeTrue();
    }

    @Test
    public void shouldLoadPropertiesFromProvider(){
        System.setProperty("app_config.provider", "org.javalite.app_config.TestAppConfigProvider");
        AppConfig.reload();

        // Provider property should be loaded
        the(p("provider.property")).shouldBeEqual("provider_value");

        // Provider should override file properties
        the(p("first.name")).shouldBeEqual("ProviderName");

        // Clean up
        System.clearProperty("app_config.provider");
        AppConfig.reload();
    }

    @Test
    public void shouldReloadPropertiesWhenActiveEnvChanges() {
        the(p("first.name")).shouldBeEqual("John");

        AppConfig.setActiveEnv("staging");
        the(p("first.name")).shouldBeEqual("Matt");

        AppConfig.setActiveEnv("development"); // restore
    }

    @Test
    public void shouldOverridePropertyViaTranslatedEnvVar() {
        // development.properties has java.home=placeholder.
        // JAVA_HOME env var translates to java.home and must override the file value.
        String javaHome = System.getenv("JAVA_HOME");
        if (javaHome == null) return; // skip if JAVA_HOME not set

        the(p("java.home")).shouldBeEqual(javaHome);
    }

    @Test
    public void shouldFindEnvVarOnlyKeyViaGetKeys() {
        // getKeys() must also scan env vars for keys absent from props.
        // EmbeddedTomcat and similar patterns rely on getKeys(prefix) to discover
        // all properties under a prefix — if those come from env vars only, they must show up.
        String javaHome = System.getenv("JAVA_HOME");
        if (javaHome == null) return;

        new AppConfig().remove("java.home"); // simulate env-var-only key (not in any prop file)

        the(AppConfig.getKeys("java")).shouldContain("java.home");
        the(p("java.home")).shouldBeEqual(javaHome); // value still reachable via p()
    }

    @Test
    public void shouldFallbackToTranslatedEnvVarWhenPropertyNotInProps() {
        // When a property is absent from props, getProperty() should fall back
        // to the translated env var name (one.two -> ONE_TWO).
        String javaHome = System.getenv("JAVA_HOME");
        if (javaHome == null) return; // skip if JAVA_HOME not set

        AppConfig.reload();
        new AppConfig().remove("java.home"); // simulate key absent from props

        the(p("java.home")).shouldBeEqual(javaHome);

        AppConfig.reload(); // restore
    }
}
