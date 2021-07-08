/*
Copyright 2009-present Igor Polevoy

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
package org.javalite.json;

import org.javalite.validation.Validatable;
import org.javalite.validation.ValidatorAdapter;

/**
 * Superclass for JSON validators.
 */
public abstract class JSONValidator extends ValidatorAdapter {

    @Override
    public final void validate(Validatable validatable) {
        JSONBase base;
        try{
            base = (JSONBase) validatable;
            validateJSONBase(base);
        }catch(ClassCastException e){
            throw new IllegalArgumentException(validatable.getClass() + " is not an instance of " + JSONBase.class);
        }
    }

    public abstract void validateJSONBase(JSONBase jsonBase);
}
