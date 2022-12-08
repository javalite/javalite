/*
Copyright 2009-(CURRENT YEAR) Igor Polevoy

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

package org.javalite.activeweb;

import org.javalite.activejdbc.connection_config.DBConfiguration;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.mock.web.MockFilterConfig;

import javax.servlet.ServletException;

/**
 *
 * Bootstraps entire application, including AppControllerConfig class, which sets up all filters exactly as at run time.
 * If a {@link org.javalite.activeweb.controller_filters.DBConnectionFilter} is used in the application, it is bypassed.
 * Instead,  the DB connection to a test DB is made from test configuration. A connection is opened to
 * a test DB, transaction is started before each test. After each test, a connection is closed and a transaction
 * is rolled back.
 *
 *
 * @author Igor Polevoy
 */
public abstract class AppIntegrationSpec extends IntegrationSpec{

    private boolean suppressDb;
    private AppContext context;
    private RequestDispatcher requestDispatcher = new RequestDispatcher();
    private boolean rollback = true;

    /**
     * Current state of 'rollback' flag.
     *
     * @return Current state of 'rollback' flag.
     */
    public boolean rollback() {
        return rollback;
    }

    /**
     * Set to true in order  to rollback a transaction at the end of the test (default is true).
     * This method will set the <code>autocommit = !rollback</code> on all connections found
     * on this thread.
     *
     * <p>
     *     <em>
     *     WARNING: if you set this value to false inside your test, the framework will not
     *     clean any remaining data you insert into your test database. Basically, this is a
     *     "manual mode" where you are responsible for cleaning after yourself.
     *     </em>
     * </p>
     *
     * @param rollback true to rollback transactions at the end of the test, false to not rollback.
     */
    public void setRollback(boolean rollback) {
        this.rollback = rollback;
        DBSpecHelper.setRollback(rollback);
    }


    @BeforeClass @BeforeAll
    public static void initDBConfig() {
        DBSpecHelper.initDBConfig();
    }

    @Before @BeforeEach
    public void beforeAppIntegrationSpec() throws ServletException {
        requestDispatcher.init(new MockFilterConfig());
        context = requestDispatcher.getContext();

        if(!suppressDb){
            DBConfiguration.openTestConnections(rollback);
        }
    }

    @After @AfterEach
    public void closeTestConnections() {
        if(!suppressDb){
            DBConfiguration.closeTestConnections(rollback);
        }
        requestDispatcher.destroy();
    }

    @AfterClass  @AfterAll
    public static void clearConnectionConfigs() {
        DBConfiguration.clearConnectionConfigs();
    }

    /**
     * Returns instance of {@link AppContext}
     * @return instance of {@link AppContext}
     */
    public AppContext getContext() {
        return context;
    }

    /**
     * Takes controller path. A controller path is a full path  to controller starting from context and ending in a
     * name of a controller on URI.
     *
     * @param controllerPath path to controller. Example: <code>/admin/permissions</code> where "admin" is a sub-package of controller
     * and "permissions" is a name of controller. Such path implies a name of a controller class:
     * <code>app.controllers.admin.PermissionsController</code>.
     * Controller paths always starts with a slash: "/".
     *
     * @return instance of a builder to help define request.
     */
    @Override
    protected RequestBuilder controller(String controllerPath){
        return new RequestBuilder(controllerPath);
    }


    /**
     * Call this method from a constructor of your spec in cases you do not need DB connections.
     * Calling from a "before" method will not work.
     */
    protected void suppressDb(){
        suppressDb = true;
    }
}
