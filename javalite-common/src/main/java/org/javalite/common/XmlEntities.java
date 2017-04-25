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



package org.javalite.common;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 *
 * <p>
 * Provides XML entity utilities.
 * </p>
 *
 * This code was taken from Apache Commons in order to avoid adding a new dependency.
 *
 * @author <a href="mailto:alex@purpletech.com">Alexander Day Chaffee</a>
 * @author <a href="mailto:ggregory@seagullsw.com">Gary Gregory</a>
 * @since 2.0
 * @version $Id: Entities.java 911986 2010-02-19 21:19:05Z niallp $
 * @deprecated use Apache Commons or {@link Escape} instead
 */
@Deprecated
public class XmlEntities {

    private static final String[][] BASIC_ARRAY = {{"quot", "34"}, // " - double-quote
        {"amp", "38"}, // & - ampersand
        {"lt", "60"}, // < - less-than
        {"gt", "62"}, // > - greater-than
    };

    private static final String[][] APOS_ARRAY = {{"apos", "39"}, // XML apostrophe
    };

    /**
     * <p>
     * The set of entities supported by standard XML.
     * </p>
     */
    public static final XmlEntities XML;


    static {
        XmlEntities xml = new XmlEntities();
        xml.addEntities(BASIC_ARRAY);
        xml.addEntities(APOS_ARRAY);
        XML = xml;
    }



    static interface EntityMap {
        /**
         * <p>
         * Add an entry to this entity map.
         * </p>
         *
         * @param name
         *            the entity name
         * @param value
         *            the entity value
         */
        void add(String name, int value);

        /**
         * <p>
         * Returns the name of the entity identified by the specified value.
         * </p>
         *
         * @param value
         *            the value to locate
         * @return entity name associated with the specified value
         */
        String name(int value);

        /**
         * <p>
         * Returns the value of the entity identified by the specified name.
         * </p>
         *
         * @param name
         *            the name to locate
         * @return entity value associated with the specified name
         */
        int value(String name);
    }

    static class PrimitiveEntityMap implements EntityMap {
        private final Map mapNameToValue = new HashMap();

        private final HashMap mapValueToName = new HashMap();

        /**
         * {@inheritDoc}
         */
        // TODO not thread-safe as there is a window between changing the two maps
        public void add(String name, int value) {
            mapNameToValue.put(name, Integer.valueOf(value));
            mapValueToName.put(value, name);
        }

        /**
         * {@inheritDoc}
         */
        public String name(int value) {
            return (String) mapValueToName.get(value);
        }

        /**
         * {@inheritDoc}
         */
        public int value(String name) {
            Object value = mapNameToValue.get(name);
            if (value == null) {
                return -1;
            }
            return ((Integer) value).intValue();
        }
    }

    abstract static class MapIntMap implements XmlEntities.EntityMap {
        protected final Map mapNameToValue;

        protected final Map mapValueToName;

        /**
         * Construct a new instance with specified maps.
         *
         * @param nameToValue name to value map
         * @param valueToName value to name map
         */
        MapIntMap(Map nameToValue, Map valueToName){
            mapNameToValue = nameToValue;
            mapValueToName = valueToName;
        }

        /**
         * {@inheritDoc}
         */
        public void add(String name, int value) {
            mapNameToValue.put(name, Integer.valueOf(value));
            mapValueToName.put(Integer.valueOf(value), name);
        }

        /**
         * {@inheritDoc}
         */
        public String name(int value) {
            return (String) mapValueToName.get(Integer.valueOf(value));
        }

        /**
         * {@inheritDoc}
         */
        public int value(String name) {
            Object value = mapNameToValue.get(name);
            if (value == null) {
                return -1;
            }
            return ((Integer) value).intValue();
        }
    }

    static class HashEntityMap extends MapIntMap {
        /**
         * Constructs a new instance of <code>HashEntityMap</code>.
         */
        public HashEntityMap() {
            super(new HashMap(), new HashMap());
        }
    }

