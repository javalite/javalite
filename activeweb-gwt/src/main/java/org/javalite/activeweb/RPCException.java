/*
Copyright 2010-2011 Max Artyukhov

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


/**
 *
 * @author Max Artyukhov
 */
class RPCException extends Exception {
    
    public RPCException() {
        super();    
    }

    public RPCException(String message) {
        super(message);    
    }

    public RPCException(String message, Throwable cause) {
        super(message, cause);    
    }

    public RPCException(Throwable cause) {
        super(cause);    
    }

    @Override
    public String getMessage() {
        String message = super.getMessage();
        if(getCause() != null){
            message += "; " + getCause().getMessage(); 
        }
        return message;
    }
    
}
