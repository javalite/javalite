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
//TODO: rename to ManyToManyAssociation, using "To" instead of "2" as the other class names?
public class Many2ManyAssociation extends Association {

    private final String sourceFkName;
    private final String targetFkName;
    private final String join;
    private final String targetPk;

    /**
     *
     * @param source name of source table in relationship
     * @param target name of target table in relationship
     * @param join name of join table in relationship
     * @param sourceFkName name of a foreign key in the join table pointing to the source table PK.
     * @param targetFkName  name of a foreign key in the join table pointing to the target table PK.
     * @param targetPk name of a PK of a target table
     */
    public Many2ManyAssociation(Class<? extends Model> source, Class<? extends Model> target, String join, String sourceFkName, String targetFkName, String targetPk) {
        super(source, target);
        this.targetFkName = targetFkName;
        this.sourceFkName = sourceFkName;
        this.join = join;
        this.targetPk = targetPk;
    }

    public Many2ManyAssociation(Class<? extends Model> sourceModelClass, Class<? extends Model> targetModelClass, String join, String sourceFkName, String targetFkName) {
        this(sourceModelClass, targetModelClass, join, sourceFkName, targetFkName, "id");
    }

    public String getSourceFkName() {
        return sourceFkName;
    }

    public String getTargetFkName() {
        return targetFkName;
    }

    public String getJoin() {
        return join;
    }

    public String getTargetPk() {
        return targetPk;
    }

    @Override
    public String toString() {
        return getSourceClass().getSimpleName() + "  >---------<  " + getTargetClass().getSimpleName() + ", type: " + "many-to-many" + ", join: " + join;
    }

    @Override
    public boolean equals(Object other) {

        if(other == null || !other.getClass().equals(getClass())){
            return false;
        }

        Many2ManyAssociation otherAss =(Many2ManyAssociation)other;

        return otherAss.getSourceClass().equals(getSourceClass())
                && otherAss.getTargetClass().equals(getTargetClass())
                && otherAss.getSourceFkName().equalsIgnoreCase(getSourceFkName())
                && otherAss.getTargetFkName().equalsIgnoreCase(getTargetFkName())
                && otherAss.getJoin().equalsIgnoreCase(getJoin());
    }
}
