package org.javalite.db_migrator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MigrationResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(MigrationResolver.class);
    private String migrationsLocation;

    private static Pattern MIGRATION_FILE_PATTERN = Pattern.compile("^(\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d)_.*\\.sql");

    public MigrationResolver(String migrationsLocation) {
        this.migrationsLocation = migrationsLocation;
    }

    public List<Migration> resolve() {
        File location = new File(migrationsLocation);
        LOGGER.info("Trying migrations at: {} ", location.getAbsolutePath());

        //assume flat directory of migrations
        File[] files = location.listFiles();

        if(files == null || files.length == 0) throw  new MigrationException("No migrations are found at: " + location.getAbsolutePath());

        //filter out garbage
        List<File> migrationsFiles = new ArrayList<File>();
        for (File file : files) {
            if (!file.isDirectory() && MIGRATION_FILE_PATTERN.matcher(file.getName()).matches()) {
                migrationsFiles.add(file);
            }
        }
        checkDuplicateVersions(migrationsFiles);


        List<Migration> migrations = new ArrayList<Migration>();

        // Extract versions and create executable migrations for each resource.
        for (File migrationFile: migrationsFiles) {
            String version = extractVersion(migrationFile.getName());
            migrations.add(new Migration(version, migrationFile));
        }

        Collections.sort(migrations);
        return migrations;
    }

    private List<String> extractVersions(List<File> migrationsFiles) {
        List<String> versions = new ArrayList<String>();
        for (File file : migrationsFiles) {
            versions.add(extractVersion(file.getName()));
        }
        return versions;
    }


    public void checkDuplicateVersions(List<File> files){
        List<String> versions = extractVersions(files);
        Set<String> versionsHash =  new HashSet<String>();
        for (String version: versions) {
            boolean isNew = versionsHash.add(version);
            if(!isNew) throw new MigrationException("Duplicate version discovered: " + version);
        }
    }


    public String extractVersion(String name) {
        String errorMessage = "Error parsing migration version from " + name;
        try {
            Matcher matcher = MIGRATION_FILE_PATTERN.matcher(name);
            boolean found = matcher.find();
            if(!found) throw  new MigrationException(errorMessage);
            return matcher.group(1);
        }catch(MigrationException e){
            throw e;
        }
        catch (Exception e) {
            throw new MigrationException(errorMessage, e);
        }
    }
}
