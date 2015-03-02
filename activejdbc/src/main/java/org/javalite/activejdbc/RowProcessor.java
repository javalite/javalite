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
package org.javalite.activejdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.javalite.common.Util.*;

@Deprecated
public class RowProcessor {
    private final ResultSet rs;
    private final Statement s;

    protected RowProcessor(ResultSet rs, Statement s){
        this.rs = rs;
        this.s = s;
    }

    /**
     * @deprecated use {@link DB#findWith(ResultSetListener, boolean, String, Object...)}
     */
    @Deprecated
    public void with(RowListener listener) {
        try {
            listener.onResultSet(rs);
        } catch(SQLException e) {
            throw new DBException(e);
        } finally {
            closeQuietly(rs);
            closeQuietly(s);
        }
    }
}
