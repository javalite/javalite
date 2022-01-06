/**
 * 
 */
package org.javalite.activejdbc.dialects;

import java.util.List;

/**
 * Supports features of the h2 sql dialect.
 * 
 * h2 database sql is very standard, and the developer seems to be adding
 * Postgres and Mysql compatibility. 
 * 
 * @see <a href='http://www.h2database.com/html/grammar.html'>http://www.h2database.com/html/grammar.html</a>
 * @author Phil Suh (http://filsa.net/)
 */
public class H2Dialect extends DefaultDialect {

    /**
     * Generates adds limit, offset and order bys to a sub-query
     *
     * @param tableName name of table. If table name is null, then the subQuery parameter is considered to be a full query, and all that needs to be done is to
     * add limit, offset and order bys
     * @param columns not used in this implementation
     * @param subQuery sub-query or a full query
     * @param orderBys
     * @param limit
     * @param offset
     * @return query with
     */
    @Override
    public String formSelect(String tableName, String[] columns, String subQuery, List<String> orderBys, long limit, long offset) {
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

}
