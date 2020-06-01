package org.javalite.db_migrator;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.javalite.common.Util;

import java.io.File;

public class GroovyMigration extends Migration {

    public GroovyMigration(String version, File migrationFile) {
        super(version, migrationFile);
    }

    @Override
    public void migrate(String encoding) {
        try{
            String script = new String(Util.read(getMigrationFile()));
            GroovyShell shell = new GroovyShell(new Binding());
            shell.evaluate(script);
        }catch(Exception e){
            throw new MigrationException(e);
        }
    }
}
