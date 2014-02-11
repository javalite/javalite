package org.javalite.activejdbc;

import org.javalite.activejdbc.cache.QueryCache;

import java.sql.Timestamp;
import java.util.List;

/**
 * @author Igor Polevoy: 4/25/12 2:45 AM
 */
class ModelDelegate {

    static int update(MetaModel metaModel, String updates, String conditions, Object... params) {

        //TODO: validate that the number of question marks is the same as number of parameters

        Object[] allParams;
        if (metaModel.hasAttribute("updated_at")) {
            updates = "updated_at = ?, " + updates;
            allParams = new Object[params.length + 1];
            System.arraycopy(params, 0, allParams, 1, params.length);

            allParams[0] = new Timestamp(System.currentTimeMillis());
        } else {
            allParams = params;
        }
        String sql = "UPDATE " + metaModel.getTableName() + " SET " + updates + ((conditions != null) ? " WHERE " + conditions : "");
        int count = new DB(metaModel.getDbName()).exec(sql, allParams);
        if (metaModel.cached()) {
            QueryCache.instance().purgeTableCache(metaModel.getTableName());
        }
        return count;
    }

    static String[] toLowerCase(String[] arr) {
        String[] newArr = new String[arr.length];
        for (int i = 0; i < newArr.length; i++) {
            newArr[i] = arr[i].toLowerCase();
        }
        return newArr;
    }

    static void setNamesAndValues(Model m, Object... namesAndValues) {

        String[] names = new String[namesAndValues.length / 2];
        Object[] values = new Object[namesAndValues.length / 2];
        int j = 0;
        for (int i = 0; i < namesAndValues.length - 1; i += 2, j++) {
            if (namesAndValues[i] == null) throw new IllegalArgumentException("attribute names cannot be nulls");
            names[j] = namesAndValues[i].toString();
            values[j] = namesAndValues[i + 1];
        }
        m.set(names, values);
    }

    static void purgeEdges(MetaModel metaModel ){
        //this is to eliminate side effects of cache on associations.
        //TODO: Need to write tests for cases;
        // 1. One to many relationship. Parent and child are cached.
        //      When a new child inserted, the parent.getAll(Child.class) should see that
        // 2. Many to many. When a new join inserted, updated or deleted, the one.getAll(Other.class) should see the difference.

        //Purge associated targets

        List<Association> associations = metaModel.getAssociations();
        for(Association association: associations){
            QueryCache.instance().purgeTableCache(association.getTarget());
        }

        //Purge edges in case this model represents a join
        List<String> edges = Registry.instance().getEdges(metaModel.getTableName());
        for(String edge: edges){
            QueryCache.instance().purgeTableCache(edge);
        }
    }
}
