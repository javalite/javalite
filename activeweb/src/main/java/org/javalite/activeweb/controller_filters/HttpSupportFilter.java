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


import org.javalite.activeweb.HttpSupport;

/**
 * Adds support for access to HTTP parameters. Exists for backwards compatibility.
 *
 * Use {@link AppControllerFilter} in new projects.
 *
 * @author Igor Polevoy
 */
public class HttpSupportFilter extends HttpSupport {

    public void before() {
    }

    public void after() {
    }

    /**
     * To be implemented by application level filters. If there is an exception generated downstream,
     * the filters
     *
     * @param e exception.
     */
    public void onException(Exception e) {
    }

}
