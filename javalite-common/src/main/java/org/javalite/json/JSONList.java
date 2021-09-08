/*
Copyright 2009-present Igor Polevoy

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


package org.javalite.json;

import org.javalite.common.Convert;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Represents a <code>java.util.List</code> as an object  that fronts a chunk of a JSON object.
 * A list can contain  all kinds of things normally found in JSON: other lists, maps, primitives, etc.
 *
 * <p></p>
 *
 * This class adds {@link #getList(int)}  and {@link #getMap(int)} for convenience when working with JSON.

 * @see JSONList#getList(int)
 * @see JSONList#getMap(int)
 */
public class JSONList extends ArrayList {
    public JSONList(List jsonList){
        super(jsonList);
    }
    public JSONList(String jsonList){
        super(JSONHelper.toList(jsonList));
    }

    public JSONList(){}

    /**
     * Returns a JSONMap at a provided index. If the object is not a map, it  will throw an exception.
     *
     * @param index index at which to look for a map.
     *
     * @throws JSONParseException;
     * @return instance of <code>JSONMap</code> at a specified index.
     */
    public JSONMap getMap(int index){
        Object map = super.get(index);
        if(map == null){
            return null;
        }else if(map instanceof Map){
            if(map instanceof  JSONMap){
                return (JSONMap) map;
            }else{
                return new JSONMap((Map) map);
            }
        }else {
            throw new JSONParseException("Object at index " + index + " is not a Map.");
        }
    }

    /**
     * Returns a <code>JSONList</code> at a provided index. If the object is not a list, it  will throw an exception.
     *
     * @param index index at which to look for a list.
     *
     * @throws JSONParseException;
     * @return instance of <code>JSONList</code> at a specified index.
     */
    public JSONList getList(int index){
        Object list = super.get(index);
        if(list == null){
            return null;
        }else if(list instanceof List){
            if(list instanceof JSONList){
                return (JSONList) list;
            }else{
                return new JSONList((List) list);
            }
        }else{
            throw new JSONParseException("Object at index " + index + " is not a List.");
        }
    }


    public boolean getBoolean(int index){
        return Convert.toBoolean(get(index));
    }

    public BigDecimal getBigDecimal(int index){
        return Convert.toBigDecimal(get(index));
    }

    public Date getDate(int index){
        return Convert.toSqlDate(get(index));
    }

    public Double getDouble(int index){
        return Convert.toDouble(get(index));
    }

    public Float getFloat(int index){
        return Convert.toFloat(get(index));
    }

    public Integer getInteger(int index){
        return Convert.toInteger(get(index));
    }

    public Long getLong(int index){
        return Convert.toLong(get(index));
    }

    public Short getShort(int index){
        return Convert.toShort(get(index));
    }

    public String getString(int index){
        return Convert.toString(get(index));
    }

    /**
     * @return a JSON representation  of this object
     */
    @Override
    public String toString() {
        return JSONHelper.toJsonString(this);
    }

    /**
     * @param pretty - true for formatted JSON.
     *
     * @return a JSON representation  of this object
     */
    public String toJSON(boolean pretty){
        return JSONHelper.toJsonString(this,  pretty);
    }
}
