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

import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * A case insensitive map for <code>java.lang.String</code> keys. The current implementation is based on
 * {@link ConcurrentSkipListMap}.
 *
 * @author Eric Nielsen
 * @author Igor Polevoy
 */
public class CaseInsensitiveMap<V> extends ConcurrentSkipListMap<String, V> {

    enum  Null {
        INSTANCE;

        @Override
        public String toString() {
            return "null";
        }
    }

    public CaseInsensitiveMap() {
        super(String.CASE_INSENSITIVE_ORDER);
    }

    public CaseInsensitiveMap(Map<? extends String, V> m) {
        this();
        putAll(m);
    }

    @Override
    public V get(Object key) {
        V v = super.get(key);
        return v == Null.INSTANCE ? null : v;
    }

    @Override
    public void putAll(Map<? extends String, ? extends V> m) {
        for(String key: m.keySet()){
            put(key, m.get(key));
        }
    }


    @Override
    public V put(String key, V value) {
        return super.put(key, value == null ? ((V)Null.INSTANCE) : value);
    }
}
