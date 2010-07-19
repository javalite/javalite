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


package activejdbc.dialects;

import javalite.common.Util;

import java.util.List;

/**
 * @author Igor Polevoy
 */
public class DefaultDialect {


    //this is for current version of Oracle
    //SELECT  * FROM ( SELECT  t.*, ROWNUM AS rn FROM mytable t ORDER BY paginator, id)WHERE   rn >= 900001 AND rownum <= 10;

    public String selectStar(String table, String query){        
        return query != null? "SELECT * FROM " + table + " WHERE " + query : "SELECT * FROM " + table;
    }

    /**
     * Produces a parametrized  AND query.
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

    public String createParametrizedInsert(String table, List<String> attributes, String idName, String idGeneratorCode){
        String query = "INSERT INTO " + table + " (" + Util.join(attributes, ", ");
        query += idGeneratorCode != null ? ", " + idName :"";
        query += ") VALUES ("+ getQuestions(attributes.size());
        query += idGeneratorCode != null ? ", " + idGeneratorCode :"";
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
            //this is only to support findFirst("order by..."), nightneed to revisit later
            if(!subQuery.toLowerCase().trim().startsWith("order") && !subQuery.toLowerCase().trim().startsWith("group")){
                fullQuery += where;
            }
            fullQuery += " " + subQuery;
        }


        if(orderBys.size() != 0){
            fullQuery += " ORDER BY " + Util.join(orderBys, ", ");
        }

        return fullQuery;
    } 
}
