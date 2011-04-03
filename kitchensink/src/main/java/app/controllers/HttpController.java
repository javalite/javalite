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

package app.controllers;

import activeweb.AppController;
import activeweb.annotations.DELETE;
import activeweb.annotations.POST;
import activeweb.annotations.PUT;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * This controller exists to test the <code>javalite.http</code> library.
 *
 * @author Igor Polevoy
 */
public class HttpController extends AppController {

    public void get() throws ClassNotFoundException, IOException, URISyntaxException {



        respond("this is a get with params:" + params() + ", Content-type: " + header("Content-type"));
    }


    

    @POST
    public void post() throws IOException {
        respond("this is a post, you sent me: '" + getRequestString() + "'" + ", Content-type: " + header("Content-type"));
    }

    @DELETE
    public void delete() {
        respond("this is a delete with params:" + params());
    }


    @PUT
    public void put() throws IOException {
        respond("this is a delete, you sent me: '" + getRequestString() + "'");
    }
}