    static class TreeEntityMap extends MapIntMap {
        /**
         * Constructs a new instance of <code>TreeEntityMap</code>.
         */
        public TreeEntityMap() {
            super(new TreeMap(), new TreeMap());
        }
    }

    static class LookupEntityMap extends PrimitiveEntityMap {
        // TODO this class is not thread-safe
        private String[] lookupTable;

        private final int LOOKUP_TABLE_SIZE = 256;

        /**
         * {@inheritDoc}
         */
        public String name(int value) {
            if (value < LOOKUP_TABLE_SIZE) {
                return lookupTable()[value];
            }
            return super.name(value);
        }

        /**
         * <p>
         * Returns the lookup table for this entity map. The lookup table is created if it has not been previously.
         * </p>
         *
         * @return the lookup table
         */
        private String[] lookupTable() {
            if (lookupTable == null) {
                createLookupTable();
            }
            return lookupTable;
        }

        /**
         * <p>
         * Creates an entity lookup table of LOOKUP_TABLE_SIZE elements, initialized with entity names.
         * </p>
         */
        private void createLookupTable() {
            lookupTable = new String[LOOKUP_TABLE_SIZE];
            for (int i = 0; i < LOOKUP_TABLE_SIZE; ++i) {
                lookupTable[i] = super.name(i);
            }
        }
    }

    static class ArrayEntityMap implements EntityMap {
        // TODO this class is not thread-safe
        protected final int growBy;

        protected int size;

        protected String[] names;

        protected int[] values;

        /**
         * Constructs a new instance of <code>ArrayEntityMap</code>.
         */
        public ArrayEntityMap() {
            this.growBy = 100;
            names = new String[growBy];
            values = new int[growBy];
        }

        /**
         * Constructs a new instance of <code>ArrayEntityMap</code> specifying the size by which the array should
         * grow.
         *
         * @param growBy
         *            array will be initialized to and will grow by this amount
         */
        public ArrayEntityMap(int growBy) {
            this.growBy = growBy;
            names = new String[growBy];
            values = new int[growBy];
        }

        /**
         * {@inheritDoc}
         */
        public void add(String name, int value) {
            ensureCapacity(size + 1);
            names[size] = name;
            values[size] = value;
            size++;
        }

        /**
         * Verifies the capacity of the entity array, adjusting the size if necessary.
         *
         * @param capacity
         *            size the array should be
         */
        protected void ensureCapacity(int capacity) {
            if (capacity > names.length) {
                int newSize = Math.max(capacity, size + growBy);
                String[] newNames = new String[newSize];
                System.arraycopy(names, 0, newNames, 0, size);
                names = newNames;
                int[] newValues = new int[newSize];
                System.arraycopy(values, 0, newValues, 0, size);
                values = newValues;
            }
        }

        /**
         * {@inheritDoc}
         */
        public String name(int value) {
            for (int i = 0; i < size; ++i) {
                if (values[i] == value) {
                    return names[i];
                }
            }
            return null;
        }

        /**
         * {@inheritDoc}
         */
        public int value(String name) {
            for (int i = 0; i < size; ++i) {
                if (names[i].equals(name)) {
                    return values[i];
                }
            }
            return -1;
        }
    }

    static class BinaryEntityMap extends ArrayEntityMap {

        // TODO - not thread-safe, because parent is not. Also references size.

        /**
         * Constructs a new instance of <code>BinaryEntityMap</code>.
         */
        public BinaryEntityMap() {
            super();
        }

        /**
         * Constructs a new instance of <code>ArrayEntityMap</code> specifying the size by which the underlying array
         * should grow.
         *
         * @param growBy
         *            array will be initialized to and will grow by this amount
         */
        public BinaryEntityMap(int growBy) {
            super(growBy);
        }

        /**
         * Performs a binary search of the entity array for the specified key. This method is based on code in
         * {@link java.util.Arrays}.
         *
         * @param key
         *            the key to be found
         * @return the index of the entity array matching the specified key
         */
        private int binarySearch(int key) {
            int low = 0;
            int high = size - 1;

            while (low <= high) {
                int mid = (low + high) >>> 1;
                int midVal = values[mid];

                if (midVal < key) {
                    low = mid + 1;
                } else if (midVal > key) {
                    high = mid - 1;
                } else {
                    return mid; // key found
                }
            }
            return -(low + 1); // key not found.
        }

