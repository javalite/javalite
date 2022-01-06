/*
Copyright 2009-2019 Igor Polevoy

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


package org.javalite.activejdbc.dialects;

import java.util.List;
import org.javalite.activejdbc.MetaModel;

/**
 * @author Igor Polevoy
 */
public class MySQLDialect extends DefaultDialect {
    @Override
    public String formSelect(String tableName, String[] columns, String subQuery, List<String> orderBys, long limit, long offset) {
        if (limit == -1L && offset != -1L) {
            throw new IllegalArgumentException("MySQL does not support OFFSET without LIMIT. OFFSET is a parameter of LIMIT function");
        }
        StringBuilder fullQuery = new StringBuilder();

        appendSelect(fullQuery, tableName, columns, null, subQuery, orderBys);

        if(limit != -1){
            fullQuery.append(" LIMIT ").append(limit);
        }

        if(offset != -1){
            fullQuery.append(" OFFSET ").append(offset);
        }

        return fullQuery.toString();
    }

    @Override
    protected void appendEmptyRow(MetaModel metaModel, StringBuilder query) {
        query.append("() VALUES ()");
    }
}
