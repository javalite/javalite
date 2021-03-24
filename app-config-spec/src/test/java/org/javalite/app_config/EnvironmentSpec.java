package org.javalite.app_config;

import org.javalite.common.RuntimeUtil;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.javalite.test.jspec.JSpec.the;


//TODO: need to move this to the ActiveJDBC  module because this test has a dependency  on  it from the tested projects



public class EnvironmentSpec {

    private List<String> environmentVariables;

    @Before
    public void grabEnvironmentVariables(){
        environmentVariables = new ArrayList<>();
        Map<String, String> systemVars  = System.getenv();
        for(String name: systemVars.keySet()) {
            environmentVariables.add(name + "=" + systemVars.get(name));
        }
    }


    private File location =  new File("src/test/project/test-project");
    @Test
    public void shouldAttemptDefaultConnection(){
        RuntimeUtil.Response response = RuntimeUtil.execute(4096, location,
                "mvn",  "-o",  "clean", "compile", "exec:java", "-Dexec.mainClass=com.doe.example.Main");
        the(response.out).shouldContain("Failed to connect to JDBC URL: jdbc:mysql://localhost/test-project_development");
    }

    @Test
    public void shouldUseSystemProperty(){
        RuntimeUtil.Response response = RuntimeUtil.execute(4096, location,
                "mvn","-o",  "clean", "compile", "exec:java", "-Dexec.mainClass=com.doe.example.Main", "-Dactive_env=jenkins");

        the(response.out).shouldContain("Failed to connect to JDBC URL: jdbc:mysql://jenkins/test-project-jenkins/jenkins");
    }

    @Test
    public void shouldUseEnvironmentVariable(){
        environmentVariables.add("ACTIVE_ENV=jenkins");

        RuntimeUtil.Response response = RuntimeUtil.execute(4096, location, environmentVariables,
                "mvn", "-o", "clean", "compile", "exec:java", "-Dexec.mainClass=com.doe.example.Main");

        the(response.out).shouldContain("Failed to connect to JDBC URL: jdbc:mysql://jenkins/test-project-jenkins/jenkins");
    }


    @Test
    public void shouldOverrideEnvironmentVariableWithSystemProperty(){
        environmentVariables.add("ACTIVE_ENV=development");

        RuntimeUtil.Response response = RuntimeUtil.execute(4096, location, environmentVariables,
                "mvn", "-o", "clean", "compile", "exec:java", "-Dexec.mainClass=com.doe.example.Main", "-Dactive_env=jenkins");
        the(response.out).shouldContain("Failed to connect to JDBC URL: jdbc:mysql://jenkins/test-project-jenkins/jenkins");
    }
}
