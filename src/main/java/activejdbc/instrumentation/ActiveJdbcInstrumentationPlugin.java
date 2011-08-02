/*
Copyright 2009-2010 Igor Polevoy

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/


package activejdbc.instrumentation;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * @author Igor Polevoy
 * @goal instrument
 * @requiresDependencyResolution compile
 * @execute phase="process-classes"
 */

public class ActiveJdbcInstrumentationPlugin extends AbstractMojo {


    /**
     * Output directory  - where to do instrumentation.
     *
     * @parameter
     */
    private String outputDirectory;


    /**
     * Output directories - refer to the maven target folder
     *
     * @parameter
     */
    private String[] outputDirectories;

    /**
     * The enclosing project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;


    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            //TODO we should support set a filter for dependencies, and convert filtered dependency as Maven Project
            // But I don't how to convert, so I use a temp solution: developer set outputDirectories(target path)
            if( outputDirectories != null ){
                for (String directory : outputDirectories) {
                    instrument(directory);
                }
            } else if( outputDirectory != null ){
                instrument(outputDirectory);
            } else{
                String instrumentationDirectory =  project.getBuild().getOutputDirectory();
                instrument(instrumentationDirectory);
                //Kadvin enhance: instruct test-classes also
                instrumentationDirectory =  project.getBuild().getTestOutputDirectory() ;
                instrument(instrumentationDirectory);
            }
        }
        catch (Exception e) {
            throw new MojoExecutionException("Failed to add output directory to classpath", e);
        }
    }



    private void instrument(String instrumentationDirectory) throws MalformedURLException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {//this is an unbelievable hack I had to do in order to add output directory to classpath.

        if(!new File(instrumentationDirectory).exists()){
            getLog().info("Instrumentation: directory " + instrumentationDirectory + " does not exist, skipping");
            return;
        }

        //If anyone has a better idea, I will gladly listen...
        //Basically, the plugin is running with a different classpath - I searched high and wide, wrote a lot of garbage code,
        //but this is the only "solution" that works. Basically I need the instrumentationDirectory be on classpath
        //Igor
        ClassLoader realmLoader = getClass().getClassLoader();
        URL outDir = new File(instrumentationDirectory).toURL();
        Method addUrlMethod = realmLoader.getClass().getSuperclass().getDeclaredMethod("addURL", URL.class);
        addUrlMethod.setAccessible(true);
        addUrlMethod.invoke(realmLoader, outDir);
        Instrumentation instrumentation = new Instrumentation();
        instrumentation.setOutputDirectory(instrumentationDirectory);
        instrumentation.instrument();
    }
}