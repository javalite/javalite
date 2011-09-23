package org.javalite.activejdbc.dialects;

import org.javalite.common.Util;

import java.util.List;


public class PostgreSQLDialect extends DefaultDialect {

    /**
     * Generates adds limit, offset and order bys to a sub-query
     *
     * @param tableName name of table. If table name is null, then the subQuery parameter is considered to be a full query, and all that needs to be done is to
     * add limit, offset and order bys
     * @param subQuery sub-query or a full query
     * @param orderBys
     * @param limit
     * @param offset
     * @return query with
     */
    @Override
    public String formSelect(String tableName, String subQuery, List<String> orderBys, long limit, long offset) {
      
        String fullQuery;
        if (tableName == null){
            fullQuery = subQuery;
        } else {
            fullQuery = "SELECT  * FROM " + tableName;
            if (!Util.blank(subQuery)) {
                String where = " WHERE ";

                if (!groupByPattern.matcher(subQuery.toLowerCase().trim()).find() &&
                        !orderByPattern.matcher(subQuery.toLowerCase().trim()).find()) {
                    fullQuery += where;
                }
                fullQuery += " " + subQuery;
            }
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
