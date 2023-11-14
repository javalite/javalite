package org.javalite.activejdbc;

import org.javalite.common.RuntimeUtil;
import org.junit.Test;

import java.io.File;

import static org.javalite.test.jspec.JSpec.the;

public class JavaAgentSpec {

    private static final String MVN = System.getProperty("os.name").toLowerCase().contains("win") ? "mvn.cmd" : "mvn";

    private static final File LOCATION =  new File("src/test/projects/javaagent");

    @Test
    public void shouldBuildAndRunProjectWithJavaAgent() {

        File javagentPath = new File("../activejdbc-instrumentation/target/activejdbc-instrumentation-3.1-SNAPSHOT.jar");

        System.out.println(new File(".").getAbsoluteFile());
        System.out.println(javagentPath.getAbsoluteFile());
        System.out.println(javagentPath.getPath());

        the(javagentPath.exists()).shouldBeTrue();


        RuntimeUtil.Response response = RuntimeUtil.execute(4096, LOCATION,
                MVN, "clean", "compile", "exec:java", "-Dexec.mainClass=org.javalite.activejdbc.Main", "-javaagent:" + javagentPath.getAbsolutePath(), "-Dactive_env=jenkins");

        System.out.println("=========");
        System.out.println(response.out);
        System.out.println("=========");

//        the(response.out).shouldContain("Failed to connect to JDBC URL: jdbc:mysql://jenkins/test-project-jenkins/jenkins");


    }
}
