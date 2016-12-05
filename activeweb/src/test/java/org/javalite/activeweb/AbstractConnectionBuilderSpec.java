package org.javalite.activeweb;

import org.javalite.activejdbc.connection_config.ConnectionJdbcSpec;
import org.javalite.activejdbc.connection_config.ConnectionJndiSpec;
import org.javalite.activejdbc.connection_config.ConnectionSpecWrapper;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.javalite.test.jspec.JSpec.a;
import static org.javalite.test.jspec.JSpec.the;

/**
 * @author Igor Polevoy on 12/1/14.
 */
public class AbstractConnectionBuilderSpec  {

    @Before
    public void before(){
        Configuration.resetConnectionWrappers();
    }

    @Test
    public void shouldConfigureJDBC(){
        class DBConfig extends AbstractDBConfig{
            public void init(AppContext appContext) {
                environment("development").testing().jdbc("com.mysql.jdbc.Driver", "jdbc:mysql://localhost/test123", "root", "****");
            }
        }

        DBConfig config = new DBConfig();
        config.init(null);

        ConnectionSpecWrapper wrapper = Configuration.getConnectionSpecWrappers().get(0);

        a(wrapper.getDbName()).shouldBeEqual("default");
        a(wrapper.isTesting()).shouldBeTrue();
        a(wrapper.getEnvironment()).shouldBeEqual("development");

        ConnectionJdbcSpec connectionSpec = (ConnectionJdbcSpec) wrapper.getConnectionSpec();

        a(connectionSpec.getDriver()).shouldBeEqual("com.mysql.jdbc.Driver");
        a(connectionSpec.getUrl()).shouldBeEqual("jdbc:mysql://localhost/test123");
        a(connectionSpec.getUser()).shouldBeEqual("root");
        a(connectionSpec.getPassword()).shouldBeEqual("****");
    }

    @Test
    public void shouldConfigureJNDI(){

        class DBConfig extends AbstractDBConfig{
            public void init(AppContext appContext) {
                environment("prod").db("second").jndi("jdbc/123_dev");
            }
        }

        DBConfig config = new DBConfig();
        config.init(null);

        ConnectionSpecWrapper wrapper = Configuration.getConnectionSpecWrappers("prod").get(0);

        a(wrapper.getDbName()).shouldBeEqual("second");
        a(wrapper.isTesting()).shouldBeFalse();
        a(wrapper.getEnvironment()).shouldBeEqual("prod");

        ConnectionJndiSpec connectionSpec = (ConnectionJndiSpec) wrapper.getConnectionSpec();
        a(connectionSpec.getDataSourceJndiName()).shouldBeEqual("jdbc/123_dev");
    }

    @Test
    public void shouldConfigureJndiFromFile(){

        class DBConfig extends AbstractDBConfig{
            public void init(AppContext appContext) {
                configFile("/database.properties");
            }
        }

        DBConfig config = new DBConfig();
        config.init(null);

        ConnectionSpecWrapper wrapper = Configuration.getConnectionSpecWrappers("production").get(0);

        a(wrapper.getDbName()).shouldBeEqual("default");
        a(wrapper.isTesting()).shouldBeFalse();
        a(wrapper.getEnvironment()).shouldBeEqual("production");

        ConnectionJndiSpec connectionSpec = (ConnectionJndiSpec) wrapper.getConnectionSpec();
        a(connectionSpec.getDataSourceJndiName()).shouldBeEqual("java:comp/env/jdbc/prod");
    }

    @Test
    public void shouldConfigureJdbcFromFile(){

        class DBConfig extends AbstractDBConfig{
            public void init(AppContext appContext) {
                configFile("/database.properties");
            }
        }

        DBConfig config = new DBConfig();
        config.init(null);

        //test first connection spec
        ConnectionSpecWrapper wrapper = Configuration.getConnectionSpecWrappers("development").get(0);
        a(wrapper.getDbName()).shouldBeEqual("default");
        a(wrapper.getEnvironment()).shouldBeEqual("development");
        a(wrapper.isTesting()).shouldBeFalse();

        ConnectionJdbcSpec connectionSpec = (ConnectionJdbcSpec) wrapper.getConnectionSpec();

        a(connectionSpec.getDriver()).shouldBeEqual("com.mysql.jdbc.Driver");
        a(connectionSpec.getUrl()).shouldBeEqual("jdbc:mysql://localhost/proj_dev");
        a(connectionSpec.getUser()).shouldBeEqual("john");
        a(connectionSpec.getPassword()).shouldBeEqual("pwd");

        //test second connection spec
        wrapper = Configuration.getConnectionSpecWrappers("development").get(1);
        a(wrapper.getDbName()).shouldBeEqual("default");
        a(wrapper.getEnvironment()).shouldBeEqual("development");
        a(wrapper.isTesting()).shouldBeTrue();

        connectionSpec = (ConnectionJdbcSpec) wrapper.getConnectionSpec();

        a(connectionSpec.getDriver()).shouldBeEqual("com.mysql.jdbc.Driver");
        a(connectionSpec.getUrl()).shouldBeEqual("jdbc:mysql://localhost/test");
        a(connectionSpec.getUser()).shouldBeEqual("mary");
        a(connectionSpec.getPassword()).shouldBeEqual("pwd1");
    }



    @Test
    public void shouldOverrideConnectionSpecForTheSameEnvironment(){

        class DBConfig extends AbstractDBConfig{
            public void init(AppContext appContext) {
                configFile("/database1.properties");
                environment("production", true).jndi("java:comp/env/jdbc/prod_new");
            }
        }

        DBConfig config = new DBConfig();
        config.init(null);

        List<ConnectionSpecWrapper> wrappers = Configuration.getConnectionSpecWrappers("production");

        //we configured two for production, one in file, one in class. But the class config overrides one in file.
        the(wrappers.size()).shouldBeEqual(1);

        ConnectionJndiSpec connectionSpec = (ConnectionJndiSpec)  wrappers.get(0).getConnectionSpec();
        the(connectionSpec.getDataSourceJndiName()).shouldBeEqual("java:comp/env/jdbc/prod_new");
    }


    /**
     * This feature is needed because often times, you have different configuration locally vs another
     * environment where you need to run tests. It is possible to achieve with Maven profiles, but kind of hacky.
     * Example: development env requires to connect to localhost. Jenkins when running tests, requires to connect to jenkins_db host.
     * This feature is not specific to Jenkins :)
     *
     * The new solution is clean.
     */
    @Test
    public void should_configure_different_test_configs_for_development_and_jenkins(){

        class DBConfig extends AbstractDBConfig{
            public void init(AppContext appContext) {
                configFile("/database2.properties");
            }
        }

        DBConfig config = new DBConfig();
        config.init(null);

        List<ConnectionSpecWrapper> wrappers = Configuration.getConnectionSpecWrappers("development");

        the(wrappers.size()).shouldBeEqual(2);


        ConnectionSpecWrapper dev = null;
        ConnectionSpecWrapper test = null;

        //have to do this because the order of specs is not deterministic
        for (ConnectionSpecWrapper connectionSpecWrapper : wrappers) {
            if(connectionSpecWrapper.isTesting()){
                test = connectionSpecWrapper;
            }else {
                dev = connectionSpecWrapper;
            }
        }
        the(((ConnectionJdbcSpec)test.getConnectionSpec()).getUrl()).shouldBeEqual("jdbc:mysql://localhost/test");
        the(((ConnectionJdbcSpec)dev.getConnectionSpec()).getUrl()).shouldBeEqual("jdbc:mysql://localhost/dev");
    }
}