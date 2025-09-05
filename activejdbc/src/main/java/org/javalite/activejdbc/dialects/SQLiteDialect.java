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
import org.javalite.common.Convert;
import org.javalite.common.Util;

/**
 * @author Igor Polevoy
 * @author Eric Nielsen
 */
public class SQLiteDialect extends DefaultDialect {
    
    
    @Override
    public String formSelect(String tableName, String[] columns, String subQuery, List<String> orderBys, long limit, long offset, boolean lockForUpdate) {
        if(lockForUpdate){
            throw new UnsupportedOperationException("Lock for update not implemented for SQLiteDialect");
        }
        if (limit == -1L && offset != -1L) {
            throw new IllegalArgumentException("SQLite does not support OFFSET without LIMIT. OFFSET is a parameter of LIMIT function");
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
    public Object overrideDriverTypeConversion(MetaModel mm, String attributeName, Object value) {
        // SQLite returns DATE and DATETIME as String or Number values
        if (value instanceof String && !Util.blank(value) || value instanceof Number) {
            String typeName = mm.getColumnMetadata().get(attributeName).getTypeName();
            if ("DATE".equalsIgnoreCase(typeName)) {
                return Convert.toSqlDate(value);
            } else if ("DATETIME".equalsIgnoreCase(typeName)) {
                return Convert.toTimestamp(value);
            } else if ("TIME".equalsIgnoreCase(typeName)) {
                return Convert.toTime(value);
            }
        }
        return value;
    }

    @Override
    protected void appendDate(StringBuilder query, java.sql.Date value) {
        // See https://www.sqlite.org/lang_datefunc.html
        query.append("date('").append(value.toString()).append("')");
    }

    @Override
    protected void appendTime(StringBuilder query, java.sql.Time value) {
        // See https://www.sqlite.org/lang_datefunc.html
        query.append("time('").append(value.toString()).append("')");
    }

    @Override
    protected void appendTimestamp(StringBuilder query, java.sql.Timestamp value) {
        // See https://www.sqlite.org/lang_datefunc.html
        query.append("datetime('").append(value.toString()).append("')");
    }
}
