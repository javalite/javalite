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

/**
 * Converts instances of <tt>Number</tt> that are zero to <tt>null</tt>.
 *
 * @author Eric Nielsen
 */
public enum ZeroToNullConverter implements Converter<Number, Object> {
    INSTANCE;

    public static ZeroToNullConverter instance() { return INSTANCE; }

    /**
     * @param sourceClass source Class
     * @param destinationClass destination Class
     * @return true if sourceClass is a subclass of Number
     */
    @Override
    public boolean canConvert(Class sourceClass, Class destinationClass) {
        return Number.class.isAssignableFrom(sourceClass);
    }

    /**
     * @param source instance of String
     * @return null if source.intValue() is zero, source otherwise
     */
    @Override
    public Object convert(Number source) {
        return source.intValue() == 0 ? null : source;
    }
}
