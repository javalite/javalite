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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;

/**
 * @author Eric Nielsen
 */
public abstract class AbstractLazyList<E> implements List<E>, RandomAccess {

    protected List<E> delegate;

    protected abstract void hydrate();

    @Override
    public int size() {
        hydrate();
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        hydrate();
        return delegate.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        hydrate();
        return delegate.contains(o);
    }

    @Override
    public Iterator<E> iterator() {
        hydrate();
        return delegate.iterator();
    }

    @Override
    public Object[] toArray() {
        hydrate();
        return delegate.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        hydrate();
        return delegate.toArray(a);
    }

    @Override
    public boolean add(E element) {
        hydrate();
        return delegate.add(element);
    }

    @Override
    public boolean remove(Object o) {
        hydrate();
        return delegate.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        hydrate();
        return delegate.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        hydrate();
        return delegate.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        hydrate();
        return delegate.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        hydrate();
        return delegate.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        hydrate();
        return delegate.retainAll(c);
    }

    @Override
    public void clear() {
        if (delegate != null) {
            delegate.clear();
        } else {
            delegate = new ArrayList<>();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        hydrate();
        return delegate.equals(o);
    }

    @Override
    public int hashCode() {
        hydrate();
        return delegate.hashCode();
    }

    @Override
    public E get(int index) {
        hydrate();
        return delegate.get(index);
    }

    @Override
    public E set(int index, E element) {
        hydrate();
        return delegate.set(index, element);
    }

    @Override
    public void add(int index, E element) {
        hydrate();
        delegate.add(index, element);
    }

    @Override
    public E remove(int index) {
        hydrate();
        return delegate.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        hydrate();
        return delegate.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        hydrate();
        return delegate.lastIndexOf(o);
    }

    @Override
    public ListIterator<E> listIterator() {
        hydrate();
        return delegate.listIterator();
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        hydrate();
        return delegate.listIterator(index);
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        hydrate();
        return delegate.subList(fromIndex, toIndex);
    }

    @Override
    public String toString() {
        hydrate();
        return delegate.toString();
    }
}
