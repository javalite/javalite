package org.javalite.db_migrator;

import java.io.File;

public abstract class Migration implements Comparable{

    private File migrationFile;
    private String version;

    Migration(String version, File migrationFile) {
        this.migrationFile = migrationFile;
        this.version = version;
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
