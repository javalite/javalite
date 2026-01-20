/*
Copyright 2009-2019 Igor Polevoy

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

import java.io.Serializable;
import java.sql.Array;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

import org.javalite.activejdbc.LockMode;
import org.javalite.activejdbc.MetaModel;
import org.javalite.activejdbc.associations.Many2ManyAssociation;

/**
 * @author Eric Nielsen
 */
public interface Dialect extends Serializable{

    /**
     * Forms a SELECT statement with optional locking.
     *
     * @param tableName table name
     * @param columns columns to select
     * @param subQuery sub-query (WHERE clause)
     * @param orderBys order by clauses
     * @param limit limit
     * @param offset offset
     * @param lockMode row-level locking mode
     * @return formed SELECT statement
     */
    String formSelect(String tableName, String[] columns, String subQuery, List<String> orderBys, long limit, long offset, LockMode lockMode);

    /**
     * Forms a SELECT statement with optional locking.
     *
     * @param tableName table name
     * @param columns columns to select
     * @param subQuery sub-query (WHERE clause)
     * @param orderBys order by clauses
     * @param limit limit
     * @param offset offset
     * @param lockForUpdate if true, adds FOR UPDATE clause (or database-specific equivalent)
     * @return formed SELECT statement
     * @deprecated Use {@link #formSelect(String, String[], String, List, long, long, LockMode)} instead.
     */
    @Deprecated
    default String formSelect(String tableName, String[] columns, String subQuery, List<String> orderBys, long limit, long offset, boolean lockForUpdate) {
        return formSelect(tableName, columns, subQuery, orderBys, limit, offset, lockForUpdate ? LockMode.FOR_UPDATE : LockMode.NONE);
    }

    /**
     * Forms a SELECT statement without locking.
     *
     * @param tableName table name
     * @param columns columns to select
     * @param subQuery sub-query (WHERE clause)
     * @param orderBys order by clauses
     * @param limit limit
     * @param offset offset
     * @return formed SELECT statement
     */
    default String formSelect(String tableName, String[] columns, String subQuery, List<String> orderBys, long limit, long offset) {
        return formSelect(tableName, columns, subQuery, orderBys, limit, offset, LockMode.NONE);
    }

    Object overrideDriverTypeConversion(MetaModel mm, String attributeName, Object value);

    String selectStar(String table);

    String selectStar(String table, String where);

    String selectStarParametrized(String table, String... parameters);

    String selectCount(String from);

    String selectCount(String table, String where);

    String selectExists(MetaModel mm);

    String selectManyToManyAssociation(Many2ManyAssociation association, String sourceFkColumnName, int questionsCount);

    String insertManyToManyAssociation(Many2ManyAssociation association);

    String insertParametrized(MetaModel metaModel, List<String> columns, boolean containsId);

    String deleteManyToManyAssociation(Many2ManyAssociation association);

    String insert(MetaModel metaModel, Map<String, Object> attributes, String ... replacements);

    String update(MetaModel metaModel, Map<String, Object> attributes, String ... replacements);
    Array toArray(String typeName, Object value, Connection connection);

    void appendQuestionsForUpdate(MetaModel metaModel, StringBuilder query, List<String> attributeNames);
}
