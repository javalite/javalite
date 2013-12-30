package org.javalite.db_migrator.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.javalite.db_migrator.MigrationException;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.javalite.common.Util.blank;

/**
 * @goal new
 */
public class NewMojo extends AbstractMigrationMojo {
    public void executeMojo() throws MojoExecutionException {

        if(blank(System.getProperty("name"))){
            getLog().error("Must provide name for migration: -Dname=migration_name");
            return;
        }

        String directory = getMigrationsPath();
        createMigrationsDirectory(directory);
        String fullName = createFileName(directory);

        try {
            new File(fullName).createNewFile();
            getLog().info("Created new migration: " + fullName);
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to create migration file: " + fullName, e);
        }
    }

    private String createFileName(String directory) {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        String fileName = df.format(new Date());
        String name = System.getProperty("name");



        if (!blank(name))
            fileName += "_" + name;

        fileName += ".sql";

        if(!directory.endsWith("/")){ // I think this is universal across Windows and *nux
            directory += "/";
        }

        return directory + fileName;
    }

    private void createMigrationsDirectory(String directory) throws MojoExecutionException {
        try {
            File f = new File(directory);
            if (f.exists() && f.isFile()) {
                throw new MojoExecutionException("Provided path is not a directory: " + directory);
            } else {
                if (!f.exists()) {
                    if(f.mkdirs())
                        getLog().info("Creating new migration directory: " + directory);
                }
            }
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to create migrations directory: " + directory, e);
        }
    }
}