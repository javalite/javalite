package org.javalite.http;
//
//import org.eclipse.jetty.server.Server;
//import org.eclipse.jetty.server.handler.AllowSymLinkAliasChecker;
//import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class HttpSpec {

//    private Server server;
    @Before
    public void before() throws Exception {
//        server = new Server(8080);
//
//        WebAppContext webapp = new WebAppContext("src/test/webapp", "/");
//
//        webapp.addAliasCheck(new AllowSymLinkAliasChecker());
//        server.setHandler(webapp);
//        server.start();
//        server.dumpStdErr();
//        server.join();
    }

    @After
    public void after() throws Exception {
//        server.stop();
    }

    @Test
    public void shouldTestGet(){

        System.out.println(Http.get("http://localhost:8080").text());
        System.out.println("hello");

    }

}
