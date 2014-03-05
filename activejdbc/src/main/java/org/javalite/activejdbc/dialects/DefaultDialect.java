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
        query += mm.isVersioned()? ", " + "record_version" :"";
        query += ") VALUES ("+ getQuestions(nonNullAttributes.size());
        query += mm.getIdGeneratorCode() != null ? ", " + mm.getIdGeneratorCode() :"";
        query += mm.isVersioned()? ", " + 1 :"";
        query +=")";

        return query; 
    }

    public String createParametrizedInsertIdUnmanaged(MetaModel mm, List<String> nonNullAttributes){
        String query = "INSERT INTO " + mm.getTableName() + " (" + Util.join(nonNullAttributes, ", ");
        query += mm.isVersioned()? ", " + "record_version" :"";
        query += ") VALUES ("+ getQuestions(nonNullAttributes.size());
        query += mm.isVersioned()? ", " + 1 :"";
        query +=")";

        return query;
    }


    protected String getQuestions(int count){
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
   
   
   
   
   
   
   
   public String DB_count(String table){
        return "SELECT COUNT(*) FROM " + table;
    }
   public String DB_count(String table, String where){
        return "SELECT COUNT(*) FROM " + table + " WHERE " + where;
    }
   public String LazyList_processOther(String target, String sourceFkName, String join, String targetPk, String targetFkName, List ids){
        return "SELECT " + target + ".*, t." + sourceFkName + " AS the_parent_record_id FROM " + target +
        " INNER JOIN " + join + " t ON " + target + "." + targetPk + " = t." + targetFkName + " WHERE (t." + sourceFkName
                + "  IN (" + Util.join(ids, ", ") + "))";
    }
   public String ModelDelegate_update(String tableName, String updates, String conditions){
       return "UPDATE " + tableName + " SET " + updates + ((conditions != null) ? " WHERE " + conditions : "");
   }
   public String Model_delete(String tableName, String idName){
       return "DELETE FROM " + tableName + " WHERE " + idName + "= ?";
   }
   public String Model_deleteJoinsForManyToMany(String join, String sourceFK, String id){
       return "DELETE FROM " + join + " WHERE " + sourceFK + " = " + id;
   }
   public String Model_deleteOne2ManyChildrenShallow(String target, String fkName){
       return "DELETE FROM " + target + " WHERE " + fkName + " = ?";
   }
   public String Model_deletePolymorphicChildrenShallow(String target){
       return "DELETE FROM " + target + " WHERE parent_id = ? AND parent_type = ?";
   }
   public String Model_staticDelete(String tableName, String query){
       return "DELETE FROM " + tableName + " WHERE " + query;
   }
   public String Model_exists(String idName, String tableName){
       return "SELECT " + idName + " FROM " + tableName
                + " WHERE " + idName + " = ?";
   }
   public String Model_deleteAll(String tableName){
       return "DELETE FROM " + tableName;
   }
   public String Model_get(String targetTable, String joinTable, String targetId, String targetFkName, String sourceFkName, String id, String additionalCriteria){
       return "SELECT " + targetTable + ".* FROM " + targetTable + ", " + joinTable +
                " WHERE " + targetTable + "." + targetId + " = " + joinTable + "." + targetFkName +
                " AND " + joinTable + "." + sourceFkName + " = " + id + additionalCriteria;
   }
   public String Model_add(String join, String sourceFkName, String targetFkName, String id, String childId){
       return "INSERT INTO " + join + " ( " + sourceFkName + ", " + targetFkName + " ) VALUES ( " + id + ", " + childId + ")";
   }
   public String Model_remove(String join, String sourceFkName, String targetFkName){
       return "DELETE FROM " + join + " WHERE " + sourceFkName + " = ? AND "
                        + targetFkName + " = ?";
   }
   public String Model_count(String tableName){
       return "SELECT COUNT(*) FROM " + tableName;
   }
   public String Model_count(String tableName, String query){
       return "SELECT COUNT(*) FROM " + tableName + " WHERE " + query;
   }
   public String Model_updatePartial(String tableName, List<String> names){
       String query = "UPDATE " + tableName + " SET ";
        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i);
            query += name + "= ?";
            if (i < names.size() - 1) {
                query += ", ";
            }
        }
        return query;
   }
   public String Model_toInsert(String tableName, List<String> names, List<Object> values){
       return new StringBuffer("INSERT INTO ")
                .append(tableName).append(" (")
                .append(Util.join(names, ", "))
                .append(") VALUES (").append(Util.join(values, ", ")).append(")").toString();
   }
   public String Paginator_Paginator(boolean fullQuery, String query, String tableName){
       return fullQuery ? "SELECT COUNT(*) " + query.substring(query.toLowerCase().indexOf("from"))
                               : "SELECT COUNT(*) FROM " + tableName + " WHERE " + query;
   }
   
   
   
   
   
   
   
   
   public String getDefaultConvertedCase(String string){
       return string.toLowerCase();
   }
}
