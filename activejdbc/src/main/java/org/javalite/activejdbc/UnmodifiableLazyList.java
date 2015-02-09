/*
Copyright 2009-2015 Igor Polevoy

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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * @author Eric Nielsen
 */
public abstract class UnmodifiableLazyList<E> implements List<E> {

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
        return new Iterator<E>() {
            private final Iterator<? extends E> it = delegate.iterator();
            @Override public boolean hasNext() { return it.hasNext(); }
            @Override public E next() { return it.next(); }
            @Override public void remove() { throw new UnsupportedOperationException(); }
        };
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
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        hydrate();
        return delegate.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(int index, E element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public E remove(int index) {
        throw new UnsupportedOperationException();
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
        return listIterator(0);
    }

    @Override
    public ListIterator<E> listIterator(final int index) {
        hydrate();
        return new ListIterator<E>() {
            private final ListIterator<? extends E> it = delegate.listIterator(index);
            @Override public boolean hasNext() { return it.hasNext(); }
            @Override public E next() { return it.next(); }
            @Override public boolean hasPrevious() { return it.hasPrevious(); }
            @Override public E previous() { return it.previous(); }
            @Override public int nextIndex() { return it.nextIndex(); }
            @Override public int previousIndex() { return it.previousIndex(); }

            @Override public void remove() { throw new UnsupportedOperationException(); }
            @Override public void set(E element) { throw new UnsupportedOperationException(); }
            @Override public void add(E element) { throw new UnsupportedOperationException(); }
	    };
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        hydrate();
        return Collections.unmodifiableList(delegate.subList(fromIndex, toIndex));
    }

    @Override
    public String toString() {
        hydrate();
        return delegate.toString();
    }
}
