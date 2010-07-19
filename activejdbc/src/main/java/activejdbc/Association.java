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


package activejdbc;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Associations are synonymous with relationships. However, in some cases, the
 * DB might have referential integrity constraints. ActiveJDBC does not account for DB referential integrity
 * constraints, associations rather based on conventions and convention overrides.
 *
 * @author Igor Polevoy
 */
public class Association implements Serializable {

    private String source, target;

    
    protected Association(String source, String target) {
        this.source = source;
        this.target = target;
    }

    public String getSource() {
        return source;
    }

    public String getTarget() {
        return target;
    }


    @Override
    public int hashCode() {
        return toString().hashCode();    
    }
}
