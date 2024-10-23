package org.javalite.embedded.tomcat;

import org.apache.catalina.LifecycleException;
import org.javalite.tomcat.EmbeddedTomcat;

public class Main {
    public static void main(String[] args) throws LifecycleException {
        new EmbeddedTomcat().start();
    }
}
