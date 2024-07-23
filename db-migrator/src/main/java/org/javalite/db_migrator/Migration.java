package org.javalite.db_migrator;

import java.util.Properties;

public abstract class Migration implements Comparable{

    private String migrationContent;
    private String fileName;
    private String version;

    protected Properties mergeProperties;

    Migration(String version, String fileName, String migrationContent, Properties mergeProperties) {
        this.migrationContent = migrationContent;
        this.fileName = fileName;
        this.version = version;
        this.mergeProperties = mergeProperties;
    }

    public String getVersion() {
        return version;
    }

    public String getFileName() {
        return fileName;
    }

    public String getMigrationContent() {
        return migrationContent;
    }

    abstract void migrate();

    public int compareTo(Object o) {
        Migration other = (Migration) o;
        return this.getVersion().compareTo(other.getVersion());
    }
}
