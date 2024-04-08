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

    public GroovyMigration(List<String> paths, String version, File migrationFile, Properties mergeProperties) {
        super(version, migrationFile, mergeProperties);
        this.paths = paths;
    }

    @Override
    public void migrate(String encoding) {
        try {
            String script = new String(Util.read(getMigrationFile()));
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
