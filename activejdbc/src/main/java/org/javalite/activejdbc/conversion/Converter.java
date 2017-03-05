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
 * Converts instances of <tt>S</tt> to <tt>T</tt>.
 * @param <S> Source type
 * @param <T> Destination type
 *
 * @author Eric Nielsen
 */
public interface Converter<S, T> {

    /**
     * Returns <tt>true</tt> if this converter can convert instances of <tt>sourceClass</tt> to
     * <tt>destinationClass</tt>.
     * @param sourceClass source Class
     * @param destinationClass destination Class
     * @return true if this converter can convert instances of sourceClass to destinationClass, false otherwise
     */
    boolean canConvert(Class sourceClass, Class destinationClass);

    /**
     * Converts instance of <tt>S</tt> to <tt>T</tt>.
     * @param source instance of S
     * @return instance of S converted to type T
     */
    T convert(S source);
}
