package org.javalite.sass.maven;

import org.apache.maven.shared.invoker.*;

import org.javalite.common.Util;
import org.junit.Test;

import java.io.*;
import java.util.Arrays;

import static org.javalite.test.jspec.JSpec.a;
import static org.javalite.test.jspec.JSpec.the;

/**
 * @author Igor Polevoy on March 9 2021.
 */
public class IntegrationTest {

    protected String execute(String root, boolean offline, String... args) throws MavenInvocationException {
        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile( new File( root + "/pom.xml" ) );
        request.setGoals(Arrays.asList(args));
        request.setOffline(offline);
        Invoker invoker = new DefaultInvoker();
        invoker.setWorkingDirectory(new File(root));


        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
        invoker.setErrorHandler(new PrintStreamHandler(new PrintStream(outputStream), true));
        invoker.setOutputHandler(new PrintStreamHandler(new PrintStream(errorStream), true));
        invoker.execute(request);
        String output = outputStream.toString();
        output += errorStream.toString();
        if(!output.contains("BUILD SUCCESS")){
            System.out.println("BUILD FAILED>>>>>>>>>>>\n" + output);
        }
        return output;
    }

    @Test
    public void shouldCompileProjectWithSingleSASSFile() throws MavenInvocationException, IOException {

        String root = "target/test-project";

        String output = execute(root, true, "clean");
        the(output).shouldContain("BUILD SUCCESS");

        output = execute(root, true,  "install");
        the(output).shouldContain("BUILD SUCCESS");

        File f = new File(root + "/target/bootstrap.css");
        a(f.exists()).shouldBeTrue();

        the(Util.readFile(f.getAbsolutePath())).shouldContain("font: 100% Helvetica, sans-serif;");
    }

    @Test
    public void shouldCompileProjectWithMultipleSASSFile() throws MavenInvocationException, IOException {
        String root = "target/test-project-list";
        String output = execute(root, true, "clean");
        the(output).shouldContain("BUILD SUCCESS");
        output = execute(root, true,  "install");
        the(output).shouldContain("BUILD SUCCESS");

        File f = new File(root + "/target/bootstrap.css");
        a(f.exists()).shouldBeTrue();

        the(Util.readFile(f.getAbsolutePath())).shouldContain("text-align: left;");
    }
}
