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


package org.javalite.maven;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

import static org.javalite.common.Util.blank;
import static org.javalite.common.Util.read;

/**
 * @author Igor Polevoy: 3/15/12 2:56 PM
 */
public class GitInfo {

    protected static String genHtml() {
        StringBuilder html = new StringBuilder();

        wrap(html, "h2", "Date/Time");
        wrap(html, "pre", new Date().toString());
        
        wrap(html, "h2", "Host");
        try {
            wrap(html, "pre", InetAddress.getLocalHost().toString());
        } catch (UnknownHostException ignore) {}

        wrap(html, "h2", "Remote URL");
        wrap(html, "pre", exec("git remote -v"));

        wrap(html, "h2", "Remote Branches");
        wrap(html, "pre", exec("git branch -r"));

        wrap(html, "h2", "Local Branch");
        wrap(html, "pre", exec("git branch"));
//
//        wrap(html, "h2", "Configuration");
//        wrap(html, "pre", exec("cat .git/config"));

        wrap(html, "h2", "Most recent commit");
        wrap(html, "pre", exec("git log --max-count=1"));
        return html.toString();
    }

    private static void wrap(StringBuilder html, String tag, String content){
        html.append('<').append(tag).append('>').append(content).append("</").append(tag).append(">\n");
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
