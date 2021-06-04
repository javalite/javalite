package org.javalite.activeweb;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;


@Mojo(name = "help")
public class HelpMojo extends AbstractMojo {


    @Override
    public void execute() {

        System.out.println("""
                This plugin has the following goals:
                * print - prints all existing routes in the given ActiveWeb project
                * generate - will generate OpenAPI documentation from those routes  that include an @OpenAPI annotation
                * help - prints this message""");
    }
}
