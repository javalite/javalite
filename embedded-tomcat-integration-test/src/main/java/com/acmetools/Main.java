package com.acmetools;

import org.apache.catalina.LifecycleException;
import org.javalite.tomcat.EmbeddedTomcat;

public class Main {
    public static void main(String[] args) throws LifecycleException {
        EmbeddedTomcat tomcat = new EmbeddedTomcat();
        tomcat.start();
    }
}
