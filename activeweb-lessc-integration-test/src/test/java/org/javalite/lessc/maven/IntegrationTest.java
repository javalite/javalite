package org.javalite.lessc.maven;

import org.apache.maven.shared.invoker.*;
import org.junit.Test;

import java.io.*;
import java.util.Arrays;

import static org.javalite.test.jspec.JSpec.a;
import static org.javalite.test.jspec.JSpec.the;

/**
 * @author Igor Polevoy on 5/22/15.
 */
public class IntegrationTest {

    protected String execute(String root, String... args) throws IOException, InterruptedException, MavenInvocationException {
        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile( new File( root + "/pom.xml" ) );
        request.setGoals(Arrays.asList(args));
        Invoker invoker = new DefaultInvoker();
        invoker.setWorkingDirectory(new File(root));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
        invoker.setErrorHandler(new PrintStreamHandler(new PrintStream(outputStream), true));
        invoker.setOutputHandler(new PrintStreamHandler(new PrintStream(errorStream), true));
        invoker.execute(request);
        String output = outputStream.toString();
        output += errorStream.toString();
        return output;
    }

    private String getContent(BufferedReader reader) throws IOException {
        String line;
        String content = "";
        while ((line = reader.readLine()) != null) {
            System.out.println("[IntegrationTest > Stderr] " + line);
            content += line;
        }
        return content;
    }

    @Test
    public void shouldCompileProjectWithSingleLessFile() throws IOException, InterruptedException, MavenInvocationException {

        String root = "target/test-project";

        String output = execute(root, "clean");
        the(output).shouldContain("BUILD SUCCESS");

        output = execute(root, "install", "-o");
        the(output).shouldContain("BUILD SUCCESS");

        File f = new File(root + "/target/web/bootstrap.css");
        a(f.exists()).shouldBeTrue();
    }

    @Test
    public void shouldCompileProjectWithMultipleLessFile() throws IOException, InterruptedException, MavenInvocationException {
        String root = "target/test-project-list";
        String output = execute(root, "clean");
        the(output).shouldContain("BUILD SUCCESS");
        output = execute(root, "install", "-o");
        the(output).shouldContain("BUILD SUCCESS");

        File f = new File(root + "/target/web1/bootstrap.css");
        a(f.exists()).shouldBeTrue();

        f = new File(root + "/target/web2/bootstrap.css");
        a(f.exists()).shouldBeTrue();
    }
}
