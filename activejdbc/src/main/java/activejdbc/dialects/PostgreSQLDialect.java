package activejdbc.dialects;

import javalite.common.Util;

import java.util.List;


public class PostgreSQLDialect extends DefaultDialect {
    @Override
    public String formSelect(String tableName, String subQuery, List<String> orderBys, long limit, long offset) {
      
        String fullQuery = "SELECT  * FROM " + tableName;

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

        if(limit != -1){
            fullQuery +=  " LIMIT " + limit;
        }

        if(offset != -1){
            fullQuery += " OFFSET " + offset;
        }

        return fullQuery;
    }
}
