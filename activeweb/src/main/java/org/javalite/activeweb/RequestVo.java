package org.javalite.activeweb;

import java.util.HashMap;
import java.util.Map;

/**
 * Carries internal data for the duration of a request.
 *
 * @author Igor Polevoy: 1/5/12 6:27 PM
 */
class RequestVo {

    private Map<String, Object> values = new HashMap<>();

    /**
     * User segments are values extracted from the URL if user segments were used on a URL,
     * They are available as regular parameters using param("name") inside controllers and filter.
     */
    private Map<String, String> userSegments = new HashMap<>();
    
    private String wildCardName, wildCardValue;

    protected Object get(String name){
        return values.get(name);
    }

    protected Map<String, String> getUserSegments(){
        return userSegments;
    }

    protected void set(String name, Object value){
        values.put(name, value);
    }

    public String getWildCardName() {
        return wildCardName;
    }

    public String getWildCardValue() {
        return wildCardValue;
    }

    public void setWildCardName(String wildCardName) {
        this.wildCardName = wildCardName;
    }

    public void setWildCardValue(String wildCardValue) {
        this.wildCardValue = wildCardValue;
    }
}
