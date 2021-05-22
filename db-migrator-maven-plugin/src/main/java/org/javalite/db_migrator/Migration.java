package org.javalite.db_migrator;

import java.io.File;
import java.util.Properties;

public abstract class Migration implements Comparable{

    private File migrationFile;
    private String version;
    protected Properties mergeProperties;

    Migration(String version, File migrationFile,Properties mergeProperties) {
        this.migrationFile = migrationFile;
        this.version = version;
        this.mergeProperties = mergeProperties;
    }

    String getVersion() {
        return version;
    }

    public String getName() {
        return migrationFile.getName();
    }

    File getMigrationFile() {
        return migrationFile;
    }

    abstract void migrate(String encoding);

    public int compareTo(Object o) {
        Migration other = (Migration) o;
        return this.getVersion().compareTo(other.getVersion());
    }
}
