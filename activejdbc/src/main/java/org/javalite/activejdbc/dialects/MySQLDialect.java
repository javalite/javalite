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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.javalite.activejdbc.CaseInsensitiveMap;
import org.javalite.activejdbc.MetaModel;

import static org.javalite.common.Util.join;

/**
 * @author Igor Polevoy
 */
public class MySQLDialect extends PostgreSQLDialect {
    @Override
    public String formSelect(String tableName, String[] columns, String subQuery, List<String> orderBys, long limit, long offset) {
        if (limit == -1L && offset != -1L) {
            throw new IllegalArgumentException("MySQL does not support OFFSET without LIMIT. OFFSET is a parameter of LIMIT function");
        }
        return super.formSelect(tableName, columns, subQuery, orderBys, limit, offset);
    }

    @Override
    protected void appendEmptyRow(MetaModel metaModel, StringBuilder query) {
        query.append("() VALUES ()");
    }

    @Override
    public String update(MetaModel metaModel, Map<String, Object> attributes) {
        if (attributes.isEmpty()) {
            throw new NoSuchElementException("No attributes set, can't create an update statement.");
        }
        StringBuilder query = new StringBuilder().append("UPDATE ").append(metaModel.getTableName()).append(" SET ");
        String idName = metaModel.getIdName();

        // don't include the id name in the SET portion
        Map<String, Object> attributesWithoutId = new CaseInsensitiveMap<>(attributes);
        attributesWithoutId.remove(idName);

        Iterator<Map.Entry<String, Object>> it = attributesWithoutId.entrySet().iterator();
        for (;;) {
            Map.Entry<String, Object> attribute = it.next();
            query.append("`").append(attribute.getKey()).append("`").append(" = ");
            appendValue(query, attribute.getValue()); // Accommodates the different types
            if (it.hasNext()) {
                query.append(", ");
            } else {
                break;
            }
        }

        if (metaModel.getCompositeKeys() == null){
            query.append(" WHERE ").append(idName).append(" = ").append(attributes.get(idName));
        } else {
            String[] compositeKeys = metaModel.getCompositeKeys();
            for (int i = 0; i < compositeKeys.length; i++) {
                query.append(i == 0 ? " WHERE " : " AND ").append("`").append(compositeKeys[i]).append("`").append(" = ");
                appendValue(query, attributes.get(compositeKeys[i]));
            }
        }
        return query.toString();
    }

    @Override
    public String insert(MetaModel metaModel, Map<String, Object> attributes) {
        StringBuilder query = new StringBuilder().append("INSERT INTO ").append(metaModel.getTableName()).append(' ');
        if (attributes.isEmpty()) {
            appendEmptyRow(metaModel, query);
        } else {
            boolean addIdGeneratorCode = (metaModel.getIdGeneratorCode() != null
                    && attributes.get(metaModel.getIdName()) == null); // do not use containsKey
            query.append('(');
            if (addIdGeneratorCode) {
                query.append(metaModel.getIdName()).append(", ");
            }
            query.append("`");
            join(query, attributes.keySet(), "`, `");
            query.append("`) VALUES (");
            if (addIdGeneratorCode) {
                query.append(metaModel.getIdGeneratorCode()).append(", ");
            }
            Iterator<Object> it = attributes.values().iterator();
            appendValue(query, it.next());
            while (it.hasNext()) {
                query.append(", ");
                appendValue(query, it.next());
            }
            query.append(')');
        }
        return query.toString();
    }

    @Override
    public String insertParametrized(MetaModel metaModel, List<String> columns, boolean containsId) {
        StringBuilder query = new StringBuilder().append("INSERT INTO ").append(metaModel.getTableName()).append(' ');
        if (columns.isEmpty()) {
            appendEmptyRow(metaModel, query);
        } else {
            boolean addIdGeneratorCode = (!containsId && metaModel.getIdGeneratorCode() != null);
            query.append('(');
            if (addIdGeneratorCode) {
                query.append(metaModel.getIdName()).append(", ");
            }
            query.append("`");
            join(query, columns, "`, `");
            query.append("`) VALUES (");
            if (addIdGeneratorCode) {
                query.append(metaModel.getIdGeneratorCode()).append(", ");
            }
            appendQuestions(query, columns.size());
            query.append(')');
        }
        return query.toString();
    }
}
