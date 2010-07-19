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
public class MySQLDialect extends DefaultDialect{
    @Override
    public String formSelect(String tableName, String subQuery, List<String> orderBys, long limit, long offset) {


        String fullQuery = "SELECT  * FROM " + tableName;

        if(!Util.blank(subQuery)){
            String where = " WHERE ";
            //this is only to support findFirst("order by..."), might need to revisit later
            if(!subQuery.toLowerCase().trim().startsWith("order") && !subQuery.toLowerCase().trim().startsWith("group")){   
                fullQuery += where;
            }
            fullQuery += " " + subQuery;
        }

        if(orderBys.size() != 0){
            fullQuery += " ORDER BY " + Util.join(orderBys, ", ");
        }

        if(limit != -1){
            fullQuery +=  " LIMIT " + limit;
        }

        if(offset != -1){
            fullQuery += " OFFSET " + offset;
        }

        return fullQuery;
    }
}
