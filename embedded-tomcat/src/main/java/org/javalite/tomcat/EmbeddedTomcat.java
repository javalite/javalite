package org.javalite.tomcat;


import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.servlets.DefaultServlet;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.ContextResource;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.javalite.app_config.AppConfig;

import javax.servlet.RequestDispatcher;
import java.io.File;
import java.util.List;

import static org.javalite.app_config.AppConfig.p;
import static org.javalite.app_config.AppConfig.pInteger;

public class EmbeddedTomcat {

    public static void main(String[] args) throws LifecycleException {
        new EmbeddedTomcat().start();
    }

    public void start() throws LifecycleException {
        Tomcat tomcat = new Tomcat();
        tomcat.enableNaming();
        tomcat.setPort(pInteger("embedded.tomcat.port"));
        tomcat.getConnector();

        Context context = tomcat.addContext("", new File(".").getAbsolutePath());

        Tomcat.addServlet(context, "default", new DefaultServlet());
        context.addServletMappingDecoded("/", "default");

        // Configure JNDI DataSource in the context
        context.getNamingResources().addResource(prepareContextResource());

        ////////////  JavaLite  ////////
        FilterDef filterDef = new FilterDef();
        filterDef.addInitParameter("exclusions", "css,images,js/,echo,ico");
        filterDef.addInitParameter("root_controller", "default");
        filterDef.setFilterName(RequestDispatcher.class.getSimpleName());
        filterDef.setFilterClass(RequestDispatcher.class.getName());
        context.addFilterDef(filterDef);

        FilterMap filterMap = new FilterMap();
        filterMap.setFilterName(RequestDispatcher.class.getSimpleName());
        filterMap.addURLPattern("/*");
        context.addFilterMap(filterMap);
        ////////////////////////

        tomcat.start();
        tomcat.getServer().await();
    }

    private static ContextResource prepareContextResource() {

        ContextResource resource = new ContextResource();
        resource.setName("jdbc/" + p("embedded.pool.name"));
        resource.setAuth("Container");
        resource.setType("javax.sql.DataSource");
        resource.setProperty("factory", "org.apache.tomcat.dbcp.dbcp2.BasicDataSourceFactory"); // see inside for additional properties

        List<String> tomcatKeys = AppConfig.getKeys("embedded.tomcat");
        tomcatKeys.remove("embedded.tomcat.port");
        tomcatKeys.remove("embedded.pool.name");

        for (String propertyName : tomcatKeys) {
            String tomcatName = propertyName.substring("embedded.tomcat.".length());
            resource.setProperty(tomcatName, AppConfig.p(propertyName));
        }
        return resource;
    }
}