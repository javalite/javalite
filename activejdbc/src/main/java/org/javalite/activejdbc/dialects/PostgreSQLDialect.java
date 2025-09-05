package org.javalite.activejdbc.dialects;

import org.javalite.activejdbc.ColumnMetadata;
import org.javalite.activejdbc.MetaModel;
import org.javalite.common.ConversionException;
import org.javalite.common.Util;

import java.sql.Array;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    public String formSelect(String tableName, String[] columns, String subQuery, List<String> orderBys, long limit, long offset, boolean lockForUpdate) {
        StringBuilder fullQuery = new StringBuilder();
        
        appendSelect(fullQuery, tableName, columns, null, subQuery, orderBys);

        if(limit != -1){
            fullQuery.append(" LIMIT ").append(limit);
        }

        if(offset != -1){
            fullQuery.append(" OFFSET ").append(offset);
        }
        
        if(lockForUpdate){
            fullQuery.append(" FOR UPDATE NOWAIT");
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
            join(query, columns, ", ");
            query.append(") VALUES (");
            if (addIdGeneratorCode) {
                query.append(metaModel.getIdGeneratorCode()).append(", ");
            }
            appendTypedQuestionsInsert(metaModel, query, columns);
            query.append(')');
        }
        return query.toString();
    }

    /**
     * Attention, this method has a side effect, it modifies the "query" parameter
     *
     * query - example passed in: "INSERT INTO statuses (status) VALUES ("
     * query - after this method: "INSERT INTO statuses (status) VALUES (?::status_type"
     *
     * In other words,  it appends a type for every value placeholder.
     */
    public void appendTypedQuestionsInsert(MetaModel metaModel, StringBuilder query, List<String> columns) {
        Map<String, ColumnMetadata> columnMetadataMap = metaModel.getColumnMetadata();

        List<String> types = new ArrayList<>();
        for (String column : columns) {
            //WTF, Postgres????
            String key = column.startsWith("\"") && column.endsWith("\"")
                    ? column.substring(1, column.length() - 1)
                    : column;

            ColumnMetadata metadata = columnMetadataMap.get(key);
            String  type =  metadata.getTypeName();
            if(type.equalsIgnoreCase("serial")){
                types.add("?");
            }else{
                types.add("?::"+ type);
            }
        }
        query.append(Util.join(types, ","));
    }

    /**
     * Appends PostgreSQL - specific code: "description = ?::varchar,status = ?::status_type"
     */
    public void appendQuestionsForUpdate(MetaModel metaModel, StringBuilder query, List<String> columns) {
        Map<String, ColumnMetadata> columnMetadataMap = metaModel.getColumnMetadata();

        List<String> tokens = new ArrayList<>();
        for (String column : columns) {
            //WTF, Postgres????
            String key = column.startsWith("\"") && column.endsWith("\"")
                    ? column.substring(1, column.length() - 1)
                    : column;

            ColumnMetadata metadata = columnMetadataMap.get(key);
            String  typeName =  metadata.getTypeName();
            String  columnName  =  metadata.getColumnName();
            if(typeName.equalsIgnoreCase("serial")){
                tokens.add("?");
            }else{
                tokens.add(columnName + " = ?::"+ typeName);
            }
        }
        query.append(Util.join(tokens, ","));
    }


    public Array toArray(String typeName, Object value, Connection connection) {
        try {
            if (value == null) {
                return null;
            } else if (value instanceof Array) {
                return (Array) value;
            } else {
                if (connection != null) {
                    return connection.createArrayOf(typeName.substring(1), (Object[]) value);
                } else {
                    throw new ConversionException("Failed to convert " + value + " to Array, no connection provided.");
                }
            }
        } catch (ConversionException e) {
            throw e;
        } catch (Exception e) {
            throw new ConversionException(e);
        }
    }
}
