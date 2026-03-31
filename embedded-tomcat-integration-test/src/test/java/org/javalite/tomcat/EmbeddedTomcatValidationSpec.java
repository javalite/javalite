package org.javalite.tomcat;

import org.javalite.app_config.AppConfig;
import org.javalite.test.jspec.JSpecSupport;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import static org.javalite.tomcat.EmbeddedTomcat.validateRequiredProperties;

public class EmbeddedTomcatValidationSpec implements JSpecSupport {

    @After
    public void tearDown() {
        AppConfig.reload();
    }

    @Test
    public void shouldPassValidationWhenAllRequiredPropertiesArePresent() {
        // development.properties has all required properties — should not throw
        validateRequiredProperties();
    }

    @Test
    public void shouldThrowWhenRequiredPropertiesAreMissing() {
        new AppConfig().remove("embedded.tomcat.port");
        new AppConfig().remove("embedded.tomcat.pool.url");
        new AppConfig().remove("embedded.tomcat.pool.password");

        try {
            validateRequiredProperties();
            Assert.fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            the(e.getMessage()).shouldContain("embedded.tomcat.port");
            the(e.getMessage()).shouldContain("embedded.tomcat.pool.url");
            the(e.getMessage()).shouldContain("embedded.tomcat.pool.password");
        }
    }

    @Test
    public void shouldListAllMissingPropertiesInOneException() {
        new AppConfig().remove("embedded.tomcat.pool.url");
        new AppConfig().remove("embedded.tomcat.pool.username");
        new AppConfig().remove("embedded.tomcat.pool.password");

        try {
            validateRequiredProperties();
            Assert.fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            the(e.getMessage()).shouldContain("embedded.tomcat.pool.url");
            the(e.getMessage()).shouldContain("embedded.tomcat.pool.username");
            the(e.getMessage()).shouldContain("embedded.tomcat.pool.password");
            // port is still set — must not appear in the error
            the(e.getMessage()).shouldNotContain("embedded.tomcat.port");
        }
    }
}
