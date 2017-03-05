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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import java.io.FileOutputStream;


/**
 * @author Igor Polevoy
 * @goal generate
 * @execute phase="compile"
 */

public class GitInfoPlugin extends AbstractMojo {


    /**
     * The enclosing project.
     *
     * @parameter property="project"
     * @required
     * @readonly
     */
    protected MavenProject project;

    public void execute() throws MojoExecutionException, MojoFailureException {
        String targetDirectory = project.getBuild().getOutputDirectory();
        String htm = GitInfo.genHtml();
        try {
            FileOutputStream fout = new FileOutputStream(targetDirectory + System.getProperty("file.separator") + "git_info.html");
            fout.write(htm.getBytes());
        } catch (Exception e) {
            throw new ExecException(e);
        }
    }
}