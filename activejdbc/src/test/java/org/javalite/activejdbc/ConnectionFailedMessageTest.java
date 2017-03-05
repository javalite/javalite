package org.javalite.activejdbc;/*
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

import org.javalite.test.SystemStreamUtil;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.javalite.test.SystemStreamUtil.getSystemErr;
import static org.javalite.test.SystemStreamUtil.replaceError;
import static org.javalite.test.SystemStreamUtil.restoreSystemErr;
import static org.javalite.test.jspec.JSpec.a;

/**
 * @author Igor Polevoy: 2/11/13 12:58 PM
 */
public class ConnectionFailedMessageTest {

    @Test
    public void shouldReportURLIfCannotConnect() {
        replaceError();
        try{
            Base.open("com.mysql.jdbc.Driver", "jdbc:mysql://doesnotexist/", "root", "blah");
        }catch(Exception e){
            e.printStackTrace();
        }
        a(getSystemErr()).shouldContain("Failed to connect to JDBC URL: jdbc:mysql://doesnotexist/");
        restoreSystemErr();
    }
}
