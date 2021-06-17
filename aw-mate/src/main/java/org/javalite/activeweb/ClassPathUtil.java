package org.javalite.activeweb;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class ClassPathUtil {

    protected static ClassLoader getCombinedClassLoader(MavenProject project) throws DependencyResolutionRequiredException, MalformedURLException {
        ClassLoader pluginCL = Thread.currentThread().getContextClassLoader();
        URL[] urls = new URL[project.getCompileClasspathElements().size()];
        for(int x = 0 ; x < urls.length; x++){
            urls[x] = new File(project.getCompileClasspathElements().get(x).toString()).toURI().toURL();
        }
        return  new URLClassLoader(urls, pluginCL);
    }
}
