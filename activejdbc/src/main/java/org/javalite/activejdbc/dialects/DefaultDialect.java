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

import java.util.List;
import java.util.regex.Pattern;

import static org.javalite.common.Util.*;

/**
 * @author Igor Polevoy
 */
public class DefaultDialect implements Dialect {
    
    protected final Pattern orderByPattern = Pattern.compile("^\\s*ORDER\\s+BY", 
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    protected final Pattern groupByPattern = Pattern.compile("^\\s*GROUP\\s+BY", 
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    @Override
    public String selectStar(String table) {
        return "SELECT * FROM " + table;
    }
    
    @Override
    public String selectStar(String table, String query) {
        return query != null ? "SELECT * FROM " + table + " WHERE " + query : selectStar(table);
    }

    /**
     * Produces a parametrized AND query.
     * Example:
     * <pre>
     * String sql = dialect.selectStarParametrized("people", "name", "ssn", "dob");
     * //generates:
     * //SELECT * FROM people WHERE name = ? AND ssn = ? AND dob = ?
     * </pre>
     *
     *
     * @param table name of table
     * @param parameters list of parameter names
     * @return something like: "select * from table_name where name = ? and last_name = ? ..."
     */
    @Override
    public String selectStarParametrized(String table, String ... parameters) {
        StringBuilder sql = new StringBuilder().append("SELECT * FROM ").append(table).append(" WHERE ");
        join(sql, parameters, " = ? AND ");
        sql.append(" = ?");
        return sql.toString();
    }

    @Override
    public String createParametrizedInsert(MetaModel mm, List<String> nonNullAttributes){
        StringBuilder query = new StringBuilder().append("INSERT INTO ").append(mm.getTableName()).append(" (");
        join(query, nonNullAttributes, ", ");
        if (mm.getIdGeneratorCode() != null) {
            query.append(", ").append(mm.getIdName());
        }
        if (mm.isVersioned()) {
            query.append(", ").append(mm.getVersionColumn());
        }
        query.append(") VALUES (");
        appendQuestions(query, nonNullAttributes.size());
        if (mm.getIdGeneratorCode() != null) {
            query.append(", ").append(mm.getIdGeneratorCode());
        }
        if (mm.isVersioned()) {
            query.append(", ").append(1);
        }
        query.append(')');
        return query.toString(); 
    }

    @Override
    public String createParametrizedInsertIdUnmanaged(MetaModel mm, List<String> nonNullAttributes){
        StringBuilder query = new StringBuilder().append("INSERT INTO ").append(mm.getTableName()).append(" (");
        join(query, nonNullAttributes, ", ");
        if (mm.isVersioned()) {
            query.append(", ").append(mm.getVersionColumn());
        }
        query.append(") VALUES (");
        appendQuestions(query, nonNullAttributes.size());
        if (mm.isVersioned()) {
            query.append(", ").append(1);
        }
        query.append(')');
        return query.toString();
    }

    protected void appendQuestions(StringBuilder query, int count) {
        joinAndRepeat(query, "?", ", ", count);
    }

    protected void appendOrderBy(StringBuilder query, List<String> orderBys) {
        if (!orderBys.isEmpty()) {
            query.append(" ORDER BY ");
            join(query, orderBys, ", ");
        }
    }

    protected void appendSubQuery(StringBuilder query, String subQuery) {
        if (!blank(subQuery)) {
            // this is only to support findFirst("order by..."), might need to revisit later
            if (!groupByPattern.matcher(subQuery).find() && !orderByPattern.matcher(subQuery).find()) {
                query.append(" WHERE");
            }
            query.append(' ').append(subQuery);
        }
    } 
    
    protected void appendSelect(StringBuilder query, String tableName, String tableAlias, String subQuery, 
            List<String> orderBys) {
        if (tableName == null) {
            query.append(subQuery);
        } else {
            if (tableAlias == null) {
                query.append("SELECT * FROM ").append(tableName);
            } else {
                query.append("SELECT ").append(tableAlias).append(".* FROM ").append(tableName).append(' ')
                        .append(tableAlias);
            }
            appendSubQuery(query, subQuery);
        }
        appendOrderBy(query, orderBys);
    }
    
    @Override
    public String formSelect(String tableName, String subQuery, List<String> orderBys, long limit, long offset) {
        StringBuilder fullQuery = new StringBuilder();
        appendSelect(fullQuery, tableName, null, subQuery, orderBys);
        return fullQuery.toString();
    }
   
    @Override
   public Object overrideDriverTypeConversion(MetaModel mm, String attributeName, Object value) {
	   return value;
   }
}
