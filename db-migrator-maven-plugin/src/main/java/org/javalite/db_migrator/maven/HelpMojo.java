package org.javalite.db_migrator.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.javalite.common.Util;


@Mojo(name = "help")
public class HelpMojo extends AbstractMojo {
    public void execute() throws MojoExecutionException {
        getLog().info(Util.readResource("/help.txt"));
    }
}