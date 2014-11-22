/*
Copyright 2009-2014 Igor Polevoy

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

import org.javalite.activejdbc.MetaModel;
import org.javalite.common.Convert;

/**
 * @author Igor Polevoy
 * @author ericbn
 */
public class SQLiteDialect extends MySQLDialect {
    @Override
    public Object overrideDriverTypeConversion(MetaModel mm, String attributeName, Object value) {
        // SQLite returns DATE and DATETIME as String or Long values
        if (value instanceof String || value instanceof Long) {
            String typeName = mm.getColumnMetadata().get(attributeName.toLowerCase()).getTypeName();
            if ("DATE".equalsIgnoreCase(typeName)) {
                return Convert.toSqlDate(value);
            } else if ("DATETIME".equalsIgnoreCase(typeName)) {
                return Convert.toTimestamp(value);
            }
        }
        return value;
    }
}
