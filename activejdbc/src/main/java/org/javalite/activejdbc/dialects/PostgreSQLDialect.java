package org.javalite.activejdbc.dialects;

import org.javalite.common.Util;

import java.util.List;
import org.javalite.activejdbc.MetaModel;

public class PostgreSQLDialect extends DefaultDialect {

    @Override
    public String selectStar(String table, String query) {
        SqlStringBuilder sb = new SqlStringBuilder("SELECT * FROM ");
        return query != null
                ? sb.appendQuotedIdentifier(table).append(" WHERE ").append(query).toString()
                : sb.appendQuotedIdentifier(table).toString();
    }

    @Override
    public String selectStarParametrized(String table, String... parameters) {
        SqlStringBuilder sql = new SqlStringBuilder("SELECT * FROM ").appendQuotedIdentifier(table).append(" WHERE ");

        for (String parameter : parameters) {
            sql.appendQuotedIdentifier(parameter).append(" = ? AND ");
        }
        return sql.substring(0, sql.length() - 5);//remove last comma
    }

    /**
     * Generates adds limit, offset and order bys to a sub-query
     *
     * @param tableName name of table. If table name is null, then the subQuery
     * parameter is considered to be a full query, and all that needs to be done
     * is to add limit, offset and order bys
     * @param subQuery sub-query or a full query
     * @param orderBys
     * @param limit
     * @param offset
     * @return query with
     */
    @Override
    public String formSelect(String tableName, String subQuery, List<String> orderBys, long limit, long offset) {

        SqlStringBuilder fullQuery = new SqlStringBuilder();
        if (tableName == null) {
            fullQuery.append(subQuery);
        } else {
            fullQuery.append("SELECT  * FROM ").appendQuotedIdentifier(tableName);
            if (!Util.blank(subQuery)) {
                if (!groupByPattern.matcher(subQuery.toLowerCase().trim()).find()
                        && !orderByPattern.matcher(subQuery.toLowerCase().trim()).find()) {
                    fullQuery.append(" WHERE ");
                }
                fullQuery.append(" ").append(subQuery);
            }
        }

        if (orderBys.size() != 0) {
            fullQuery.append(" ORDER BY ").appendQuotedIdentifier(Util.join(orderBys, new SqlStringBuilder().appendQuotedIdentifier(", ").toString()));
        }

        if (limit != -1) {
            fullQuery.append(" LIMIT ").append(limit);
        }

        if (offset != -1) {
            fullQuery.append(" OFFSET ").append(offset);
        }

        return fullQuery.toString();
    }

    @Override
    public String createParametrizedInsert(MetaModel mm, List<String> nonNullAttributes) {

        return new SqlStringBuilder("INSERT INTO ").appendQuotedIdentifier(mm.getTableName()).append(" (")
            .appendQuotedIdentifier(Util.join(nonNullAttributes, new SqlStringBuilder().appendQuotedIdentifier(", ").toString()))
            .append(mm.getIdGeneratorCode() != null ? new SqlStringBuilder(", ").appendQuotedIdentifier(mm.getIdName()).toString() : "")
            .append(mm.isVersioned() ? ", record_version" : "")
            .append(") VALUES (").append(getQuestions(nonNullAttributes.size()))
            .append(mm.getIdGeneratorCode() != null ? ", " + mm.getIdGeneratorCode() : "")
            .append(mm.isVersioned() ? ", 1" : "")
            .append(")").toString();
    }

    @Override
    public String createParametrizedInsertIdUnmanaged(MetaModel mm, List<String> nonNullAttributes) {
        
        return new SqlStringBuilder("INSERT INTO ").appendQuotedIdentifier(mm.getTableName()).append(" (")
            .appendQuotedIdentifier(Util.join(nonNullAttributes, new SqlStringBuilder().appendQuotedIdentifier(", ").toString()))
            .append(mm.isVersioned() ? ", record_version" : "")
            .append(") VALUES (").append(getQuestions(nonNullAttributes.size()))
            .append(mm.isVersioned() ? ", 1" : "")
            .append(")").toString();
    }

