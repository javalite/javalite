package org.javalite.app_config;

import org.javalite.common.RuntimeUtil;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.javalite.test.jspec.JSpec.the;

public class EnvironmentSpec {
    private File location =  new File("target/test-project");
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

        the(response.out).shouldContain("Failed to connect to JDBC URL: jdbc:mariadb://test-project-jenkins/jenkins");
    }

    @Test
    public void shouldUseEnvironmentVariable(){

        List<String> env = new ArrayList<>();
        env.add("ACTIVE_ENV=jenkins");

        RuntimeUtil.Response response = RuntimeUtil.execute(4096, location, env,
                "mvn", "-o", "clean", "compile", "exec:java", "-Dexec.mainClass=com.doe.example.Main");

        the(response.out).shouldContain("Failed to connect to JDBC URL: jdbc:mariadb://test-project-jenkins/jenkins");
    }


    @Test
    public void shouldOverrideEnvironmentVariableWithSystemProperty(){

        List<String> env = new ArrayList<>();
        env.add("ACTIVE_ENV=development");

        RuntimeUtil.Response response = RuntimeUtil.execute(4096, location, env,
                "mvn", "-o", "clean", "compile", "exec:java", "-Dexec.mainClass=com.doe.example.Main", "-Dactive_env=jenkins");

        the(response.out).shouldContain("Failed to connect to JDBC URL: jdbc:mariadb://test-project-jenkins/jenkins");
    }
}
