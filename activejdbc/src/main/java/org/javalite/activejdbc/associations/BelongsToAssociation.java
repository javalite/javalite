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


package org.javalite.activejdbc.associations;

import org.javalite.activejdbc.Association;

/**
 * This association indicates that a source table belongs to the target table.
 * It is used for one to many relationships.
 *
 * @author Igor Polevoy
 */
public class BelongsToAssociation extends Association {

    private String  fkName;

    public BelongsToAssociation(String source, String target, String fkName) {
        super(source, target);
        this.fkName = fkName;
    }

    public String getFkName() {
        return fkName;
    }
    
    @Override
    public String toString() {
        return new StringBuilder().append(getSource()).append("  >----------  ").append(getTarget())
                .append(", type: ").append("belongs-to").toString();
    }

    @Override
    public boolean equals(Object other) {
        
        if(other == null || !other.getClass().equals(getClass())){
            return false;
        }

        BelongsToAssociation otherAss =(BelongsToAssociation )other;

        return otherAss.fkName.equals(fkName)
                && otherAss.getSource().equals(getSource())
                && otherAss.getTarget().equals(getTarget());
    }
}