    @Override
    public String DB_count(String table) {
        return new SqlStringBuilder("SELECT COUNT(*) FROM ").appendQuotedIdentifier(table).toString();
    }

    @Override
    public String DB_count(String table, String where) {
        return new SqlStringBuilder("SELECT COUNT(*) FROM ").appendQuotedIdentifier(table).append(" WHERE ").append(where).toString();
    }

    @Override
    public String LazyList_processOther(String target, String sourceFkName, String join, String targetPk, String targetFkName, List ids) {
        return new SqlStringBuilder("SELECT ").appendQuotedIdentifier(target).append(".*, t.").appendQuotedIdentifier(sourceFkName)
                .append(" AS the_parent_record_id FROM ").appendQuotedIdentifier(target).append(" INNER JOIN ").appendQuotedIdentifier(join)
                .append(" t ON ").appendQuotedIdentifier(target).append(".").appendQuotedIdentifier(targetPk).append(" = t.")
                .appendQuotedIdentifier(targetFkName).append(" WHERE (t.").appendQuotedIdentifier(sourceFkName)
                .append("  IN (").append(Util.join(ids, ", ")).append("))").toString();
    }

    @Override
    public String ModelDelegate_update(String tableName, String updates, String conditions) {
        return new SqlStringBuilder("UPDATE ").appendQuotedIdentifier(tableName).append(" SET ").append(updates)
                .append((conditions != null) ? " WHERE " + conditions : "").toString();
    }

    @Override
    public String Model_delete(String tableName, String idName) {
        return new SqlStringBuilder("DELETE FROM ").appendQuotedIdentifier(tableName).append(" WHERE ").appendQuotedIdentifier(idName).append("= ?").toString();
    }

    @Override
    public String Model_deleteJoinsForManyToMany(String join, String sourceFK, String id) {
        return new SqlStringBuilder("DELETE FROM ").appendQuotedIdentifier(join).append(" WHERE ").appendQuotedIdentifier(sourceFK).append(" = ")
                .append(id).toString();
    }

    @Override
    public String Model_deleteOne2ManyChildrenShallow(String target, String fkName) {
        return new SqlStringBuilder("DELETE FROM ").appendQuotedIdentifier(target).append(" WHERE ").appendQuotedIdentifier(fkName).append(" = ?").toString();
    }

    @Override
    public String Model_deletePolymorphicChildrenShallow(String target) {
        return new SqlStringBuilder("DELETE FROM ").appendQuotedIdentifier(target).append(" WHERE parent_id = ? AND parent_type = ?").toString();
    }

    @Override
    public String Model_staticDelete(String tableName, String query) {
        return new SqlStringBuilder("DELETE FROM ").appendQuotedIdentifier(tableName).append(" WHERE ").append(query).toString();
    }

    @Override
    public String Model_exists(String idName, String tableName) {
        return new SqlStringBuilder("SELECT ").appendQuotedIdentifier(idName).append(" FROM ").appendQuotedIdentifier(tableName)
                .append(" WHERE ").appendQuotedIdentifier(idName).append(" = ?").toString();
    }

    @Override
    public String Model_deleteAll(String tableName) {
        return new SqlStringBuilder("DELETE FROM ").appendQuotedIdentifier(tableName).toString();
    }

    @Override
    public String Model_get(String targetTable, String joinTable, String targetId, String targetFkName, String sourceFkName, String id, String additionalCriteria) {
        return new SqlStringBuilder("SELECT ").appendQuotedIdentifier(targetTable).append(".* FROM ").appendQuotedIdentifier(targetTable).append(", ")
                .appendQuotedIdentifier(joinTable).append(" WHERE ").appendQuotedIdentifier(targetTable).append(".").appendQuotedIdentifier(targetId)
                .append(" = ").appendQuotedIdentifier(joinTable).append(".").appendQuotedIdentifier(targetFkName).append(" AND ")
                .appendQuotedIdentifier(joinTable).append(".").appendQuotedIdentifier(sourceFkName).append(" = ").append(id).append(additionalCriteria)
                .toString();
    }

