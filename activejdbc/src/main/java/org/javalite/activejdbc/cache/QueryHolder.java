/*
Copyright 2009-2010 Igor Polevoy 

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

package org.javalite.activejdbc.cache;

import java.util.Arrays;

import org.javalite.common.Util;

/**
 * 
 * @author amarkhel
 *
 * This class is simple wrapper for sql query. Contains string representation of query and array of parameters for this query.
 */
public class QueryHolder {

	private String query;
	private Object[] params;
	public QueryHolder(String query, Object[] params) {
		if(query != null){
			this.query = Util.toGracefulSQL(query);
		}
		if(params != null) {
			this.params = params;
		} else {
			this.params = new Object[0];
		}
		
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((query == null) ? 0 : query.hashCode());
		result = prime * result + hashCodeForArray(params);
		return result;
	}
	
	private int hashCodeForArray(Object[] a) {
		if (a == null)
            return 0;

        int result = 1;

        for (Object element : a) {
        	if(element == null){
        		result = 31 * result;
        		continue;
        	}
			if(element instanceof Number){
				result = 31 * result + ((Number)element).intValue() + (int)((Number)element).doubleValue();
				continue;
			}
			result = 31 * result + element.hashCode();
		}

        return result;
	}
	public String getQuery() {
		return query;
	}
	public Object[] getParams() {
		return params;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		QueryHolder other = (QueryHolder) obj;
		if (query == null) {
			if (other.query != null)
				return false;
		} else if (!query.equals(other.query))
			return false;
		if (!equalsArrays(params, other.params))
			return false;
		
		return true;
	}
	private boolean equalsArrays(Object[] a, Object[] a2) {
		if (a==a2)
            return true;
        if (a==null || a2==null)
            return false;

        int length = a.length;
        if (a2.length != length)
            return false;

        for (int i=0; i<length; i++) {
            Object o1 = a[i];
            Object o2 = a2[i];
            if (o1==null || o2==null ){
            	return false;
            }
            if(o1.equals(o2)){
            	return true;
            }
            if(o1 instanceof Number){
            	if(!(o2 instanceof Number)){
            		return false;
            	}else {
            		return ((Number)o1).doubleValue() == ((Number)o2).doubleValue() && ((Number)o1).intValue() == ((Number)o2).intValue();
            	}
            }
        }
        return true;
	}
}
