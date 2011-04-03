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

package activeweb;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * This is a utility class to discover controller packages under "app.controllers" package in Jar files and directories
 * on classpath.
 * 
 * @author Igor Polevoy
 */
public class ControllerPackageLocator {
    public static List<String> locateControllerPackages()  {
        String controllerPath = System.getProperty("file.separator") + Configuration.getRootPackage() + System.getProperty("file.separator") + "controllers";
        List<String> controllerPackages = new ArrayList<String>();
        URL[] urls =  ((URLClassLoader)Thread.currentThread().getContextClassLoader()).getURLs();
        for (URL url : urls) {
            File f = new File(url.getFile());
            if (f.isDirectory()) {
                try{
                    discoverInDirectory(f.getCanonicalPath() + controllerPath, controllerPackages, "");
                }catch(Exception ignore){}
            }else{//assuming jar file
                discoverInJar(f, controllerPackages);
            }
        }
        return controllerPackages;
    }

    private static void discoverInDirectory(String directoryPath, List<String> controllerPackages, String parent) {
        try{
            File directory = new File(directoryPath);
            if(!directory.exists()){
                //nothing
            }else{
                File[]  files = directory.listFiles();
                for (File file : files) {
                    if(file.isDirectory()){
                        controllerPackages.add(parent + (parent.equals("")? "" :".") + file.getName());
                        discoverInDirectory(file.getCanonicalPath(), controllerPackages, parent + (parent.equals("")? "" :".") + file.getName());
                    }
                }
            }
        }catch(Exception ignore){}
    }

    protected static void discoverInJar(File file, List<String> controllerPackages) {
        String base = "app/controllers/";
        try{
            JarFile jarFile = new JarFile(file);

            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();

                String path = jarEntry.toString();
                if(path.startsWith(base) && !path.endsWith(".class") &&  !path.equals(base)){
                         controllerPackages.add(path.substring(base.length(), path.length() - 1).replace("/", "."));
                }
            }
        }catch(Exception ignore){}
    }
}
