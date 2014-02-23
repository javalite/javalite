package org.javalite.activejdbc.dialects;

import org.javalite.common.Util;

import java.util.List;
import org.javalite.activejdbc.MetaModel;


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
            fullQuery = "SELECT  * FROM " + getQuotedIdentifier(tableName);
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
            fullQuery += " ORDER BY " + getQuotedIdentifier(Util.join(orderBys, getQuotedIdentifier(", ")));
        }

        if(limit != -1){
            fullQuery +=  " LIMIT " + limit;
        }

        if(offset != -1){
            fullQuery += " OFFSET " + offset;
        }

        return fullQuery;
    }
    
    
    
    
    
    public String selectStarParametrized(String table, String ... parameters) {
        String sql = "SELECT * FROM " + getQuotedIdentifier(table) + " WHERE ";

        for(String parameter:parameters){
            sql += getQuotedIdentifier(parameter) + " = ? AND ";
        }
        return sql.substring(0, sql.length() - 5);//remove last comma
    }

    public String createParametrizedInsert(MetaModel mm, List<String> nonNullAttributes){

        String query = "INSERT INTO " + getQuotedIdentifier(mm.getTableName()) + " (" + getQuotedIdentifier(Util.join(nonNullAttributes, getQuotedIdentifier(", ")));
        query += mm.getIdGeneratorCode()!= null ? ", " + getQuotedIdentifier(mm.getIdName()) :"";
        query += mm.isVersioned()? ", " + "record_version" :"";
        query += ") VALUES ("+ getQuestions(nonNullAttributes.size());
        query += mm.getIdGeneratorCode() != null ? ", " + mm.getIdGeneratorCode() :"";
        query += mm.isVersioned()? ", " + 1 :"";
        query +=")";

        return query; 
    }

    public String createParametrizedInsertIdUnmanaged(MetaModel mm, List<String> nonNullAttributes){
        String query = "INSERT INTO " + getQuotedIdentifier(mm.getTableName()) + " (" + getQuotedIdentifier(Util.join(nonNullAttributes, getQuotedIdentifier(", ")));
        query += mm.isVersioned()? ", " + "record_version" :"";
        query += ") VALUES ("+ getQuestions(nonNullAttributes.size());
        query += mm.isVersioned()? ", " + 1 :"";
        query +=")";

        return query;
    }

   

    
    
    
    
    

    @Override
    public String getQuotedIdentifier(String identifier) {
        return "\""+identifier+"\"";
    }

    @Override
    public String getDefaultConvertedCase(String string) {
        return string;
    }
    
}
