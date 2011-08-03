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
public class OracleDialect extends DefaultDialect {

    /**
     * Example of a query we are building here:
     *
     * <code>SELECT * FROM ( SELECT t2.*, ROWNUM as rn FROM ( SELECT t.* FROM PAGES t WHERE <conditions> order by id ) t2) WHERE rn >= 20 AND rownum <= 10;</code>
     *
     * Look here for reference: <a href="http://explainextended.com/2009/05/06/oracle-row_number-vs-rownum/">Oracle: ROW_NUMBER vs ROWNUM</a>
     *
     * @param tableName name of table
     * @param subQuery sub query, something like: "name = ? and ssn = ?". It can be blank: "" or null;
     * @param orderBys collection of order by: "dob desc" - one example
     * @param limit limit value, -1 if not needed.
     * @param offset offset value, -1 if not needed.
     * @return Oracle - specific select query. Here is one example:
     *
     * <code>SELECT * FROM ( SELECT t2.*, ROWNUM as rn FROM ( SELECT t.* FROM PAGES t  WHERE <conditions> order by id ) t2) WHERE rn >= 20 AND rownum <= 10;</code>
     * Can't think of an uglier thing. Shame on you, Oracle.
     */
    public String formSelect(String tableName, String subQuery, List<String> orderBys, long limit, long offset) {

        boolean needLimit = limit != -1;
        boolean needOffset = offset != -1;

        offset += 1;//Oracle offset starts with 1, not like MySQL with 0;

        String fullQuery = needLimit || needOffset ? "SELECT t.* FROM " + tableName + " t " : "SELECT * FROM " + tableName;

        if(!Util.blank(subQuery)){
            String where = " WHERE ";
            //this is only to support findFirst("order by..."), might need to revisit later

            if(!groupByPattern.matcher(subQuery.toLowerCase().trim()).find()  &&
                   !orderByPattern.matcher(subQuery.toLowerCase().trim()).find() ){
                fullQuery += where;
            }
            fullQuery += subQuery;
        }

        if(orderBys.size() != 0){
            fullQuery += " ORDER BY " + Util.join(orderBys, ", ");
        }

        String tmp;
        if(needLimit && needOffset){
            tmp = "SELECT * FROM ( SELECT t2.*, ROWNUM as ORACLE_ROW_NUMBER FROM ( " + fullQuery + " ) t2) WHERE ";
            fullQuery = tmp +   "ORACLE_ROW_NUMBER >= " + offset + " AND ROWNUM <= " + limit;
        }
        else if(needLimit && !needOffset){
            tmp = "SELECT * FROM ( SELECT t2.* FROM ( " + fullQuery + " ) t2) WHERE ";
            fullQuery = tmp + "ROWNUM <= " + limit;            
        }else if(needOffset){
            tmp = "SELECT * FROM ( SELECT t2.*, ROWNUM as ORACLE_ROW_NUMBER FROM ( " + fullQuery + " ) t2) WHERE ";
            fullQuery = tmp +   "ORACLE_ROW_NUMBER >= " + offset;
        }

        return fullQuery;
    }
}
