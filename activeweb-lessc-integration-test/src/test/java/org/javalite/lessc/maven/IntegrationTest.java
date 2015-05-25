package org.javalite.lessc.maven;

import org.junit.Test;

import java.io.*;

import static org.javalite.test.jspec.JSpec.a;
import static org.javalite.test.jspec.JSpec.the;

/**
 * @author Igor Polevoy on 5/22/15.
 */
public class IntegrationTest {

    String maven = System.getProperty("os.name").contains("Windows") ? "cmd.exe /c mvn" : "mvn";

    protected String execute(String dir, String... args) throws IOException, InterruptedException {
        InputStream stdErr, stdOut;
        Process process = Runtime.getRuntime().exec(args, new String[]{"JAVA_HOME=" + System.getProperty("java.home")}, new File(dir));
        stdErr = process.getErrorStream();
        stdOut = process.getInputStream();
        BufferedReader reader =  new BufferedReader(new InputStreamReader(stdOut));
        String content = getContent(reader);
        reader.close();
        reader = new BufferedReader(new InputStreamReader(stdErr));
        content += getContent(reader);
        reader.close();
        return content;
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
    public void shouldCompileProjectWithSingleLessFile() throws IOException, InterruptedException {

        String root = "target/test-project";

        String output = execute(root, maven, "clean");
        the(output).shouldContain("BUILD SUCCESS");
        output = execute(root, "mvn", "install", "-o");
        the(output).shouldContain("BUILD SUCCESS");

        File f = new File(root + "/target/web/bootstrap.css");
        a(f.exists()).shouldBeTrue();
    }

    @Test
    public void shouldCompileProjectWithMultipleLessFile() throws IOException, InterruptedException {
        String root = "target/test-project-list";
        String output = execute(root, maven, "clean");
        the(output).shouldContain("BUILD SUCCESS");
        output = execute(root, maven, "install", "-o");
        the(output).shouldContain("BUILD SUCCESS");

        File f = new File(root + "/target/web1/bootstrap.css");
        a(f.exists()).shouldBeTrue();

        f = new File(root + "/target/web2/bootstrap.css");
        a(f.exists()).shouldBeTrue();
    }
}
