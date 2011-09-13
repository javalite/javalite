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

package app.filters;

import app.services.Redirector;
import com.google.inject.Inject;
import org.javalite.activeweb.controller_filters.HttpSupportFilter;

/**
 * @author Igor Polevoy
 */
public class InjectionRedirectFilter extends HttpSupportFilter {

    private Redirector redirector;

    public void before() {
        if (!blank("target")) {
            String path = redirector.getRedirectPath(param("target"));
            redirect(path);
        }
    }

    public void after() {

    }

    public void onException(Exception e) {

    }

    @Inject
    public void setRedirector(Redirector redirector) {
        this.redirector = redirector;
    }

}
