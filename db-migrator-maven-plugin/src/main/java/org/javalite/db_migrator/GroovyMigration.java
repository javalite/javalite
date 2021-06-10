package org.javalite.db_migrator;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;
import org.apache.maven.project.MavenProject;
import org.javalite.common.Templator;
import org.javalite.common.Util;

import java.io.File;
import java.util.Properties;

public class GroovyMigration extends Migration {

    private MavenProject mavenProject;

    public GroovyMigration(MavenProject mavenProject, String version, File migrationFile, Properties mergeProperties) {
        super(version, migrationFile, mergeProperties);
        this.mavenProject = mavenProject;
    }

    @Override
    public void migrate(String encoding) {
        try {
            String script = new String(Util.read(getMigrationFile()));
            GroovyShell shell = new GroovyShell(new Binding());
            GroovyClassLoader classLoader = shell.getClassLoader();
            mavenProject.getCompileClasspathElements().forEach(o -> classLoader.addClasspath(o.toString()));
            String groovyScript = mergeProperties == null ? script : Templator.mergeFromTemplate(script, mergeProperties, false);
            shell.evaluate(groovyScript);
        } catch (Exception e) {
            throw new MigrationException(e);
        }
    }

}
