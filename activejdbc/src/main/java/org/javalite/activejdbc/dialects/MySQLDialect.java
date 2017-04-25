/*
Copyright 2009-2016 Igor Polevoy

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
public class MySQLDialect extends PostgreSQLDialect {
    @Override
    public String formSelect(String tableName, String[] columns, String subQuery, List<String> orderBys, long limit, long offset) {
        if (limit == -1L && offset != -1L) {
            throw new IllegalArgumentException("MySQL does not support OFFSET without LIMIT. OFFSET is a parameter of LIMIT function");
        }
        return super.formSelect(tableName, columns, subQuery, orderBys, limit, offset);
    }

    @Override
    protected void appendEmptyRow(MetaModel metaModel, StringBuilder query) {
        query.append("() VALUES ()");
    }
}
