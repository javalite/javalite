package org.javalite.db_migrator;

import org.apache.maven.plugin.MojoExecutionException;
import org.javalite.db_migrator.maven.MigrateMojo;
import org.javalite.json.JSONHelper;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.javalite.db_migrator.JdbcPropertiesOverride.*;
import static org.javalite.test.jspec.JSpec.the;

public class MergePropertiesSpec {

    @Test
    public void shouldUseEmptyMergePropertiesWithDefaultEnvironment() throws MojoExecutionException {
        Map<String, Properties> envProperties = execute(null, null);
        the(compareMap(JSONHelper.toMap("{\n" +
                "    \"driver\" : \"" + driver() + "\",\n" +
                "    \"password\" : \"" + password() + "\",\n" +
                "    \"url\" : \"" + fullUrl() + "\",\n" +
                "    \"username\" : \"" + user() + "\"\n" +
                "}"), envProperties.get("null"))).shouldBeTrue();
    }

    @Test
    public void shouldUseMergePropertiesWithDefaultEnvironment() throws MojoExecutionException {
        Map<String, Properties> envProperties = execute(null,"/test_migrations/templator/table-names.properties");
        the(compareMap(JSONHelper.toMap("{\n" +
                "    \"authors-table\" : \"authors\",\n" +
                "    \"books-table\" : \"books\",\n" +
                "    \"driver\" : \"" + driver() + "\",\n" +
                "    \"development.authors-table\" : \"development-authors\",\n" +
                "    \"development.books-table\" : \"development-books\",\n" +
                "    \"staging.books-table\" : \"staging-books\",\n" +
                "    \"password\" : \"" + password() + "\",\n" +
                "    \"url\" : \"" + fullUrl() + "\",\n" +
                "    \"username\" : \"" + user() + "\"\n" +
                "}"), envProperties.get("null"))).shouldBeTrue();
    }

    @Test
    public void shouldUseMergePropertiesWithDevelopmentEnvironment() throws MojoExecutionException {
        Map<String, Properties> envProperties = execute("development","/test_migrations/templator/table-names.properties");
        the(compareMap(JSONHelper.toMap("{\n" +
                "    \"authors-table\" : \"development-authors\",\n" +
                "    \"books-table\" : \"development-books\",\n" +
                "    \"driver\" : \"" + driver() + "\",\n" +
                "    \"staging.books-table\" : \"staging-books\",\n" +
                "    \"password\" : \"" + password() + "\",\n" +
                "    \"url\" : \"" + fullUrl() + "\",\n" +
                "    \"username\" : \"" + user() + "\"\n" +
                "}"), envProperties.get("development"))).shouldBeTrue();
    }

    @Test
    public void shouldUseMergePropertiesWithDevelopmentAndStagingEnvironments() throws MojoExecutionException {
        Map<String, Properties> envProperties = execute("development,staging","/test_migrations/templator/table-names.properties");
        the(compareMap(JSONHelper.toMap("{\n" +
                "    \"authors-table\" : \"development-authors\",\n" +
                "    \"books-table\" : \"development-books\",\n" +
                "    \"driver\" : \"" + driver() + "\",\n" +
                "    \"password\" : \"" + password() + "\",\n" +
                "    \"url\" : \"" + fullUrl() + "\",\n" +
                "    \"username\" : \"" + user() + "\"\n" +
                "}"), envProperties.get("development"))).shouldBeTrue();
        the(compareMap(JSONHelper.toMap("{\n" +
                "    \"authors-table\" : \"authors\",\n" +
                "    \"books-table\" : \"staging-books\",\n" +
                "    \"driver\" : \"" + driver() + "\",\n" +
                "    \"password\" : \"" + password() + "\",\n" +
                "    \"url\" : \"jdbc:mysql://localhost/test_project_stage\",\n" +
                "    \"username\" : \"" + user() + "\"\n" +
                "}"), envProperties.get("staging"))).shouldBeTrue();
    }

    private Map<String, Properties> execute(String environments, String mergeProperties) throws MojoExecutionException {
        Map<String,Properties> result = new HashMap<>();
        MigrateMojo migrateMojo = new MigrateMojo() {
            @Override
            public void executeMojo() throws MojoExecutionException {
                result.put(getCurrentEnvironment() == null ? "null" : getCurrentEnvironment(), getCurrentMergeProperties());
            }
        };
        migrateMojo.setEnvironments(environments);
        migrateMojo.setDriver(driver());
        migrateMojo.setUsername(user());
        migrateMojo.setPassword(password());
        migrateMojo.setUrl(fullUrl());
        migrateMojo.setConfigFile(System.getProperty("jdbc.properties.file"));
        migrateMojo.setMergeProperties(mergeProperties);
        migrateMojo.setMigrationsPath("src/test/resources/test_migrations/mysql-templator/");
        migrateMojo.execute();
        return result;
    }

    private boolean compareMap(Map m1, Map m2) {
        System.out.println("-----------");
        System.out.println(JSONHelper.toJsonString(m1, true));
        System.out.println("              VS");
        System.out.println(JSONHelper.toJsonString(m1, true));
        if (m1 == m2) return true;
        if (m1 == null || m2 == null) {
            System.out.println(m1 + " != " + m2);
            return false;
        }
        if (m1.size() != m2.size()) {
            System.out.println(m1.size() + " != " + m2.size());
            return false;
        }
        for (Object o : m1.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            Object v = m2.get(entry.getKey());
            if (!entry.getValue().equals(v)) {
                System.out.println(entry.getKey() + "=" + entry.getValue() + " != " + entry.getKey() + "=" + v);
                return false;
            }
        }
        return true;
    }



}
