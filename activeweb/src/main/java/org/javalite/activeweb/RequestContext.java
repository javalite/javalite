package org.javalite.activeweb;

import java.util.HashMap;
import java.util.Map;

/**
 * Carries internal data for the duration of a request.
 *
 * @author Igor Polevoy: 1/5/12 6:27 PM
 */
public class RequestContext {

    private Map<String, String> values = new HashMap<String, String>();
    private Map<String, String> userSegments = new HashMap<String, String>();


    protected Object get(String name){
        return values.get(name);
    }

    protected Map<String, String> getUserSegments(){
        return userSegments;
    }

    protected void set(String name, String value){
        values.put(name, value);
    }
}
