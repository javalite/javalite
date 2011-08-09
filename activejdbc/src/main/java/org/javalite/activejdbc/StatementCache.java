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


package org.javalite.activejdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;

/**
 * @author Igor Polevoy
 */
class StatementCache {

    private static StatementCache  instance = new StatementCache();
    private StatementCache (){}

    static StatementCache instance(){return instance;}

    private HashMap<Connection, HashMap<String, PreparedStatement>> statementCache = new HashMap<Connection, HashMap<String, PreparedStatement>>();

    PreparedStatement getPreparedStatement(Connection connection, String query) throws SQLException {

        if (!statementCache.containsKey(connection)) {
            statementCache.put(connection, new HashMap<String, PreparedStatement>());
        }

        HashMap<String, PreparedStatement> preparedStatementMap = statementCache.get(connection);
        return preparedStatementMap.get(query);
    }

    public void cache(Connection connection, String query, PreparedStatement ps) {
        statementCache.get(connection).put(query, ps);
    }

    void cleanStatementCache(Connection connection) {
        statementCache.remove(connection);
    }
}
