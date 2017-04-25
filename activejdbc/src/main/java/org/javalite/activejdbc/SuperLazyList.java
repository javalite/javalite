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


package org.javalite.activejdbc;

/**
 * The purpose of this class is to provide <code>toMaps()</code> method in cases of eager loading of dependencies.
 * This class is never used by application code directly, rather as a return value from <code>Model.getAll(..)</code>
 * methods.
 *
 * @author Igor Polevoy
 */
public class SuperLazyList<T extends Model> extends LazyList<T> {

    protected SuperLazyList(){}

    @Override
    public boolean add(T o) {
        return delegate.add(o);    
    }

    @Override
    protected void hydrate() {
        //NOP - do nothing
    }

    @Override
    public <E extends Model> LazyList<E> load() {
        throw new UnsupportedOperationException("not supported");
    }

    @Override
    public <E extends Model> LazyList<E> include(Class<? extends Model>... classes) {
        throw new UnsupportedOperationException("not supported");
    }

    @Override
    public <E extends Model> LazyList<E> orderBy(String orderBy) {
        throw new UnsupportedOperationException("not supported");
    }

    @Override
    public <E extends Model> LazyList<E> offset(long offset) {
        throw new UnsupportedOperationException("not supported");
    }

    @Override
    public <E extends Model> LazyList<E> limit(long limit) {
        throw new UnsupportedOperationException("not supported");
    }
}
