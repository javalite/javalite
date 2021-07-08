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

import static java.lang.String.format;

/**
 * Validates a boolean value in a JSON document.
 */
public class BooleanValidator extends JSONValidator {

    private final String booleanPath;
    private boolean expected;

    /**
     * Defaults to expect a "true" value at hte path. If the value at a path evaluates to false, the validator
     * will provide a corresponding message.
     *
     * @param booleanPath path ot a  boolean value in a JSON document
     */
    public BooleanValidator(String booleanPath) {
        this(booleanPath, true);
    }

    public BooleanValidator(String booleanPath, boolean expected) {
        this.booleanPath = booleanPath;
        this.expected = expected;
        setMessage(format("value under path '%s' is not %s", booleanPath, expected));
    }

    @Override
    public void validateJSONBase(JSONBase jsonBase) {
        if(this.expected != jsonBase.getBoolean(booleanPath)){
            jsonBase.addFailedValidator(this, booleanPath);
        }
    }
}
