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


package org.javalite.activejdbc.associations;

import org.javalite.activejdbc.Association;
import org.javalite.activejdbc.Model;

/**
 * @author Igor Polevoy
 */
public class OneToManyAssociation extends Association {

    private final String fkName;

    /**
     * @param sourceModelClass source class, the one that has many targets
     * @param targetModelClass target class - many targets belong to source.
     * @param fkName name of a foreign key in teh target table.
     */
    public OneToManyAssociation(Class<? extends Model> sourceModelClass, Class<? extends Model> targetModelClass, String fkName) {
        super(sourceModelClass, targetModelClass);
        this.fkName = fkName;
    }

    public String getFkName() {
        return fkName;
    }

    @Override
    public String toString() {
        return getSourceClass().getSimpleName() + "  ----------<  " + getTargetClass().getSimpleName() + ", type: " + "has-many";
    }

    @Override
    public boolean equals(Object other) {

        if (other == null || !other.getClass().equals(getClass())) {
            return false;
        } else {
            OneToManyAssociation otherAss = (OneToManyAssociation) other;
            return otherAss.fkName.equals(fkName)
                    && otherAss.getSourceClass().equals(getSourceClass())
                    && otherAss.getTargetClass().equals(getTargetClass());
        }
    }
}
