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


package org.javalite.activejdbc.web;


import org.javalite.activejdbc.Base;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This is a filter for opening a connection before and closing connection after servlet. Also create temporary cache for holding query results.
 * Example of configuration:

 * <pre>

     &lt;filter&gt;
        &lt;filter-name&gt;activeJdbcFilter&lt;/filter-name&gt;
        &lt;filter-class&gt;activejdbc.web.ActiveJdbcCachedFilter&lt;/filter-class&gt;
        &lt;init-param&gt;
            &lt;param-name&gt;jndiName&lt;/param-name&gt;
            &lt;param-value&gt;jdbc/test_jndi&lt;/param-value&gt;
        &lt;/init-param&gt;        
    &lt;/filter&gt;
 * </pre>
 * @author Igor Polevoy
 */
public class ActiveJdbcCachedFilter extends ActiveJdbcFilter {

    final Logger logger = LoggerFactory.getLogger(ActiveJdbcCachedFilter.class);

    @Override
    public void openDB() {
		Base.openCached(getJndiName());
	}
}
