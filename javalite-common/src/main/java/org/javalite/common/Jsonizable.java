package org.javalite.common;

import java.util.Map;

/**
 * Interface for classes that are to be serialized to JSON format
 * using {@link JsonHelper}.
 *
 * @author igor on 11/12/16.
 */
public interface Jsonizable {


    /**
     * Converts an object to its JSON object.
     * One of the attributes of the object is <code>_class</code>,
     * which is a fully qualified name of a source class. This
     * attribute is used in the future to de-serialize back into
     * the object form.
     *
     * @return JSON representation of the argument.
     */
    String toJSON();

    void hydrate(Map<String, Object> attributesMap);
}
