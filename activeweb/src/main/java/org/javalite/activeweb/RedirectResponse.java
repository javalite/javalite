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

import java.net.URL;

/**
 * @author Igor Polevoy
 */
class RedirectResponse extends ControllerResponse {
    private URL url;
    private String path;

    protected RedirectResponse(URL url) {
        if(url == null) throw new IllegalArgumentException("url can't be null");
        this.url = url;
        setStatus(302);
    }

    protected RedirectResponse(String path) {
        if(path == null) throw new IllegalArgumentException("url can't be null");
        this.path = path;
        setStatus(302);
    }

    @Override
    void doProcess() {
        try{
            if(url != null){
                RequestContext.getHttpResponse().sendRedirect(url.toString());
            }else if(path != null){
                RequestContext.getHttpResponse().sendRedirect(path);
            }
        }
        catch(Exception e){
            throw new ControllerException(e);
        }
    }

    public String redirectValue() {
        return path != null? path :url.toString();
    }
}
