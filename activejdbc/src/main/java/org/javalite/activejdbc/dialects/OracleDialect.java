/*
Copyright 2009-2016 Igor Polevoy

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

import java.util.List;
import org.javalite.activejdbc.MetaModel;

/**
 * @author Igor Polevoy
 * @author Eric Nielsen
 */
public class OracleDialect extends DefaultDialect {

    /**
     * Example of a query we are building here:
     *
     * <blockquote><pre>
     * SELECT * FROM (
     *   SELECT t2.*, ROWNUM AS oracle_row_number FROM (
     *     SELECT t.* FROM pages t WHERE &lt;conditions&gt; ORDER BY id
     *   ) t2
     * ) WHERE oracle_row_number &gt;= 20 AND rownum &lt;= 10;
     * </pre></blockquote>
     *
     * <p>Look here for reference: <a href="http://explainextended.com/2009/05/06/oracle-row_number-vs-rownum/">Oracle: ROW_NUMBER vs ROWNUM</a>
     *
     * @param tableName name of table. If table name is null, then the subQuery parameter is considered to be a full query, and all that needs to be done is to
     * add limit, offset and order bys
     * @param columns not used in this implementation.
     * @param subQuery sub query, something like: "name = ? AND ssn = ?". It can be blank: "" or null;
     * @param orderBys collection of order by: "dob desc" - one example
     * @param limit limit value, -1 if not needed.
     * @param offset offset value, -1 if not needed.
     * @return Oracle - specific select query. Here is one example:
     *
     * <pre>SELECT * FROM (SELECT t2.*, ROWNUM AS oracle_row_number FROM (SELECT t.* FROM pages t WHERE &lt;conditions&gt; ORDER BY id) t2) WHERE oracle_row_number &gt;= 20 AND rownum &lt;= 10;</pre>
     * Can't think of an uglier thing. Shame on you, Oracle.
     */
    @Override
    public String formSelect(String tableName, String[] columns, String subQuery, List<String> orderBys, long limit, long offset) {

        boolean needLimit = limit != -1L;
        boolean needOffset = offset != -1L;

        StringBuilder fullQuery = new StringBuilder();
        if (needOffset) {
            fullQuery.append("SELECT * FROM (SELECT t2.*, ROWNUM AS oracle_row_number FROM (");
        } else if (needLimit) { // if needLimit and don't needOffset
            fullQuery.append("SELECT * FROM (SELECT t2.* FROM (");
        }
        //TODO check if this can be simplified removing the alias t
        appendSelect(fullQuery, tableName, null, (needLimit || needOffset) ? "t" : null, subQuery, orderBys);

        if (needOffset) {
            // Oracle offset starts with 1, not like MySQL with 0;
            fullQuery.append(") t2) WHERE oracle_row_number >= ").append(offset + 1);
            if (needLimit) {
                fullQuery.append(" AND ROWNUM <= ").append(limit);
            }
        } else if (needLimit) {
            fullQuery.append(") t2) WHERE ROWNUM <= ").append(limit);            
        }

        return fullQuery.toString();
    }

    @Override
    protected void appendEmptyRow(MetaModel metaModel, StringBuilder query) {
        query.append('(').append(metaModel.getIdName()).append(") VALUES (")
                .append(metaModel.getIdGeneratorCode() != null ? metaModel.getIdGeneratorCode() : "NULL")
                .append(')');
    }

    @Override
    protected void appendTime(StringBuilder query, java.sql.Time value) {
        // Oracle has no TIME type
        appendTimestamp(query, new java.sql.Timestamp(value.getTime()));
    }
}
