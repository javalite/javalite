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
import org.javalite.common.Util;

import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Igor Polevoy
 */
public class DefaultDialect {
    
    protected final Pattern orderByPattern = Pattern.compile("^order *by", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    protected final Pattern groupByPattern = Pattern.compile("^group *by", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    public String selectStar(String table, String query){        
        return query != null? "SELECT * FROM " + table + " WHERE " + query : "SELECT * FROM " + table;
    }

    /**
     * Produces a parametrized  AND query.
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
    public String selectStarParametrized(String table, String ... parameters) {
        String sql = "SELECT * FROM " + table + " WHERE ";

        for(String parameter:parameters){
            sql += parameter + " = ? AND ";
        }
        return sql.substring(0, sql.length() - 5);//remove last comma
    }

    public String createParametrizedInsert(MetaModel mm, List<String> nonNullAttributes){

        String query = "INSERT INTO " + mm.getTableName() + " (" + Util.join(nonNullAttributes, ", ");
        query += mm.getIdGeneratorCode()!= null ? ", " + mm.getIdName() :"";
        query += mm.isVersioned()? ", " + mm.getVersionColumn() :"";
        query += ") VALUES ("+ getQuestions(nonNullAttributes.size());
        query += mm.getIdGeneratorCode() != null ? ", " + mm.getIdGeneratorCode() :"";
        query += mm.isVersioned()? ", " + 1 :"";
        query +=")";

        return query; 
    }

    public String createParametrizedInsertIdUnmanaged(MetaModel mm, List<String> nonNullAttributes){
        String query = "INSERT INTO " + mm.getTableName() + " (" + Util.join(nonNullAttributes, ", ");
        query += mm.isVersioned()? ", " + mm.getVersionColumn() :"";
        query += ") VALUES ("+ getQuestions(nonNullAttributes.size());
        query += mm.isVersioned()? ", " + 1 :"";
        query +=")";

        return query;
    }


    private String getQuestions(int count){
        String [] questions = new String[count];
        for(int i = 0; i < count; i++){
            questions[i] = "?";
        }
        return Util.join(questions, ", ");
    }

   public String formSelect(String tableName, String subQuery, List<String> orderBys, long limit, long offset) {

        String fullQuery = "SELECT * FROM " + tableName;

        if(!Util.blank(subQuery)){
            String where = " WHERE ";
            if(!groupByPattern.matcher(subQuery.toLowerCase().trim()).find() &&
                   !orderByPattern.matcher(subQuery.toLowerCase().trim()).find() ){
                fullQuery += where;
            }
            fullQuery += " " + subQuery;
        }

        if(orderBys.size() != 0){
            fullQuery += " ORDER BY " + Util.join(orderBys, ", ");
        }

        return fullQuery;
    }
   
   public Object overrideDriverTypeConversion(MetaModel mm, String attributeName, Object value) {
	   return value;
   }
}
