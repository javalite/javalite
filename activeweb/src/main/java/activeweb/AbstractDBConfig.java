/*
Copyright 2009-2010 Igor Polevoy 

Licensed under the Apache License, Version 2.0 (the "License"); 
you may not use this file except in compliance with the License. 
You may obtain a copy of the License at 

http://www.apache.org/licenses/LICENSE-2.0 

Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS, 
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
See the License for the specific language governing permissions and 
limitations under the License. 
*/
package activeweb;

/**
 * This class is designed to be sub-classed by an application level class called <code>app.config.DbConfig</code>.
 * It is used to configure database connections for various <strong>environments and modes</strong>.                                                       
 *
 * <h4> What is an environment?</h4>
 * An ActiveWeb environment is a computer where a  project executes. In the process of software development there can be
 * a number of environments where a project gets executed, such as development, continuous integration, QA, staging, production and more.
 * The number of environments for ActiveWeb is custom for every project.
 *
 * <h4>How to specify an environment</h4>
 * An environment is specified by an environment variable <strong>ACTIVE_ENV</strong>. Every computer where an ActiveWeb project
 * gets executed, needs to have this variable specified. This value is used to determine which DB connections need
 * to be initialized.
 *  
 * <h4>Default environment</h4>
 * In case an environment variable <code>ACTIVE_ENV</code> is not provided, the framework defaults to "development". 
 *
 * <h4>What is a mode?</h4>
 * ActiveWeb defines two modes of operation: "standard", which is also implicit, and "testing". Standard mode
 * is used during regular run of the program, and testing used during the build when tests are executed.
 * ActiveWeb promotes a style of development where one database used for testing, but a different one used under normal execution.
 * When tests are executed, a "test" database is used, and when a project is run in a normal mode, a "development"
 * database is used. Having a separate database for testing ensures safety of data in the development database.
 *
 * <h4> Example of configuration</h4>
 *<pre>
 * 1. public class DbConfig extends AbstractDBConfig {
 * 2.  public void init() {
 * 3.      environment("development").jndi("jdbc/kitchensink_development");
 * 4.      environment("development").testing().jdbc("com.mysql.jdbc.Driver", "jdbc:mysql://localhost/kitchensink_test", "root", "****");
 * 5.      environment("hudson").testing().jdbc("com.mysql.jdbc.Driver", "jdbc:mysql://172.30.64.31/kitchensink_test", "root", "****");
 * 6.      environment("production").jndi("jdbc/kitchensink_production");
 * 7.  }
 * 8.}
 * </pre>
 * The code above is an example from Kitchensink project. Lets examine it line by line.
 *
 * <ul>
 *  <li>Line 3: here we provide configuration for a "standard" mode in "development" environment. This DB connection
 * will be used when the application is running under normal conditions in development environment.
 *  <li>Line 4: This is a configuration of DB connection for "development" environment, but for "testing" mode. This
 * connection will be used by unit and integration tests during the build.
 *  <li>Line 5: This is a configuration of DB connection for "hudson" environment, but for "testing" mode. The "hudson"
 * environment is a computer where this project is built by Hudson - the continuous integration server. Since Hudson
 * computer is fully automated, and this project is not running there in "standard" mode, there is no standard configuration
 * for hudson environment, just one for testing.
 * <li>Line 6: This is configuration similar to one on line 3, but for "production" environment.
 * </ul>
 *
 *
 *
 * 
 * @author Igor Polevoy
 */
public abstract class AbstractDBConfig {

    public ConnectionBuilder environment(String environment){
        return new ConnectionBuilder(environment);
    }

    public abstract void init();
}
