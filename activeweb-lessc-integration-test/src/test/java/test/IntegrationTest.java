package test;

import org.javalite.lessc.maven.AbstractIntegrationSpec;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Igor Polevoy on 5/22/15.
 */
public class IntegrationTest extends AbstractIntegrationSpec {

    @BeforeClass
    public static void before() throws IOException, InterruptedException {
//        System.out.println(execute(".", "clean"));
        System.out.println(execute(".", "install"));
    }

    @Test
    public void shouldCompileProjectWithSingleLessFile() throws IOException, InterruptedException {

        System.out.println(">>>>>>>>>>>> hello");
//        execute("target/test-project", "mvn clean package", "-o");
    }
}
