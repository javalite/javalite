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
public class OneToManyPolymorphicAssociation extends Association {

    private final String typeLabel;

    /**
     * @param source source table, the one that has many targets
     * @param target target table - many targets belong to source.
     * @param typeLabel
     */
    public OneToManyPolymorphicAssociation(Class<? extends Model> source, Class<? extends Model> target, String typeLabel) {
        super(source, target);
        this.typeLabel = typeLabel;
    }

    public String getTypeLabel() {
        return typeLabel;
    }

    @Override
    public String toString() {
        return getSourceClass().getSimpleName() + "  ----------<  " + getTargetClass().getSimpleName() + ", type: " + "has-many-polymorphic";
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !other.getClass().equals(getClass())) {
            return false;
        } else {
            OneToManyPolymorphicAssociation otherAss = (OneToManyPolymorphicAssociation) other;
            return otherAss.typeLabel.equals(typeLabel)
                    && otherAss.getSourceClass().equals(getSourceClass())
                    && otherAss.getTargetClass().equals(getTargetClass());
        }
    }
}