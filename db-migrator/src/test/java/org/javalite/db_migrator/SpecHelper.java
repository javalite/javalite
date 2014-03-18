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

/**
 * @author Igor Polevoy: 12/28/13 12:50 PM
 */

package org.javalite.db_migrator;

import org.codehaus.plexus.util.FileUtils;
import org.javalite.common.Util;
import org.junit.Ignore;

import java.io.*;

@Ignore
public class SpecHelper {

    public static final String TEST_PROJECT_DIR = "target/test-project";


    public static String execute(String... args) throws IOException, InterruptedException {
        Process p = Runtime.getRuntime().exec(args, null, new File(TEST_PROJECT_DIR));
        p.waitFor();
        String out = Util.read(p.getInputStream());
        String err = Util.read(p.getInputStream());
        return "TEST MAVEN EXECUTION START >>>>>>>>>>>>>>>>>>>>>>>>\nOut: \n" + out + "\nErr:" + err + "\nTEST MAVEN EXECUTION END <<<<<<<<<<<<<<<<<<<<<<";
    }

    public static void reCreateProject() throws IOException {
        FileUtils.deleteDirectory(TEST_PROJECT_DIR);
        copyFolder(new File("src/test/test-project"), new File("target/test-project"));
    }

    public static void copyFolder(File src, File dest) throws IOException {
        if (src.isDirectory()) {
            if (!dest.exists())
                dest.mkdir();

            for (String file : src.list())
                copyFolder(new File(src, file), new File(dest, file));
        } else {
            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0)
                out.write(buffer, 0, length);

            in.close();
            out.close();
        }
    }

    //will return null of not found
    public static String findMigrationFile(String substring) {
        String[] files = new File(TEST_PROJECT_DIR + "/src/migrations").list();
        for (String file : files) {
            if (file.contains(substring))
                return file;
        }
        return null;
    }
    

}