        /**
         * {@inheritDoc}
         */
        public void add(String name, int value) {
            ensureCapacity(size + 1);
            int insertAt = binarySearch(value);
            if (insertAt > 0) {
                return; // note: this means you can't insert the same value twice
            }
            insertAt = -(insertAt + 1); // binarySearch returns it negative and off-by-one
            System.arraycopy(values, insertAt, values, insertAt + 1, size - insertAt);
            values[insertAt] = value;
            System.arraycopy(names, insertAt, names, insertAt + 1, size - insertAt);
            names[insertAt] = name;
            size++;
        }

        /**
         * {@inheritDoc}
         */
        public String name(int value) {
            int index = binarySearch(value);
            if (index < 0) {
                return null;
            }
            return names[index];
        }
    }

    private final EntityMap map;

    /**
     * Default constructor.
     */
    public XmlEntities(){
        map = new XmlEntities.LookupEntityMap();
    }

    /**
     * package scoped constructor for testing.
     *
     * @param emap entity map.
     */
    XmlEntities(EntityMap emap){
        map = emap;
    }

    /**
     * <p>
     * Adds entities to this entity.
     * </p>
     *
     * @param entityArray
     *            array of entities to be added
     */
    public void addEntities(String[][] entityArray) {
        for (String[] anEntityArray : entityArray) {
            addEntity(anEntityArray[0], Integer.parseInt(anEntityArray[1]));
        }
    }

    /**
     * <p>
     * Add an entity to this entity.
     * </p>
     *
     * @param name
     *            name of the entity
     * @param value
     *            vale of the entity
     */
    public void addEntity(String name, int value) {
        map.add(name, value);
    }

    /**
     * <p>
     * Returns the name of the entity identified by the specified value.
     * </p>
     *
     * @param value
     *            the value to locate
     * @return entity name associated with the specified value
     */
    public String entityName(int value) {
        return map.name(value);
    }

    /**
     * <p>
     * Returns the value of the entity identified by the specified name.
     * </p>
     *
     * @param name
     *            the name to locate
     * @return entity value associated with the specified name
     */
    public int entityValue(String name) {
        return map.value(name);
    }

    /**
     * <p>
     * Escapes the characters in a <code>String</code>.
     * </p>
     *
     * <p>
     * For example, if you have called addEntity(&quot;foo&quot;, 0xA1), escape(&quot;\u00A1&quot;) will return
     * &quot;&amp;foo;&quot;
     * </p>
     *
     * @param str
     *            The <code>String</code> to escape.
     * @return A new escaped <code>String</code>.
     */
    public String escape(String str) {
        StringBuilder sb = createStringBuilder(str);
        escape(sb, str);
        return sb.toString();
    }

    /**
     * <p>
     * Escapes the characters in the <code>String</code> passed and writes the result to the <code>StringBuilder</code>
     * passed.
     * </p>
     *
     * @param sb
     *            The <code>StringBuilder</code> to write the results of the escaping to. Assumed to be a non-null value.
     * @param str
     *            The <code>String</code> to escape. Assumed to be a non-null value.
     *
     * @see #escape(String)
     * @see java.lang.StringBuilder
     */
    public void escape(StringBuilder sb, String str) {
        int len = str.length();
        for (int i = 0; i < len; i++) {
            char c = str.charAt(i);
            String entityName = this.entityName(c);
            if (entityName == null) {
                if (c > 0x7F) {
                    sb.append("&#");
                    sb.append(Integer.toString(c, 10));
                    sb.append(';');
                } else {
                    sb.append(c);
                }
            } else {
                sb.append('&');
                sb.append(entityName);
                sb.append(';');
            }
        }
    }

    @Deprecated
    public void escape(Writer writer, String str) throws IOException {
        writer.write(unescape(str));
    }

