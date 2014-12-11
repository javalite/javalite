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
 * @author Igor Polevoy
 */
public class OneToManyAssociation extends Association {

    private final String fkName;

    /**
     * @param source source table, the one that has many targets
     * @param target target table - many targets belong to source.
     * @param fkName name of a foreign key in teh target table.
     */
    public OneToManyAssociation(String source, String target, String fkName) {
        super(source, target);
        this.fkName = fkName;
    }

    public String getFkName() {
        return fkName;
    }

    @Override
    public String toString() {
        return new StringBuilder().append(getSource()).append("  ----------<  ").append(getTarget())
                .append(", type: ").append("has-many").toString();
    }

    @Override
    public boolean equals(Object other) {

        if(other == null || !other.getClass().equals(getClass())){
            return false;
        }

        OneToManyAssociation otherAss =(OneToManyAssociation)other;

        return otherAss.fkName.equals(fkName)
                && otherAss.getSource().equals(getSource())
                && otherAss.getTarget().equals(getTarget());
    }
}
