package org.javalite.app_config;

import org.javalite.common.RuntimeUtil;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.javalite.test.jspec.JSpec.the;


//TODO: need to move this to the ActiveJDBC  module because this test has a dependency  on  it from the tested projects


/**
 * This test will attempt to  connect to non-existent databases. This is by design.
 * It is only testing that the framework picks up the right configuration depending under different conditions.
 *
 */
public class EnvironmentSpec {
    private final File location =  new File("src/test/project/test-project");

    private static final int BUFFER_SIZE = 200000;
    @Test
    public void shouldAttemptDefaultConnection(){
        RuntimeUtil.Response response = RuntimeUtil.execute(BUFFER_SIZE, location,
                "mvn",  "clean", "compile", "exec:java", "-Dexec.mainClass=com.doe.example.Main");
        the(response.out).shouldContain("Failed to connect to JDBC URL: jdbc:mysql://localhost-local/test-project_development with user: hello");
        the(response.out).shouldContain("Communications link failure");
    }

    @Test
    public void shouldUseSystemProperty(){
        RuntimeUtil.Response response = RuntimeUtil.execute(BUFFER_SIZE, location,
                "mvn",  "clean", "compile", "exec:java", "-Dexec.mainClass=com.doe.example.Main", "-Dactive_env=jenkins");
        checkJenkins(response.out);
    }

    @Test
    public void shouldUseEnvironmentVariable(){

        List<String> env = new ArrayList<>();
        env.add("ACTIVE_ENV=jenkins");

        RuntimeUtil.Response response = RuntimeUtil.execute(BUFFER_SIZE, location, env,
                "mvn", "clean", "compile", "exec:java", "-Dexec.mainClass=com.doe.example.Main");
        checkJenkins(response.out);
    }


    @Test
    public void shouldOverrideEnvironmentVariableWithSystemProperty(){

        List<String> env = new ArrayList<>();
        env.add("ACTIVE_ENV=development");

        RuntimeUtil.Response response = RuntimeUtil.execute(BUFFER_SIZE, location, env,
                "mvn", "clean", "compile", "exec:java", "-Dexec.mainClass=com.doe.example.Main", "-Dactive_env=jenkins");
        checkJenkins(response.out);
    }

    private void checkJenkins(String out){
        the(out).shouldContain("Failed to connect to JDBC URL: jdbc:mysql://test-project-jenkins/jenkins with user: hello");
        the(out).shouldContain("Communications link failure");
    }
}
