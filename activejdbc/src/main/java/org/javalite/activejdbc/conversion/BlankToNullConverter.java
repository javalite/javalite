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

package org.javalite.activejdbc.conversion;

import static org.javalite.common.Util.*;

/**
 * Converts instances of <tt>String</tt> that are empty or contain only whitespaces to <tt>null</tt>.
 *
 * @author Eric Nielsen
 */
public enum BlankToNullConverter implements Converter<String, Object> {
    INSTANCE;

    public static BlankToNullConverter instance() { return INSTANCE; }

    /**
     * @param sourceClass source Class
     * @param destinationClass destination Class
     * @return true if sourceClass is String
     */
    @Override
    public boolean canConvert(Class sourceClass, Class destinationClass) {
        return String.class.equals(sourceClass);
    }

    /**
     * @param source instance of String
     * @return null if source is empty or contains only whitespaces, source otherwise
     */
    @Override
    public Object convert(String source) {
        return blank(source) ? null : source;
    }
}
