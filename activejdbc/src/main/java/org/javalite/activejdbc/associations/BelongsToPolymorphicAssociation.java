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
 *
 * @author Igor Polevoy
 */
public class BelongsToPolymorphicAssociation extends Association {

    private String typeLabel, parentClassName;

    public BelongsToPolymorphicAssociation(String source, String target, String typeLabel, String parentClassName) {
        super(source, target);
        this.typeLabel = typeLabel;
        this.parentClassName = parentClassName;
    }

    public String getParentClassName() {
        return parentClassName;
    }

    public String getTypeLabel() {
        return typeLabel;
    }

    @Override
    public String toString() {
        return new StringBuilder().append(getSource()).append("  >----------  ").append(getTarget())
                .append(", type: ").append("belongs-to-polymorphic").toString();
    }

    @Override
    public boolean equals(Object other) {

        if(other == null || !other.getClass().equals(getClass())){
            return false;
        }

        BelongsToPolymorphicAssociation otherAss =(BelongsToPolymorphicAssociation)other;

        return otherAss.typeLabel.equals(typeLabel)
                && otherAss.getSource().equals(getSource())
                && otherAss.getTarget().equals(getTarget());
    }
}