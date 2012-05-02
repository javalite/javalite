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


package org.javalite.activejdbc;

import java.util.HashMap;
import java.util.List;


public class CachedRowProcessor implements RowProcessor {
	
    private List<HashMap<String, Object>> queryMap;

    public CachedRowProcessor(List<HashMap<String, Object>> queryMap){
        this.queryMap = queryMap;
    }

    public void with(RowListener listener){
    	if(queryMap == null){
    		return;
    	}
    	for(HashMap<String, Object> row : queryMap){
    		//TODO Add hook for onLoad?? 
    		if(!listener.next(row)) break;
    	}
    }
}