/*
Copyright 2009-2018 Igor Polevoy

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

package org.javalite.db_migrator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.maven.cli.MavenCli;
import static org.javalite.db_migrator.DbUtils.closeQuietly;

public abstract class AbstractIntegrationSpec {

    protected String execute(String dir, String... args) {
        OutputStream outos = null;
        PrintStream outps = null;
        OutputStream erros = null;
        PrintStream errps = null;
        try {
            outos = new ByteArrayOutputStream();
            outps = new PrintStream(outos);
            erros = new ByteArrayOutputStream();
            errps = new PrintStream(erros);
            MavenCli cli = new MavenCli();

            Properties props = System.getProperties();

            //if running on Travis, set a profile
            if(props.getProperty("user.name").equals("travis")){
                List<String> argsList = Arrays.asList(args);
                argsList.add("-Ptravis");
                args = argsList.toArray(new String[0]);
            }
            int code = cli.doMain(args, dir, outps, errps);
            String out = outos.toString();
            String err = erros.toString();

            System.out.println("TEST MAVEN EXECUTION START >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            System.out.print("Executing: mvn");
            for (String arg : args) {
                System.out.print(' ');
                System.out.print(arg);
            }
            System.out.println();
            System.out.print("Exit code: ");
            System.out.println(code);
            System.out.print(out);
            System.err.print(err);
            System.out.println("TEST MAVEN EXECUTION END <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");

            return out + err;
        } finally {
            closeQuietly(errps);
            closeQuietly(erros);
            closeQuietly(outps);
            closeQuietly(outos);
        }
    }
}