    @Override
    public String Model_add(String join, String sourceFkName, String targetFkName, String id, String childId) {
        return new SqlStringBuilder("INSERT INTO ").appendQuotedIdentifier(join).append(" ( ").appendQuotedIdentifier(sourceFkName).append(", ")
                .appendQuotedIdentifier(targetFkName).append(" ) VALUES ( ").append(id).append(", ").append(childId).append(")").toString();
    }

    @Override
    public String Model_remove(String join, String sourceFkName, String targetFkName) {
        return new SqlStringBuilder("DELETE FROM ").appendQuotedIdentifier(join).append(" WHERE ").appendQuotedIdentifier(sourceFkName).append(" = ? AND ")
                .appendQuotedIdentifier(targetFkName).append(" = ?").toString();
    }

    @Override
    public String Model_count(String tableName) {
        return new SqlStringBuilder("SELECT COUNT(*) FROM ").appendQuotedIdentifier(tableName).toString();
    }

    @Override
    public String Model_count(String tableName, String query) {
        return new SqlStringBuilder("SELECT COUNT(*) FROM ").appendQuotedIdentifier(tableName).append(" WHERE ").append(query).toString();
    }

    @Override
    public String Model_updatePartial(String tableName, List<String> names) {
        SqlStringBuilder query = new SqlStringBuilder("UPDATE ").appendQuotedIdentifier(tableName).append(" SET ");
        for (int i = 0; i < names.size(); i++) {
            query.appendQuotedIdentifier(names.get(i)).append("= ?");
            if (i < names.size() - 1) {
                query.append(", ");
            }
        }
        return query.toString();
    }

    @Override
    public String Model_toInsert(String tableName, List<String> names, List<Object> values) {
        return new SqlStringBuilder("INSERT INTO ")
                .appendQuotedIdentifier(tableName).append(" (")
                .appendQuotedIdentifier(Util.join(names, new SqlStringBuilder().appendQuotedIdentifier(", ").toString()))
                .append(") VALUES (").append(Util.join(values, ", ")).append(")").toString();
    }

    @Override
    public String Paginator_Paginator(boolean fullQuery, String query, String tableName) {
        return fullQuery ? new SqlStringBuilder("SELECT COUNT(*) ").append(query.substring(query.toLowerCase().indexOf("from"))).toString()
                : new SqlStringBuilder("SELECT COUNT(*) FROM ").appendQuotedIdentifier(tableName).append(" WHERE ").append(query).toString();
    }

    @Override
    public String getDefaultConvertedCase(String string) {
        return string;
    }
    
    class SqlStringBuilder {

        final StringBuilder sb;

        SqlStringBuilder() {
            sb = new StringBuilder();
        }

        SqlStringBuilder(String string) {
            sb = new StringBuilder(string);
        }

        SqlStringBuilder appendQuotedIdentifier(String identifier) {
            sb.append("\"").append(identifier).append("\"");
            return this;
        }

        public SqlStringBuilder append(SqlStringBuilder sqlStringBuilder){
            sb.append(sqlStringBuilder.toStringBuilder());
            return this;
        }
        public SqlStringBuilder append(StringBuilder sb) {
            sb.append(sb);
            return this;
        }

        public SqlStringBuilder append(String string) {
            sb.append(string);
            return this;
        }

        public StringBuilder toStringBuilder() {
            return sb;
        }

        public SqlStringBuilder append(long l) {
            sb.append(l);
            return this;
        }

        @Override
        public String toString() {
            return sb.toString();
        }

        public String substring(int start, int end) {
            return sb.substring(start, end);
        }

        public int length() {
            return sb.length();
        }
    }
}
