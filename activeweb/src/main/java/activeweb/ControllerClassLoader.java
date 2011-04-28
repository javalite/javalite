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


import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

class ControllerClassLoader extends ClassLoader {

    private String baseDir;

    ControllerClassLoader(ClassLoader parent, String baseDir){
        super(parent);
        this.baseDir = baseDir;
    }

    public Class<?> loadClass(String name) throws ClassNotFoundException {
        //TODO - improve/refactor classloading

        if(name.startsWith("activeweb.")){
            return Thread.currentThread().getContextClassLoader().loadClass(name);
        }

        try{
            if(!(name.endsWith("Controller") || name.contains("Controller$"))){//this is to load inner anonymous classes
                throw new RuntimeException();
            }
            
            String pathToClassFile = name.replace('.', '/') + ".class";

            byte[] classBytes = read(getResourceAsStream(pathToClassFile));
            return defineClass(name, classBytes, 0, classBytes.length);
        }
        catch(Exception e){            
            return Thread.currentThread().getContextClassLoader().loadClass(name);
        }
    }

    

    @Override
    public InputStream getResourceAsStream(String name) {
        try {
            String pathToFile = baseDir + System.getProperty("file.separator") + name;
            return new FileInputStream(pathToFile);
        } catch (Exception e) {
            return super.getResourceAsStream(name);
        }
    }

    private byte[] read(InputStream in) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        for (int x = in.read(); x != -1; x = in.read()){
               bout.write(x);
        }
        return bout.toByteArray();
    }
}
