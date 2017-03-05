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
package org.javalite.activejdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Map;

import static org.javalite.common.Util.*;


public class RowProcessor {
    private final ResultSet rs;
    private final Statement s;

    protected RowProcessor(ResultSet rs, Statement s){
        this.rs = rs;
        this.s = s;
    }

    public void with(RowListener listener){
        try {
            processRS(listener);
        } catch(SQLException e) {
            throw new DBException(e);
        } finally {
            //TODO: shouldn't these be closed in the same scope they were created?
            closeQuietly(rs);
            closeQuietly(s);
        }
    }

    protected void processRS(RowListener listener) throws SQLException {

        ResultSetMetaData metaData = rs.getMetaData();

        String[] labels = new String[metaData.getColumnCount()];
        int i = 0;
        while (i < labels.length) {
            labels[i++] = metaData.getColumnLabel(i);
        }

        while (rs.next()) {
            Map<String, Object> row = new CaseInsensitiveMap<>();
            i = 0;
            while (i < labels.length) {
                row.put(labels[i++], rs.getObject(i));
            }
            if (!listener.next(row)) { break; }
        }
    }
}
