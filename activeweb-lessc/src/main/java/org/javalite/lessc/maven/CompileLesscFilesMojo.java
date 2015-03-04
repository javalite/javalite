package org.javalite.lessc.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import java.io.*;

import static java.lang.Runtime.getRuntime;


/**
 * @goal compile
 * @phase prepare-package
 */
public class CompileLesscFilesMojo extends AbstractMojo {

    /**
     * @parameter property="project"
     * @required
     */
    protected MavenProject project;

    /**
     * @parameter
     * @required
     */
    private String lesscMain = "src/main/webapp/less/main.less";

    /**
     * @parameter
     * @required
     */
    private String targetDirectory = "target/web";

    /**
     * @parameter
     * @required
     */
    private String targetFileName = "main.css";

    public final void execute() throws MojoExecutionException {

        try {
            String css = lessc(new File(lesscMain));
            File dir = new File(targetDirectory);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    throw new MojoExecutionException("Failed to create directory: " + targetDirectory);
                }
            }
            getLog().info("Storing CSS into: " + targetDirectory + System.getProperty("file.separator") + targetFileName);
            FileWriter writer = new FileWriter(targetDirectory + System.getProperty("file.separator") + targetFileName);
            writer.write(css);
            writer.close();
        } catch (Exception e) {
            if (e instanceof MojoExecutionException) {
                throw (MojoExecutionException) e;
            } else {
                throw new MojoExecutionException(e.getMessage(), e);
            }
        }
    }

    public String lessc(File lessFile) throws IOException, InterruptedException, MojoExecutionException {

        if(!lessFile.exists()){
            throw new MojoExecutionException("File: " + lessFile.getPath() + " does not exist. Current directory: " + new File(".").getCanonicalPath());
        }
        getLog().info("Executing: " + "lessc " + lessFile.getPath());

        String exec = "lessc";
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            exec += ".cmd";
        }
        Process process = getRuntime().exec(new String[]{exec, lessFile.getPath()});
        String css = read(process.getInputStream(), "UTF-8");
        String error = read(process.getErrorStream(), "UTF-8");
        if (process.waitFor() != 0) {
            throw new MojoExecutionException(error);
        }
        return css;
    }

    public static String read(InputStream in, String charset) throws IOException {
        if (in == null)
            throw new IllegalArgumentException("input stream cannot be null");

        InputStreamReader reader = new InputStreamReader(in, charset);
        char[] buffer = new char[1024];
        StringBuilder sb = new StringBuilder();

        for (int x = reader.read(buffer); x != -1; x = reader.read(buffer)) {
            sb.append(buffer, 0, x);
        }
        return sb.toString();
    }
}
