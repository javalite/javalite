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
package org.javalite.activejdbc;

import org.javalite.activejdbc.associations.Many2ManyAssociation;
import org.javalite.activejdbc.logging.LogFilter;
import org.javalite.activejdbc.logging.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Igor Polevoy
 */
class MetaModels {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetaModels.class);

    private final Map<String, MetaModel> metaModelsByTableName = new CaseInsensitiveMap<>();
    private final Map<String, MetaModel> metaModelsByClassName = new HashMap<>();
    //these are all many to many associations across all models.
    private final List<Many2ManyAssociation> many2ManyAssociations = new ArrayList<>();

    void addMetaModel(MetaModel mm, Class<? extends Model> modelClass) {
        Object o = metaModelsByClassName.put(modelClass.getName(), mm);
        if (o != null) {
            LogFilter.log(LOGGER, LogLevel.WARNING, "Double-register: {}: {}", modelClass, o);
        }
        o = metaModelsByTableName.put(mm.getTableName(), mm);
        many2ManyAssociations.addAll(mm.getManyToManyAssociations(Collections.<Association>emptyList()));
        if (o != null) {
            LogFilter.log(LOGGER, LogLevel.WARNING, "Double-register: {}: {}", mm.getTableName(), o);
        }
    }

    MetaModel getMetaModel(Class<? extends Model> modelClass) {
        return metaModelsByClassName.get(modelClass.getName());
    }

    MetaModel getMetaModel(String tableName) {
        return metaModelsByTableName.get(tableName);
    }

    String[] getTableNames(String dbName) {

        ArrayList<String> tableNames = new ArrayList<>();
        for (MetaModel metaModel : metaModelsByTableName.values()) {
            if (metaModel.getDbName().equals(dbName))
                tableNames.add(metaModel.getTableName());
        }
        return tableNames.toArray(new String[tableNames.size()]);
    }

    Class<? extends Model> getModelClass(String tableName) {
        MetaModel mm = metaModelsByTableName.get(tableName);
        return mm == null ? null : mm.getModelClass();
    }

    String getTableName(Class<? extends Model> modelClass) {
        return metaModelsByClassName.containsKey(modelClass.getName())?
            metaModelsByClassName.get(modelClass.getName()).getTableName():null;
    }

    public void setColumnMetadata(String table, Map<String, ColumnMetadata> metaParams) {
        metaModelsByTableName.get(table).setColumnMetadata(metaParams);
    }

    /**
     * An edge is a table in a many to many relationship that is not a join.
     *
     * @param join join table
     *
     * @return edges for a join.
     */
    protected List<String> getEdges(String join) {
        List<String> results = new ArrayList<>();
        for (Many2ManyAssociation a : many2ManyAssociations) {
            if (a.getJoin().equalsIgnoreCase(join)) {
                results.add(getMetaModel(a.getSourceClass()).getTableName());
                results.add(getMetaModel(a.getTargetClass()).getTableName());
            }
        }
        return results;
    }
}
