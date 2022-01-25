package org.javalite.db_migrator;

import org.apache.maven.plugin.MojoExecutionException;
import org.javalite.db_migrator.maven.MigrateMojo;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.javalite.common.Collections.map;
import static org.javalite.db_migrator.JdbcPropertiesOverride.*;
import static org.javalite.test.jspec.JSpec.the;

public class MergePropertiesSpec {

    @Test
    public void shouldUseEmptyMergePropertiesWithDefaultEnvironment() throws MojoExecutionException {
        Map<String, Properties> envProperties = execute(null, null);
        the(envProperties.get("null")).shouldBeEqual(map("driver", driver(), "password", password(), "url", fullUrl(), "username", user()));
    }

    @Test
    public void shouldUseMergePropertiesWithDefaultEnvironment() throws MojoExecutionException {
        Map<String, Properties> envProperties = execute(null,"/test_migrations/templator/table-names.properties");
        the(envProperties.get("null")).shouldBeEqual(map("authors-table", "authors",
                "books-table", "books",
                "driver", driver(),
                "development.authors-table", "development-authors",
                "development.books-table", "development-books",
                "staging.books-table", "staging-books",
                "password", password(),
                "url", fullUrl(),
                "username", user()));
    }

    @Test
    public void shouldUseMergePropertiesWithDevelopmentEnvironment() throws MojoExecutionException {
        Map<String, Properties> envProperties = execute("development","/test_migrations/templator/table-names.properties");
        the(envProperties.get("development")).shouldBeEqual(map(
                "authors-table", "development-authors",
                "books-table", "development-books",
                "driver", driver(),
                "staging.books-table", "staging-books",
                "password", password(),
                "url", fullUrl(),
                "username", user()
        ));
    }

    @Test
    public void shouldUseMergePropertiesWithDevelopmentAndStagingEnvironments() throws MojoExecutionException {
        Map<String, Properties> envProperties = execute("development,staging","/test_migrations/templator/table-names.properties");
        the(envProperties.get("development")).shouldBeEqual(map(
                "authors-table", "development-authors",
                "books-table", "development-books",
                "driver", driver(),
                "password", password(),
                "url", fullUrl(),
                "username", user()));
        the(envProperties.get("staging")).shouldBeEqual(map(
                "authors-table", "authors",
                "books-table", "staging-books",
                "driver", driver(),
                "password", password(),
                "url", "jdbc:mariadb://localhost:3307/test_project_stage",
                "username", user()));
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

}
