/*
Copyright 2009-2016 Igor Polevoy

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

import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.mock.web.MockFilterConfig;

import javax.servlet.ServletException;

/**
 *
 * Bootstraps entire application, including AppControllerConfig class, which sets up all filters exactly as at run time.
 * If a {@link org.javalite.activeweb.controller_filters.DBConnectionFilter} is used in the application, it is bypassed.
 * Instead,  the DB connection to a test DB is made from a super class. A connection is opened to
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

    @Before @BeforeEach
    public void beforeAppIntegrationSpec() throws ServletException {
        requestDispatcher.init(new MockFilterConfig());
        context = requestDispatcher.getContext();

        if(!suppressDb){
            DBSpecHelper.openTestConnections();
        }
    }

    @After @AfterEach
    public void afterAppIntegrationSpec() {

        if(!suppressDb){
            DBSpecHelper.closeTestConnections();
            DBSpecHelper.clearConnectionWrappers();
        }

        requestDispatcher.destroy();
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
        return new RequestBuilder(controllerPath, session());
    }


    /**
     * Call this method from a constructor of your spec in cases you do not need DB connections.
     * Calling from a "before" method will not work.
     */
    protected void suppressDb(){
        suppressDb = true;
    }
}
