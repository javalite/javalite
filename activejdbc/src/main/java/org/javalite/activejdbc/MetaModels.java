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
package org.javalite.activejdbc;

import org.javalite.activejdbc.associations.Association;
import org.javalite.activejdbc.associations.Many2ManyAssociation;
import org.javalite.activejdbc.logging.LogFilter;
import org.javalite.activejdbc.logging.LogLevel;
import org.javalite.common.CaseInsensitiveMap;
import org.javalite.common.JsonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.*;

import static java.util.Collections.emptyList;
import static org.javalite.common.Collections.map;

/**
 * @author Igor Polevoy
 */
class MetaModels {

    private static final String DB_NAME = "dbName";
    private static final String DB_TYPE = "dbType";
    private static final String MODEL_CLASS = "modelClass";
    private static final String COLUMN_METADATA = "columnMetadata";
    private static final String COLUMN_METADATA_NAME = "columnName";
    private static final String COLUMN_METADATA_TYPE = "typeName";
    private static final String COLUMN_METADATA_SIZE = "columnSize";
    private static final String ASSOCIATIONS = "associations";

    private static final Logger LOGGER = LoggerFactory.getLogger(MetaModels.class);

    private final Map<String, MetaModel> metaModelsByTableName = new CaseInsensitiveMap<>();
    private final Map<String, MetaModel> metaModelsByClassName = new HashMap<>();
    //these are all many to many associations across all models.
    private final List<Many2ManyAssociation> many2ManyAssociations = new ArrayList<>();
    private final Map<Class, ModelRegistry> modelRegistries = new HashMap<>();

    void addMetaModel(MetaModel mm, Class<? extends Model> modelClass) {
        Object o = metaModelsByClassName.put(modelClass.getName(), mm);
        if (o != null) {
            LogFilter.log(LOGGER, LogLevel.WARNING, "Double-register: {}: {}", modelClass, o);
        }
        o = metaModelsByTableName.put(mm.getTableName(), mm);
        many2ManyAssociations.addAll(mm.getManyToManyAssociations());
        if (o != null) {
            LogFilter.log(LOGGER, LogLevel.WARNING, "Double-register: {}: {}", mm.getTableName(), o);
        }
    }

    ModelRegistry getModelRegistry(Class<? extends Model> modelClass) {
        return modelRegistries.computeIfAbsent(modelClass, k -> new ModelRegistry());
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

    protected String toJSON() {
        List models = new ArrayList();
        metaModelsByTableName.values().forEach(metaModel -> {
            
            try {
                List associations = new ArrayList();
            metaModel.getAssociations().forEach(association -> associations.add(association.toMap()));
            models.add(map(
                    MODEL_CLASS, metaModel.getModelClass().getName(),
                    DB_TYPE, metaModel.getDbType(),
                    DB_NAME, metaModel.getDbName(),
                    COLUMN_METADATA, metaModel.getColumnMetadata(),
                    ASSOCIATIONS, associations
            ));
            } catch(InitException ex) {
                LogFilter.log(LOGGER, LogLevel.WARNING, "Failed to retrieve metadata for table: '{}'."
                    + " Are you sure this table exists? For some databases table names are case sensitive.",
                    metaModel.getTableName());
            }
            
        });
        return JsonHelper.toJsonString(models,false);
    }

    protected void fromJSON(String json) {
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            MethodType methodType = MethodType.methodType(void.class, Map.class);

            for(Object o : JsonHelper.toList(json)) {
                Map metaModelMap = (Map) o;

                MetaModel metaModel = new MetaModel(
                        (String) metaModelMap.get(DB_NAME),
                        (Class<? extends Model>) Class.forName((String) metaModelMap.get(MODEL_CLASS)),
                        (String) metaModelMap.get(DB_TYPE)
                );

                Map<String, ColumnMetadata> columnMetadataMap = new CaseInsensitiveMap<>();
                ((Map) metaModelMap.getOrDefault(COLUMN_METADATA, map())).forEach((column, map) -> {
                    Map metadata = (Map) map;
                    columnMetadataMap.put(
                            (String) column,
                            new ColumnMetadata(
                                    (String) metadata.get(COLUMN_METADATA_NAME),
                                    (String) metadata.get(COLUMN_METADATA_TYPE),
                                    (Integer) metadata.get(COLUMN_METADATA_SIZE)
                            )
                    );
                });
                metaModel.setColumnMetadata(columnMetadataMap);

                for(Object a : (List) metaModelMap.getOrDefault(ASSOCIATIONS, emptyList())) {
                    Map map = ((Map) a);
                    metaModel.addAssociation(
                            (Association) lookup.findConstructor(
                                    Class.forName((String) map.get(Association.CLASS)),
                                    methodType
                            ).invoke(map)
                    );
                }

                addMetaModel(metaModel, metaModel.getModelClass());
            }
        } catch(Throwable e) {
            throw new InitException("Cannot load metadata", e);
        }
    }
}
