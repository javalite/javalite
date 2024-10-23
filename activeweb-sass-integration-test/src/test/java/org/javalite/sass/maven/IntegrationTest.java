package org.javalite.sass.maven;

import org.javalite.common.RuntimeUtil;
import org.javalite.common.Util;
import org.junit.Test;

import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;

import static org.javalite.test.jspec.JSpec.a;
import static org.javalite.test.jspec.JSpec.the;

/**
 * @author Igor Polevoy on March 9 2021.
 */
public class IntegrationTest {


    private static final String MVN = System.getProperty("os.name").toLowerCase().contains("win") ? "mvn.cmd " : "mvn";

    protected String execute(String root, String... args){
        String  mavenArgs = Util.join(args, " ");
        RuntimeUtil.Response response = RuntimeUtil.execute(80000, new File(root), MVN + " " +  mavenArgs);
        return "************ STDOUT ***********\n"
                + response.out + "\n"
                + "************ STDERR ***********\n"
                + response.err + "\n";
    }

    @Test
    public void shouldCompileProjectWithSingleSASSFile() throws IOException {

        String root = "target/test-project";

        String output = execute(root, "clean");
        the(output).shouldContain("BUILD SUCCESS");

        output = execute(root, "install");
        the(output).shouldContain("BUILD SUCCESS");

        File f = new File(root + "/target/bootstrap.css");
        a(f.exists()).shouldBeTrue();

        the(Files.readString(f.toPath())).shouldContain("font: 100% Helvetica, sans-serif;");

    }

    @Test
    public void shouldCompileProjectWithMultipleSASSFile() throws IOException {
        String root = "target/test-project-list";
        String output = execute(root, "clean");
        the(output).shouldContain("BUILD SUCCESS");
        output = execute(root,  "install");
        the(output).shouldContain("BUILD SUCCESS");

        File f = new File(root + "/target/bootstrap.css");
        a(f.exists()).shouldBeTrue();

        the(Files.readString(f.toPath())).shouldContain("text-align: left;");
    }
}
