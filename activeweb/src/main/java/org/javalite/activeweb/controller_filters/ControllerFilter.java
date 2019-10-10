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
package org.javalite.activeweb.controller_filters;


/**
 *
 * Controller filters are similar to that of Servlet filters, but designed to wrap execution of controllers.
 * They can be used for many tasks that need to trigger before and after execution of a controller, such as login in, loggin,
 * opening a DB connection, timing, etc.
 *
 *  <p/><p/>
 * Instances of filters are <font color="red"><em>not thread safe</em></font>.
 * The same object will be reused across many threads at the same time. Create instance variables at your own peril.
 * 
 * @author Igor Polevoy
 */
public interface ControllerFilter {

    /**
     * Called by framework before executing a controller
     */
    void before();

    /**
     * Called by framework after executing a controller
     */
    void after();

    /**
     * Called by framework in case there was an exception inside a controller
     *
     * @param e exception.
     */
    void onException(Exception e);
}
