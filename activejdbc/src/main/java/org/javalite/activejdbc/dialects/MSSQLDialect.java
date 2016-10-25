package org.javalite.activejdbc.dialects;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.javalite.activejdbc.CaseInsensitiveMap;
import org.javalite.activejdbc.DBException;
import org.javalite.activejdbc.MetaModel;

import static org.javalite.common.Util.join;

public class MSSQLDialect extends DefaultDialect {
    protected final Pattern selectPattern = Pattern.compile("^\\s*SELECT\\s*",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    /**
     * Generates adds limit, offset and order bys to a sub-query
     *
     * @param tableName name of table. If table name is null, then the subQuery parameter is considered to be a full query, and all that needs to be done is to
     * add limit, offset and order bys
     * @param subQuery sub-query or a full query
     * @param columns - not implemented in this dialog
     * @param orderBys
     * @param limit
     * @param offset
     * @return query with
     */
    @Override
    public String formSelect(String tableName, String[] columns, String subQuery, List<String> orderBys, long limit, long offset) {
        boolean needLimit = limit != -1;
        boolean needOffset = offset != -1;

        if(needOffset && (orderBys == null || orderBys.isEmpty())) {
        	throw new DBException("MSSQL offset queries require an order by column.");
        }

        boolean keepSelect = false;
        StringBuilder fullQuery = new StringBuilder();
        if (needOffset) {
            fullQuery.append("SELECT " + getSQColumns(columns) + " FROM (SELECT ROW_NUMBER() OVER (ORDER BY ");
            join(fullQuery, orderBys, ", ");
            fullQuery.append(") AS rownumber,");
        } else if (needLimit) {
            fullQuery.append("SELECT TOP ").append(limit);
        } else {
            keepSelect = true;
        }

		if (tableName == null) { //table is in the sub-query already
            if (keepSelect) {
                fullQuery.append(subQuery);
            } else {
                Matcher m = selectPattern.matcher(subQuery);
                if (m.find()) {
                    fullQuery.append(' ').append(subQuery.substring(m.end()));
                } else {
                    fullQuery.append(subQuery);
                }
            }
        } else {
            if (keepSelect) { fullQuery.append("SELECT"); }
            fullQuery.append(getAllColumns(columns)).append(" FROM ").append(tableName);
            appendSubQuery(fullQuery, subQuery);
        }

        if (needOffset) {
            // T-SQL offset starts with 1, not like MySQL with 0;
            if (needLimit) {
                fullQuery.append(") AS sq WHERE rownumber BETWEEN ").append(offset + 1)
                        .append(" AND ").append(limit + offset);
            } else {
                fullQuery.append(") AS sq WHERE rownumber >= ").append(offset + 1);
            }
        } else {
            appendOrderBy(fullQuery, orderBys);
        }

        return fullQuery.toString();
    }

    private String getSQColumns(String[] columns){

        if(columns == null){
            return "sq.*";
        }

        List<String> names = new ArrayList<>();
        for (String column : columns) {
            names.add("sq." + column);
        }
        return join(names, ", ");
    }

    private String getAllColumns(String[] columns){
        return columns == null ? " *" : " " + join(columns, ", ");
    }

    /**
     * TDS converts a number of important data types to String. This isn't what we want, nor helpful. Here, we change them back.
     */
    @Override
    public Object overrideDriverTypeConversion(MetaModel mm, String attributeName, Object value) {
        if (value instanceof String) {
            String typeName = mm.getColumnMetadata().get(attributeName).getTypeName();
            if ("date".equalsIgnoreCase(typeName)) {
                return java.sql.Date.valueOf((String) value);
            } else if ("datetime2".equalsIgnoreCase(typeName)) {
                return java.sql.Timestamp.valueOf((String) value);
            }
        }
        return value;
    }

    @Override
    protected void appendDate(StringBuilder query, java.sql.Date value) {
        query.append("CONVERT(date, '").append(value.toString()).append("')");
    }

    @Override
    protected void appendTime(StringBuilder query, java.sql.Time value) {
        query.append("CONVERT(time, '").append(value.toString()).append("')");
    }

    @Override
    protected void appendTimestamp(StringBuilder query, java.sql.Timestamp value) {
        query.append("CONVERT(datetime2, '").append(value.toString()).append("')");
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
            query.append("[");
            join(query, columns, "], [");
            query.append("]) VALUES (");
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
            query.append("[");
            join(query, attributes.keySet(), "], [");
            query.append("]) VALUES (");
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
            query.append("[").append(attribute.getKey()).append("] = ");
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
                query.append(i == 0 ? " WHERE " : " AND ").append("[").append(compositeKeys[i]).append("]").append(" = ");
                appendValue(query, attributes.get(compositeKeys[i]));
            }
        }
        return query.toString();
    }
}
