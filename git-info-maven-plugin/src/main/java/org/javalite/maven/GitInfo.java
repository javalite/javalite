/*
Copyright 2009-2014 Igor Polevoy

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


package org.javalite.maven;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

import static org.javalite.common.Util.blank;
import static org.javalite.common.Util.read;

/**
 * @author Igor Polevoy: 3/15/12 2:56 PM
 */
public class GitInfo {

    public static void main(String[] args) throws IOException {
        System.out.println(genHtml());
    }


    protected static String genHtml() {
        String html = "";

        html += wrap("h2", "Date/Time");
        html += wrap("pre", new Date().toString());

        html += wrap("h2", "Host");
        try {
            html += wrap("pre", InetAddress.getLocalHost().toString());
        } catch (UnknownHostException ignore) {}

        html += wrap("h2", "Remote URL");
        html += wrap("pre", exec("git remote -v"));

        html += wrap("h2", "Remote Branches");
        html += wrap("pre", exec("git branch -r"));

        html += wrap("h2", "Local Branch");
        html += wrap("pre", exec("git branch"));
//
//        html += wrap("h2", "Configuration");
//        html += wrap("pre", exec("cat .git/config"));

        html += wrap("h2", "Most recent commit");
        html += wrap("pre", exec("git log --max-count=1"));
        return html;
    }

    private static String wrap(String tag, String content){
        return "<" + tag + ">" + content + "</" + tag + ">\n" ;
    }


    private static String exec(String command) {

        Runtime runtime = Runtime.getRuntime();

        try {

            Process p = runtime.exec(command);
            String output = read(p.getInputStream());
            String error = read(p.getErrorStream());

            if (!blank(error)) {
                throw new ExecException(error);
            }
            return output;
        } catch (ExecException e) {
            throw e;
        } catch (Exception e) {
            throw new ExecException(e);
        }
    }
}
