package org.javalite.lessc.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.javalite.common.Util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static java.lang.Runtime.getRuntime;


@Mojo( name = "compile", defaultPhase = LifecyclePhase.PREPARE_PACKAGE)
public class CompileLesscFilesMojo extends AbstractMojo {

    @Parameter( property = "project", required = true )
    protected MavenProject project;

    @Parameter( required = false )
    private String lesscMain ;

    @Parameter( required = false )
    private String targetDirectory;

    @Parameter( required = false )
    private String targetFileName;


    @Parameter( required = false )
    private List<LessConfig> lessConfigs;

    public final void execute() throws MojoExecutionException {

        if ((lesscMain == null || targetDirectory == null || targetFileName == null) && lessConfigs == null
                || lesscMain != null && targetDirectory != null && targetFileName != null && lessConfigs != null) {
            throw new MojoExecutionException("You must provide configuration for one less file or for a list, but not both");
        }


        if(lessConfigs == null){
            processFile(lesscMain, targetDirectory, targetFileName, null);
        }else{
            for (LessConfig config : lessConfigs) {
                processFile(config.getLesscMain(), config.getTargetDirectory(), config.getTargetFileName(), config.getLesscArguments());
            }
        }
    }

    private void processFile(String sourceFile, String targetDirectory, String targetFileName, String arguments) throws MojoExecutionException {

        try {
            String css = compile(new File(sourceFile), arguments);
            File dir = new File(targetDirectory);
            if (!dir.exists() && !dir.mkdirs()) {
                throw new MojoExecutionException("Failed to create directory: " + targetDirectory);
            }

            String path = targetDirectory + System.getProperty("file.separator") + targetFileName;
            getLog().info("Storing CSS into: " + path);
            Files.write(Paths.get(path), css.getBytes());
        } catch (Exception e) {
            if (e instanceof MojoExecutionException) {
                throw (MojoExecutionException) e;
            } else {
                throw new MojoExecutionException(e.getMessage(), e);
            }
        }
    }

    public String compile(File sourceFile, String arguments) throws IOException, InterruptedException, MojoExecutionException {

        if(!sourceFile.exists()){
            throw new MojoExecutionException("File: " + sourceFile.getPath() + " does not exist. Current directory: " + new File(".").getCanonicalPath());
        }

        String[] commandLine = getCommandLine(arguments, sourceFile.getPath());
        getLog().info("Executing: " + Util.join(commandLine, " "));
        Process process = getRuntime().exec(commandLine);
        String css = read(process.getInputStream(), "UTF-8");
        String error = read(process.getErrorStream(), "UTF-8");
        if (process.waitFor() != 0) {
            throw new MojoExecutionException(error);
        }
        return css;
    }

    private String[] getCommandLine(String arguments, String targetFilePath){
        String executable = "lessc";
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            executable += ".cmd";
        }
        String[] res;
        if(arguments == null){
            res = new String[2];
            res[0] = executable;
            res[1] = targetFilePath;
        }else{
            String[] args = arguments.split(" ");
            res = new String[args.length + 2];
            res[0] = executable;
            System.arraycopy(args, 0, res, 1, args.length);
            res[res.length - 1] = targetFilePath;
        }
        return res;
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
