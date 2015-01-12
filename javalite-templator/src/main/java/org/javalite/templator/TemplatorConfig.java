package org.javalite.templator;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Igor Polevoy on 1/11/15.
 */
public enum TemplatorConfig {

    INSTANCE; //singleton

    public static TemplatorConfig instance() {
        return INSTANCE;
    }

    private final Map<String, AbstractTag> tags = new HashMap<String, AbstractTag>();

    public void registerTag(String name, AbstractTag tag) {
        tags.put(name, tag);
    }

    public AbstractTag getTag(String name) {
        if (!tags.containsKey(name))
            throw new TemplateException("Tag named '" + name + "' was not registered");

        return tags.get(name);
    }
}
