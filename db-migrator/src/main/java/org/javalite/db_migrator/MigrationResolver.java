package org.javalite.db_migrator;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.Resource;
import io.github.classgraph.ScanResult;
import org.javalite.common.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MigrationResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(MigrationResolver.class);


    private File migrationsLocation; // directory on the file system where migrations reside
    private  String classpathMigrationsLocation; // location on classpath where migrations reside


    private List<String> additionalClasspaths;

    private Properties mergeProperties;

    private static final Pattern MIGRATION_FILE_PATTERN = Pattern.compile("^(\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d)_.*\\.(sql|groovy)");

    /**
     * @param paths to be added to the classpath for search of project classes for the Groovy migrator.
     * @param migrationsLocation - directory on the file system where migrations reside
     */
    public MigrationResolver(List<String> paths, File migrationsLocation) {
        this(paths, migrationsLocation, null);
    }

    /**
     * @param paths to be added to the classpath for search of project classes for the Groovy migrator.
     * @param migrationsLocation - directory where migrations reside
     */
    public MigrationResolver(List<String> paths, File migrationsLocation, Properties mergeProperties) {
        this.additionalClasspaths = paths;
        this.migrationsLocation = migrationsLocation;
        this.mergeProperties = mergeProperties;
    }

    /**
     * @param additionalClasspaths to be added to the classpath for search of project classes for the Groovy migrator.
     * @param classpathMigrationsLocation - top directory of migrations if packaged on classpath
     */
    public MigrationResolver(List<String> additionalClasspaths, String classpathMigrationsLocation, Properties mergeProperties) {
        this.additionalClasspaths = additionalClasspaths;
        this.classpathMigrationsLocation = classpathMigrationsLocation;
        this.mergeProperties = mergeProperties;
    }

    public List<Migration> resolve() {

        List<Migration> migrations = new ArrayList<>();

        if(this.classpathMigrationsLocation != null) { // assume loading from classpath
            migrations = loadMigrationsFromClassPath();
        }else {
            migrations = loadMigrationsFromFiles();
        }
        Collections.sort(migrations);
        return migrations;
    }

    private List<Migration> loadMigrationsFromClassPath() {
        List<Migration> migrations = loadMigrationsFromClassPath("sql");
        List<Migration> groovyMigrations = loadMigrationsFromClassPath("groovy");
        migrations.addAll(groovyMigrations);
        return migrations;
    }

    private List<Migration> loadMigrationsFromClassPath(String extension) {
        List<Migration> migrations = new ArrayList<>();
        try (ScanResult scanResult = new ClassGraph().acceptPathsNonRecursive(classpathMigrationsLocation).scan()) {
            scanResult.getResourcesWithExtension(extension).forEachByteArrayThrowingIOException((Resource res, byte[] content) -> {
                String version = extractVersion(res.getPath().substring(res.getPath().lastIndexOf("/") + 1));
                migrations.add(new SQLMigration(version, res.getPath().toString(), new String(content), mergeProperties));
            });
        } catch (IOException e) {
            throw new MigrationException(e);
        }
        return migrations;
    }



    List<Migration> loadMigrationsFromFiles(){
        List<Migration> migrations = new ArrayList<>();
        LOGGER.info("Trying migrations at: {} ", migrationsLocation.getAbsolutePath());
        //assume flat directory of migrations
        File[] files = migrationsLocation.listFiles();

        List<File> migrationsFiles = new ArrayList<>();
        for (File file : files) {
            if (!file.isDirectory() && MIGRATION_FILE_PATTERN.matcher(file.getName()).matches()) {
                migrationsFiles.add(file);
            }
        }

        if(!migrationsLocation.isDirectory()){
            throw  new IllegalArgumentException(migrationsLocation + " is not a directory");
        }
        LOGGER.info("Trying migrations at: {} ", migrationsLocation.getAbsolutePath());
        // Extract versions and create executable migrations for each resource.
        for (File migrationFile: migrationsFiles) {
            String version = extractVersion(migrationFile.getName());
            if(migrationFile.getName().endsWith("sql")){
                migrations.add(new SQLMigration(version, migrationFile.getName(), Util.readFile(migrationFile), mergeProperties));
            }else if(migrationFile.getName().endsWith("groovy")){
                migrations.add(new GroovyMigration(additionalClasspaths, version, migrationFile.getName(), Util.readFile(migrationFile), mergeProperties));
            }else {
                throw new RuntimeException("file type not supported");
            }
        }

        return migrations;
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
