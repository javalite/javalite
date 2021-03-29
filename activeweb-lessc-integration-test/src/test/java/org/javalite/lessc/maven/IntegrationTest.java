package org.javalite.lessc.maven;

import org.javalite.common.RuntimeUtil;
import org.javalite.common.Util;
import org.junit.Test;

import java.io.File;

import static org.javalite.test.jspec.JSpec.a;
import static org.javalite.test.jspec.JSpec.the;

/**
 * @author Igor Polevoy on 5/22/15.
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
    public void shouldCompileProjectWithSingleLessFile(){

        String root = "target/test-project";

        String output = execute(root, "-o",  "clean");
        the(output).shouldContain("BUILD SUCCESS");

        output = execute(root, "-o",   "compile");
        the(output).shouldContain("BUILD SUCCESS");

        File f = new File(root + "/target/web/bootstrap.css");
        a(f.exists()).shouldBeTrue();
    }

    @Test
    public void shouldCompileProjectWithMultipleLessFile(){
        String root = "target/test-project-list";
        String output = execute(root, "-o", "clean");
        the(output).shouldContain("BUILD SUCCESS");
        output = execute(root, "-o",  "compile");
        the(output).shouldContain("BUILD SUCCESS");
        the(output).shouldContain("--verbose");

        File f = new File(root + "/target/web1/bootstrap.css");
        a(f.exists()).shouldBeTrue();

        f = new File(root + "/target/web2/bootstrap.css");
        a(f.exists()).shouldBeTrue();
    }
}
