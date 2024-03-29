/*
Copyright 2009-2019 Igor Polevoy

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

package org.javalite.conversion;

/**
 * Converts instances of <tt>S</tt> to <tt>T</tt>.
 * @param <S> Source type
 * @param <T> Destination type
 *
 * @author Eric Nielsen
 */
public abstract class ConverterAdapter<S, T> implements Converter<S, T> {

    @Override
    public boolean canConvert(Class<S> aSourceClass, Class<T> aDestinationClass) {
        return sourceClass().isAssignableFrom(aSourceClass) && destinationClass().isAssignableFrom(aDestinationClass);
    }

    protected abstract Class<S> sourceClass();

    protected abstract Class<T> destinationClass();

    @Override
    public T convert(S source) {
        try {
            return doConvert(source);
        } catch (Exception e) {
            throw new ConversionException(e);
        }
    }

    /**
     * Converts instance of <tt>S</tt> to <tt>T</tt>.
     * @param source instance of S, can be null
     * @return instance of S converted to type T
     */
    protected abstract T doConvert(S source) throws Exception;
}
