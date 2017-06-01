package org.javalite.activejdbc.dialects;

import org.javalite.activejdbc.CaseInsensitiveMap;
import org.javalite.activejdbc.MetaModel;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.javalite.common.Util.join;


public class PostgreSQLDialect extends DefaultDialect {

    /**
     * Generates adds limit, offset and order bys to a sub-query
     *
     * @param tableName name of table. If table name is null, then the subQuery parameter is considered to be a full query, and all that needs to be done is to
     * add limit, offset and order bys
     * @param columns not used in this implementation
     * @param subQuery sub-query or a full query
     * @param orderBys
     * @param limit
     * @param offset
     * @return query with
     */
    @Override
    public String formSelect(String tableName, String[] columns, String subQuery, List<String> orderBys, long limit, long offset) {
        StringBuilder fullQuery = new StringBuilder();
        
        appendSelect(fullQuery, tableName, columns, null, subQuery, orderBys);

        if(limit != -1){
            fullQuery.append(" LIMIT ").append(limit);
        }

        if(offset != -1){
            fullQuery.append(" OFFSET ").append(offset);
        }

        return fullQuery.toString();
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
            query.append("\"");
            join(query, columns, "\", \"");
            query.append("\") VALUES (");
            query.append(") VALUES (");
            if (addIdGeneratorCode) {
                query.append(metaModel.getIdGeneratorCode()).append(", ");
            }
            appendQuestions(query, columns.size());
            query.append(')');
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
            query.append("\"");
            join(query, attributes.keySet(), "\", \"");
            query.append("\") VALUES (");
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
            query.append("\"").append(attribute.getKey()).append("\" = ");
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
                query.append(i == 0 ? " WHERE " : " AND ").append("\"").append(compositeKeys[i]).append("\"").append(" = ");
                appendValue(query, attributes.get(compositeKeys[i]));
            }
        }
        return query.toString();
    }
}
