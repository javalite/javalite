/*
Copyright 2009-2014 Igor Polevoy

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

package app.services;

/**
 * @author Igor Polevoy
 */
public class RedirectorImpl implements Redirector{
    public String getRedirectPath(String data) {
        if(data.contains("Google")){
            return "http://google.com";
        }else if(data.contains("Yahoo")){
            return "http://yahoo.com";
        }else{
            return "http://javalite.org";
        }
    }
}
