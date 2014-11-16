/*
Copyright 2009-2014 Igor Polevoy

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Igor Polevoy
 */
class MetaModels {

    private final static Logger logger = LoggerFactory.getLogger(MetaModels.class);

    private final Map<String, MetaModel> metaModelsByTableName = new HashMap<String, MetaModel>();
    private final Map<Class<? extends Model>, MetaModel> metaModelsByClass = new HashMap<Class<? extends Model>, MetaModel>();
    private final Map<String, MetaModel> metaModelsByClassName = new HashMap<String, MetaModel>();
    //these are all many to many associations across all models.
    private final List<Many2ManyAssociation> many2ManyAssociations = new ArrayList<Many2ManyAssociation>();

    void addMetaModel(MetaModel<?, ?> mm, Class<? extends Model> modelClass) {
        Object o = metaModelsByClass.put(modelClass, mm);
        if (o != null) {
            logger.warn("Double-register: {}: {}", modelClass, o);
        }
        o = metaModelsByTableName.put(mm.getTableName(), mm);
        many2ManyAssociations.addAll(mm.getManyToManyAssociations(Collections.<Association>emptyList()));
        if (o != null) {
            logger.warn("Double-register: {}: {}", mm.getTableName(), o);
        }

        metaModelsByClassName.put(modelClass.getName(), mm);
    }

    MetaModel getMetaModelByClassName(String className) {
        return metaModelsByClassName.get(className);
    }

    MetaModel getMetaModel(Class<? extends Model> modelClass) {
        return metaModelsByClass.get(modelClass);
    }

    MetaModel getMetaModel(String tableName) {
        MetaModel mm = metaModelsByTableName.get(tableName);
        if (mm != null) {
            return mm;
        }
        mm = metaModelsByTableName.get(tableName.toLowerCase());
        if (mm != null) {
            return mm;
        }
        return metaModelsByTableName.get(tableName.toUpperCase());
    }

    String[] getTableNames(String dbName) {

        ArrayList<String> tableNames = new ArrayList<String>();
        for (MetaModel metaModel : metaModelsByTableName.values()) {
            if (metaModel.getDbName().equals(dbName))
                tableNames.add(metaModel.getTableName());
        }
        return tableNames.toArray(new String[tableNames.size()]);
    }

    Class getModelClass(String tableName) {
        return  metaModelsByTableName.containsKey(tableName) ? metaModelsByTableName.get(tableName).getModelClass() : null;
    }

    String getTableName(Class<? extends Model> modelClass) {
        MetaModel mm = metaModelsByClass.get(modelClass);
        return mm == null ? null : mm.getTableName();
    }

    public void setColumnMetadata(String table, Map<String, ColumnMetadata> metaParams) {
        metaModelsByTableName.get(table).setColumnMetadata(metaParams);
    }

    protected List<String> getEdges(String join) {
        List<String> results = new ArrayList<String>();
        for (Many2ManyAssociation a : many2ManyAssociations) {
            if (a.getJoin().equalsIgnoreCase(join)){
                results.add(a.getSource());
                results.add(a.getTarget());
            }
        }
        return results;
    }
}
