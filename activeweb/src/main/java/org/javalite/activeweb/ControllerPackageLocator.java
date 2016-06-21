/*
Copyright 2009-2016 Igor Polevoy

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

package org.javalite.activeweb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.FilterConfig;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * This is a utility class to discover controller packages under "app.controllers" package in Jar files and directories
 * on classpath.
 *
 * @author Igor Polevoy
 */
class ControllerPackageLocator {

    private static Logger LOGGER = LoggerFactory.getLogger(ControllerPackageLocator.class.getSimpleName());

    private ControllerPackageLocator() {}

    public static List<String> locateControllerPackages(FilterConfig config) {
        String controllerPath = System.getProperty("file.separator") + Configuration.getRootPackage() + System.getProperty("file.separator") + "controllers";
        List<String> controllerPackages = new ArrayList<>();
        List<URL> urls = getUrls(config);
        for (URL url : urls) {
            File f = new File(url.getFile());
            if (f.isDirectory()) {
                try {
                    discoverInDirectory(f.getCanonicalPath() + controllerPath, controllerPackages, "");
                } catch (Exception ignore) {
                }
            } else {//assuming jar file
                discoverInJar(f, controllerPackages);
            }
        }
        return controllerPackages;
    }

    private static void discoverInDirectory(String directoryPath, List<String> controllerPackages, String parent) {
        try {
            File directory = new File(directoryPath);
            if (!directory.exists()) {
                //nothing
            } else {
                File[] files = directory.listFiles();
                for (File file : files) {
                    if (file.isDirectory()) {
                        controllerPackages.add(parent + (parent.equals("") ? "" : ".") + file.getName());
                        discoverInDirectory(file.getCanonicalPath(), controllerPackages, parent + (parent.equals("") ? "" : ".") + file.getName());
                    }
                }
            }
        } catch (Exception ignore) {
        }
    }

    protected static void discoverInJar(File file, List<String> controllerPackages) {
        String base = "app/controllers/";
        try {
            JarFile jarFile = new JarFile(file);

            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();

                String path = jarEntry.toString();
                if (path.startsWith(base) && !path.endsWith(".class") && !path.equals(base)) {
                    controllerPackages.add(path.substring(base.length(), path.length() - 1).replace("/", "."));
                }
            }
        } catch (Exception ignore) {
        }
    }

    private static List<URL> getUrls(FilterConfig config) {
        URL[] urls;
        try {
            urls = ((URLClassLoader) Thread.currentThread().getContextClassLoader()).getURLs();
            return Arrays.asList(urls);
        } catch (ClassCastException e) {
            return hackForWeblogic(config);
        }
    }

    //Maybe this is a hack for other containers too?? Maybe this is not a hack at all?
    private static List<URL> hackForWeblogic(FilterConfig config) {
        List<URL> urls = new ArrayList<>();
        Set libJars = config.getServletContext().getResourcePaths("/WEB-INF/lib");
        for (Object jar : libJars) {
            try {
                urls.add(config.getServletContext().getResource((String) jar));
            }
            catch (MalformedURLException e) {
                LOGGER.warn("Failed to get resource: " + jar);
            }
        }
        addClassesUrl(config, urls);
        return urls;
    }

    private static void addClassesUrl(FilterConfig config, List<URL> urls) {
        Set resources = config.getServletContext().getResourcePaths("/WEB-INF/classes/");
        System.out.println(resources);
        if (!resources.isEmpty()) {
            try {
                String first = resources.iterator().next().toString();
                String urlString = config.getServletContext().getResource(first).toString();
                String url = null;
                if(urlString.startsWith("zip") && urlString.contains("!")){ // example: zip:/home/igor/projects/domains/vrs/domain/vrs/servers/vrs_web/tmp/_WL_user/_appsdir_vrs-ear-4.0.1-SNAPSHOT_ear/tr9ese/war/WEB-INF/lib/_wl_cls_gen.jar!/com/
                    url = urlString.substring(urlString.indexOf(":") + 2, urlString.indexOf("!"));
                }else if(urlString.startsWith("jndi")){ // example: jndi:/default-host/activeweb-bootstrap-1.1-SNAPSHOT/WEB-INF/classes/app/
                    url = urlString.substring(urlString.indexOf(":") + 2);
                }
                if(url != null){
                    urls.add(new URL("file:/" + url));
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
