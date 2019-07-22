/*
Copyright 2009-2019 Igor Polevoy

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


package org.javalite.instrumentation;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.javalite.activejdbc.StaticMetadataGenerator;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;


/**
 * @author Igor Polevoy
 * @goal instrument
 * @requiresDependencyResolution compile+runtime
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
     * Database configuration list
     * @parameter
     */
    private List<DBParameters> databases;

    /**
     * Output directories - refer to the maven target folder
     *
     * @parameter
     */
    private String[] outputDirectories;

    /**
     * The enclosing project.
     *
     * @parameter property="project"
     * @required
     * @readonly
     */
    protected MavenProject project;

    /**
     * @parameter property="basedir"
     * @required
     * @readonly
     */
    private String basedir;

    /**
     * Generate static metadata
     * @parameter
     */
    private boolean generateStaticMetadata = false;


    public void execute() throws MojoExecutionException{
        Logger.setLog(new Log() {
            @Override
            public void info(String s) {
                getLog().info(s);
            }
            @Override
            public void error(String s) {
                getLog().error(s);
            }
        });
        try {
            addProjectClassPathToPlugin();//this will add a runtime classpath to the plugin

            if( outputDirectories != null ){
                for (String directory : outputDirectories) {
                    instrument(directory);
                }
            } else if( outputDirectory != null ){
                instrument(outputDirectory);
                if (generateStaticMetadata) {
                    generateStaticMetadata(outputDirectory);
                }
            } else{
                instrument(project.getBuild().getOutputDirectory());
                if (generateStaticMetadata) {
                    generateStaticMetadata(project.getBuild().getOutputDirectory());
                }
                //Kadvin enhance: instruct test-classes also
                instrument(project.getBuild().getTestOutputDirectory());
            }
        }
        catch (Exception e) {
            throw new MojoExecutionException("Failed to instrument...", e);
        }
    }

    private void addProjectClassPathToPlugin() throws DependencyResolutionRequiredException, MalformedURLException {
        List runtimeClasspathElements = project.getRuntimeClasspathElements();
        for (Object runtimeClasspathElement : runtimeClasspathElements) {
            String element = (String) runtimeClasspathElement;
            URL url = new File(element).toURI().toURL();
            addUrlToPluginClasspath(url);
        }
    }

    private void addUrlToPluginClasspath(URL url) {
        //Nice API, Maven :(
        PluginDescriptor pluginDescriptor = (PluginDescriptor) getPluginContext().get("pluginDescriptor");
        ClassRealm realm = pluginDescriptor.getClassRealm();
        realm.addURL(url);
    }

    private void instrument(String instrumentationDirectory) throws MalformedURLException{

        if(!new File(instrumentationDirectory).exists()){
            Logger.info("Output directory " + instrumentationDirectory + " does not exist, skipping");
            return;
        }
        URL outDir = new File(instrumentationDirectory).toURI().toURL();
        addUrlToPluginClasspath(outDir);
        Instrumentation instrumentation = new Instrumentation();
        instrumentation.setOutputDirectory(instrumentationDirectory);
        instrumentation.instrument();
    }

    private void generateStaticMetadata(String outputDirectory) {
        if(!new File(outputDirectory).exists()){
            Logger.info("Output directory " + outputDirectory + " does not exist, skipping");
            return;
        }
        StaticMetadataGenerator generator = new StaticMetadataGenerator();
        generator.setDBParameters(databases);
        generator.generate(outputDirectory);
    }
}