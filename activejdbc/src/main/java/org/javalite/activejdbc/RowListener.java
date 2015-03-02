/*
Copyright 2009-2015 Igor Polevoy

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

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;

public abstract class RowListener implements ResultSetListener {
    @Override
    public final void onResultSet(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        while (rs.next()) {
            Map<String, Object> row = new CaseInsensitiveMap<Object>();
            int i = 1;
            while (i <= metaData.getColumnCount()) {
                row.put(metaData.getColumnLabel(i), rs.getObject(i));
                i++;
            }
            if (!next(row)) { break; }
        }
    }

    /**
     * Implementations of this interface can return "false" from the next() method in order to stop fetching more results from DB.
     * Immediately after returning "false", ActiveJDBC will close JDBC resources associated with this request:
     * Statement and ResultSet.
     *
     * @param row Map instance containing values for a row. Keys are names of columns and values are .. values.
     * @return false if this listener needs to stop processing (no more calls to this method)
     */
    public abstract boolean next(Map<String, Object> row);
}
