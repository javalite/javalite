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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.javalite.common.Util;


/**
 * 
 * @author amarkhel
 *
 * Query caching is a AJ feature that caches the result set returned by each query so that if AJ encounters the same query again for that request, it will use the cached result set as opposed to running the query against the database again.
 * This cache is automatically cleared during DB.close() call.
 * Also cache automatically purged when some modifying action(INSERT UPDATE DELETE) executed.
 */
public class ConnectionCache {

	private Map<QueryHolder, List<HashMap<String, Object>>> cache = new HashMap<QueryHolder, List<HashMap<String, Object>>>();
	
	/**
	 * Return cached result if exist.
	 * Step1 - Firstly, it tried find cached items by non-modified queryString and if queryString is the same but parameters little differ(Integer(1) and Long(1)), tries to compare it by semantics.
	 * Step2 - If we found items in step 1 then return them
	 * Step3 - If items not found, it tried to find cached items for query with trimmed "limit" and "offset" parts. That's legal because all items that can be found by query "select * from items limit 1",
	 * also contains in cached item for query "select * from items".
	 * Step 4 - If something found at Step 3 - then should be adjusted to "limit" and "offset". For example, if query was "select * from items limit 2" and some items were found under key "select * from items", method return all cached items except first 2. Also with offset.
	 * Step 5 - return cached items or null, assuming cache didn't contain items.
	 * @param query - queryHolder(contains queryString and query params) to search
	 * @return cached items or null
	 */
	public List<HashMap<String, Object>> getItem(QueryHolder query){
		List<HashMap<String, Object>> cached = cache.get(query);
		if(cached == null){
			//try find cached item without limits and offsets
			//there are no sense specify offset without limit
			if(query.getQuery().contains("limit")){
				String firstSQL = query.getQuery();
				query = new QueryHolder(Util.trimLimitAndOffset(query.getQuery()), query.getParams());
				cached = cache.get(query);
				if(cached != null){
					int[] limits = Util.getLimitAndOffsetFromString(firstSQL);
					int offset = limits[1];
					int limit = limits[0];
					if(offset != 0){
						if(offset >= cached.size()){
							cached = new ArrayList<HashMap<String, Object>>();
						} else {
							int endIndex = offset + limit;
							if(endIndex > cached.size()){
								endIndex = cached.size();
							}
							cached = cached.subList(offset, endIndex);
						}
					} else {
						cached = cached.subList(0, limit > cached.size() ? cached.size() : limit);
					}
				}
			}
		}
		return cached;
	}
	
	/**
	 * Put items in a cache.
	 * @param query - key to store cached items
	 * @param toCache - cached items
	 */
	public void putItem(QueryHolder query, List<HashMap<String, Object>> toCache){
		cache.put(query, toCache);
	}
	
	/**
	 * Purge cache. Typically called when INSERT UPDATE DELETE statements were executed.
	 */
	public void purgeSqlCache(){
		cache.clear();
	}
}
