package org.javalite.db_migrator.maven;

import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.javalite.db_migrator.DbUtils.blank;


/**
 * @goal new
 */
public class NewMojo extends AbstractMigrationMojo {

    public void execute() throws MojoExecutionException {
        if (blank(System.getProperty("name"))) {
            getLog().error("Must provide name for migration: -Dname=migration_name");
            return;
        }

        String directory = getMigrationsPath();
        createMigrationsDirectory(directory);

        File file = new File(directory, createFileName());
        try {
            file.createNewFile();
            getLog().info("Created new migration: " + file);
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to create migration file: " + file, e);
        }
    }

    private String createFileName() {
        StringBuilder fileName = new StringBuilder();
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        fileName.append(df.format(new Date()));

        String name = System.getProperty("name");
        if (!blank(name)) {
            fileName.append('_').append(name);
        }

        fileName.append(".sql");

        return fileName.toString();
    }

    private void createMigrationsDirectory(String directory) throws MojoExecutionException {
        File f = new File(directory);
        if (f.exists() && f.isFile()) {
            throw new MojoExecutionException("Provided path is not a directory: " + directory);
        } else {
            if (!f.exists()) {
                if (f.mkdirs()) {
                    getLog().info("Creating new migration directory: " + directory);
                }
            }
        }
    }
}