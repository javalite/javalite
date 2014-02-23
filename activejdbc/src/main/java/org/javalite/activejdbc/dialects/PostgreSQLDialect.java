package org.javalite.activejdbc.dialects;

import org.javalite.common.Util;

import java.util.List;
import org.javalite.activejdbc.MetaModel;


public class PostgreSQLDialect extends DefaultDialect {

    @Override
    public String selectStar(String table, String query){        
        return query != null? "SELECT * FROM " + getQuotedIdentifier(table) + " WHERE " + query : "SELECT * FROM " + getQuotedIdentifier(table);
    }
    @Override
    public String selectStarParametrized(String table, String ... parameters) {
        String sql = "SELECT * FROM " + getQuotedIdentifier(table) + " WHERE ";

        for(String parameter:parameters){
            sql += getQuotedIdentifier(parameter) + " = ? AND ";
        }
        return sql.substring(0, sql.length() - 5);//remove last comma
    }
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
    public String DB_count(String table){
        return "SELECT COUNT(*) FROM " + getQuotedIdentifier(table);
    }
    @Override
    public String DB_count(String table, String where){
        return "SELECT COUNT(*) FROM " + getQuotedIdentifier(table) + " WHERE " + where;
    }
    @Override
  public String LazyList_processOther(String target, String sourceFkName, String join, String targetPk, String targetFkName, List ids){
        return "SELECT " + getQuotedIdentifier(target) + ".*, t." + getQuotedIdentifier(sourceFkName) + " AS the_parent_record_id FROM " + getQuotedIdentifier(target) +
        " INNER JOIN " + getQuotedIdentifier(join) + " t ON " + getQuotedIdentifier(target) + "." + getQuotedIdentifier(targetPk) + " = t." + getQuotedIdentifier(targetFkName) + " WHERE (t." + getQuotedIdentifier(sourceFkName)
                + "  IN (" + Util.join(ids, ", ") + "))";
    }
    
    @Override
    public String ModelDelegate_update(String tableName, String updates, String conditions){
       return "UPDATE " + getQuotedIdentifier(tableName) + " SET " + updates + ((conditions != null) ? " WHERE " + conditions : "");
   }
    @Override
    public String Model_delete(String tableName, String idName){
       return "DELETE FROM " + getQuotedIdentifier(tableName) + " WHERE " + getQuotedIdentifier(idName) + "= ?";
   }
    @Override
    public String Model_deleteJoinsForManyToMany(String join, String sourceFK, String id){
       return "DELETE FROM " + getQuotedIdentifier(join) + " WHERE " + getQuotedIdentifier(sourceFK) + " = " + id;
   }
    @Override
   public String Model_deleteOne2ManyChildrenShallow(String target, String fkName){
       return "DELETE FROM " + getQuotedIdentifier(target) + " WHERE " + getQuotedIdentifier(fkName) + " = ?";
   }
    @Override
  public String Model_deletePolymorphicChildrenShallow(String target){
       return "DELETE FROM " + getQuotedIdentifier(target) + " WHERE parent_id = ? AND parent_type = ?";
   }
    @Override
    public String Model_staticDelete(String tableName, String query){
       return "DELETE FROM " + getQuotedIdentifier(tableName) + " WHERE " + query;
   }
    @Override
    public String Model_exists(String idName, String tableName){
       return "SELECT " + getQuotedIdentifier(idName) + " FROM " + getQuotedIdentifier(tableName)
                + " WHERE " + getQuotedIdentifier(idName) + " = ?";
   } 
    @Override
    public String Model_deleteAll(String tableName){
       return "DELETE FROM " + getQuotedIdentifier(tableName);
   }
    @Override
    public String Model_get(String targetTable, String joinTable, String targetId, String targetFkName, String sourceFkName, String id, String additionalCriteria){
       return "SELECT " + getQuotedIdentifier(targetTable) + ".* FROM " + getQuotedIdentifier(targetTable) + ", " + getQuotedIdentifier(joinTable) +
                " WHERE " + getQuotedIdentifier(targetTable) + "." + getQuotedIdentifier(targetId) + " = " + getQuotedIdentifier(joinTable) + "." + getQuotedIdentifier(targetFkName) +
                " AND " + getQuotedIdentifier(joinTable) + "." + getQuotedIdentifier(sourceFkName) + " = " + id + additionalCriteria;
   }
    @Override
    public String Model_add(String join, String sourceFkName, String targetFkName, String id, String childId){
       return "INSERT INTO " + getQuotedIdentifier(join) + " ( " + getQuotedIdentifier(sourceFkName) + ", " + getQuotedIdentifier(targetFkName) + " ) VALUES ( " + id+ ", " + childId + ")";
   }
    @Override
    public String Model_remove(String join, String sourceFkName, String targetFkName){
       return "DELETE FROM " + getQuotedIdentifier(join) + " WHERE " + getQuotedIdentifier(sourceFkName) + " = ? AND "
                        + getQuotedIdentifier(targetFkName) + " = ?";
   }
    @Override
    public String Model_count(String tableName){
       return "SELECT COUNT(*) FROM " + getQuotedIdentifier(tableName);
   }
    @Override
    public String Model_count(String tableName, String query){
       return "SELECT COUNT(*) FROM " + getQuotedIdentifier(tableName) + " WHERE " + query;
   }
    @Override
    public String Model_updatePartial(String tableName, List<String> names){
       String query = "UPDATE " + getQuotedIdentifier(tableName) + " SET ";
        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i);
            query += getQuotedIdentifier(name) + "= ?";
            if (i < names.size() - 1) {
                query += ", ";
            }
        }
        return query;
   }
    @Override
   public String Model_toInsert(String tableName, List<String> names, List<Object> values){
       return new StringBuffer("INSERT INTO ")
                .append(getQuotedIdentifier(tableName)).append(" (")
                .append(getQuotedIdentifier(Util.join(names, getQuotedIdentifier(", "))))
                .append(") VALUES (").append(Util.join(values, ", ")).append(")").toString();
   } 
    @Override
    public String Paginator_Paginator(boolean fullQuery, String query, String tableName){
       return fullQuery ? "SELECT COUNT(*) " + query.substring(query.toLowerCase().indexOf("from"))
                               : "SELECT COUNT(*) FROM " + getQuotedIdentifier(tableName) + " WHERE " + query;
   }
    
    
    
    
    

    private String getQuotedIdentifier(String identifier) {
        return "\""+identifier+"\"";
    }

    @Override
    public String getDefaultConvertedCase(String string) {
        return string;
    }
    
}