    /**
     * <p>
     * Unescapes the entities in a <code>String</code>.
     * </p>
     *
     * <p>
     * For example, if you have called addEntity(&quot;foo&quot;, 0xA1), unescape(&quot;&amp;foo;&quot;) will return
     * &quot;\u00A1&quot;
     * </p>
     *
     * @param str
     *            The <code>String</code> to escape.
     * @return A new escaped <code>String</code>.
     */
    public String unescape(String str) {
        int firstAmp = str.indexOf('&');
        if (firstAmp < 0) {
            return str;
        } else {
            StringBuilder sb = createStringBuilder(str);
            doUnescape(sb, str, firstAmp);
            return sb.toString();
        }
    }

    /**
     * Make the StringBuilder 10% larger than the source String to avoid growing the builder
     *
     * @param str The source string
     * @return A newly created StringBuilder
     */
    private StringBuilder createStringBuilder(String str) {
        return new StringBuilder(str.length() + (str.length()/10));
    }

    /**
     * <p>
     * Unescapes the escaped entities in the <code>String</code> passed and writes the result to the
     * <code>StringBuilder</code> passed.
     * </p>
     *
     * @param sb
     *            The <code>StringBuilder</code> to write the results to; assumed to be non-null.
     * @param str
     *            The source <code>String</code> to unescape; assumed to be non-null.
     *
     * @see #escape(String)
     * @see java.lang.StringBuilder
     */
    public void unescape(StringBuilder sb, String str) {
        int firstAmp = str.indexOf('&');
        if (firstAmp < 0) {
            sb.append(str);
        } else {
            doUnescape(sb, str, firstAmp);
        }
    }

    @Deprecated
    public void unescape(Writer writer, String str) throws IOException {
        writer.write(unescape(str));
    }

    /**
     * Underlying unescape method that allows the optimisation of not starting from the 0 index again.
     *
     * @param sb
     *            The <code>StringBuilder</code> to write the results to; assumed to be non-null.
     * @param str
     *            The source <code>String</code> to unescape; assumed to be non-null.
     * @param firstAmp
     *            The <code>int</code> index of the first ampersand in the source String.
     */
    private void doUnescape(StringBuilder sb, String str, int firstAmp) {
        sb.append(str, 0, firstAmp);
        int len = str.length();
        for (int i = firstAmp; i < len; i++) {
            char c = str.charAt(i);
            if (c == '&') {
                int nextIdx = i + 1;
                int semiColonIdx = str.indexOf(';', nextIdx);
                if (semiColonIdx == -1) {
                    sb.append(c);
                    continue;
                }
                int amphersandIdx = str.indexOf('&', i + 1);
                if (amphersandIdx != -1 && amphersandIdx < semiColonIdx) {
                    // Then the text looks like &...&...;
                    sb.append(c);
                    continue;
                }
                String entityContent = str.substring(nextIdx, semiColonIdx);
                int entityValue = -1;
                int entityContentLen = entityContent.length();
                if (entityContentLen > 0) {
                    if (entityContent.charAt(0) == '#') { // escaped value content is an integer (decimal or
                        // hexidecimal)
                        if (entityContentLen > 1) {
                            char isHexChar = entityContent.charAt(1);
                            try {
                                switch (isHexChar) {
                                    case 'X' :
                                    case 'x' : {
                                        entityValue = Integer.parseInt(entityContent.substring(2), 16);
                                        break;
                                    }
                                    default : {
                                        entityValue = Integer.parseInt(entityContent.substring(1), 10);
                                    }
                                }
                                if (entityValue > 0xFFFF) {
                                    entityValue = -1;
                                }
                            } catch (NumberFormatException e) {
                                entityValue = -1;
                            }
                        }
                    } else { // escaped value content is an entity name
                        entityValue = this.entityValue(entityContent);
                    }
                }

                if (entityValue == -1) {
                    sb.append('&');
                    sb.append(entityContent);
                    sb.append(';');
                } else {
                    sb.append(Character.toChars(entityValue));
                }
                i = semiColonIdx; // move index up to the semi-colon
            } else {
                sb.append(c);
            }
        }
    }

}
