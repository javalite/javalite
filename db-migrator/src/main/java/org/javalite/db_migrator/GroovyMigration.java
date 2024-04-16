package org.javalite.db_migrator;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;
import org.javalite.common.Templator;
import org.javalite.common.Util;

import java.io.File;
import java.util.List;
import java.util.Properties;

public class GroovyMigration extends Migration {

    private List<String> paths;

    /**
     * @param paths list of paths to be added to the classpath before executing migrations.
     */
    public GroovyMigration(List<String> paths, String version, String fileName, String migrationContent, Properties mergeProperties) {
        super(version, fileName, migrationContent, mergeProperties);
        this.paths = paths;
    }

    @Override
    public void migrate() {
        try {
            String script = getMigrationContent();
            GroovyShell shell = new GroovyShell(new Binding());
            GroovyClassLoader classLoader = shell.getClassLoader();
            paths.forEach(classLoader::addClasspath);
            String groovyScript = mergeProperties == null ? script : Templator.mergeFromTemplate(script, mergeProperties, false);
            shell.evaluate(groovyScript);
        } catch (Exception e) {
            throw new MigrationException(e);
        }
    }

}
