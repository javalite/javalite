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


package activejdbc.dialects;

import javalite.common.Util;

import java.util.List;

/**
 * @author Igor Polevoy
 */
public class MySQLDialect extends PostgreSQLDialect{
    @Override
    public String formSelect(String tableName, String subQuery, List<String> orderBys, long limit, long offset) {

        if(limit == -1 && offset != -1){
            throw new IllegalArgumentException("MySQL does not support OFFSET without LIMIT. OFFSET is a parameter of LIMIT function");
        }

        return super.formSelect(tableName, subQuery, orderBys, limit, offset);
    }
}
