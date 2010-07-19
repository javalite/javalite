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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.io.File;


/**
 * @author Igor Polevoy
 * @goal instrument
 * @requiresDependencyResolution compile
 * @execute phase="process-classes"
 */

public class ActiveJdbcInstrumentationPlugin extends AbstractMojo {

    /**
     * The enclosing project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;


    public void execute() throws MojoExecutionException, MojoFailureException {

        //this is an unbelievable hack I had to do in order to add output directory to classpath.
        //if anyone has a better idea, I will gladly listen...
        //Basically, the plugin is running with a different classpath - I searched high and wide, wrote a lot of garbage code,
        // but this is the only "solution" that works.
        //Igor
        try {
            ClassLoader realmLoader = getClass().getClassLoader();
            URL outDir = new File(project.getBuild().getOutputDirectory()).toURL();
            Method addUrlMethod = realmLoader.getClass().getSuperclass().getDeclaredMethod("addURL", URL.class);
            addUrlMethod.setAccessible(true);
            addUrlMethod.invoke(realmLoader, outDir);
            Instrumentation instrumentation = new Instrumentation();
            instrumentation.setOutputDirectory(project.getBuild().getOutputDirectory());
            instrumentation.instrument();
        }
        catch (Exception e) {
            throw new MojoExecutionException("Failed to add output directory to classpath", e);
        }
    }
}
