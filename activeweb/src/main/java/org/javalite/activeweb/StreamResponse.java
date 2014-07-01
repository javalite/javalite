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
package org.javalite.activeweb;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Igor Polevoy
 */
class StreamResponse extends ControllerResponse{

    private InputStream in;

    StreamResponse(InputStream in){
        this.in = in;
    }
    @Override
    void doProcess() {
        try{
            OutputStream out = Context.getHttpResponse().getOutputStream();
            byte[] bytes = new byte[1024];

            int x;
            while((x = in.read(bytes)) != -1){
                out.write(bytes, 0, x);
            }
            out.flush();
        }
        catch(Exception e){
            throw new ControllerException(e);
        }
    }
}
