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

import org.javalite.activejdbc.annotations.*;
import org.javalite.activejdbc.associations.*;
import org.javalite.activejdbc.cache.CacheManager;
import org.javalite.activejdbc.cache.QueryCache;
import org.javalite.activejdbc.logging.LogFilter;
import org.javalite.activejdbc.logging.LogLevel;
import org.javalite.activejdbc.statistics.StatisticsQueue;
import org.javalite.common.CaseInsensitiveMap;
import org.javalite.common.Inflector;
import org.javalite.common.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


/**
 * @author Igor Polevoy
 * @author Eric Nielsen
 */
public enum Registry {

    //our singleton!
    INSTANCE;

    private static final Logger LOGGER = LoggerFactory.getLogger(Registry.class);

    private static final int STATIC_METADATA_CHECKED = 1;
    private static final int STATIC_METADATA_LOADED = 2;

    private final MetaModels metaModels = new MetaModels();
    private final Configuration configuration = new Configuration();
    private final StatisticsQueue statisticsQueue;
    private final Set<String> initedDbs = new HashSet<>();
    private int staticMetadataStatus = 0;
    private String modelFile = "activejdbc_models.properties";

    Registry() {
        statisticsQueue = configuration.collectStatistics()
                ? new StatisticsQueue(configuration.collectStatisticsOnHold())
                : null;
    }


    public static Registry instance() {
        return INSTANCE;
    }

    public StatisticsQueue getStatisticsQueue() {
        if (statisticsQueue == null) {
            throw new InitException("cannot collect statistics if this was not configured in activejdbc.properties file. Add 'collectStatistics = true' to it.");
        }
        return statisticsQueue;
    }

    public Configuration getConfiguration(){
        return configuration;
    }

    public static CacheManager cacheManager(){
        return QueryCache.instance().getCacheManager();
    }

    /**
     * Provides a MetaModel of a model representing a table.
     *
     * @param table name of table represented by this MetaModel.
     * @return MetaModel of a model representing a table.
     */
    public MetaModel getMetaModel(String table) {
        return metaModels.getMetaModel(table);
    }

    public MetaModel getMetaModel(Class<? extends Model> modelClass) {

        String dbName = MetaModel.getDbName(modelClass);
        init(dbName);

        return metaModels.getMetaModel(modelClass);
    }

    ModelRegistry modelRegistryOf(Class<? extends Model> modelClass) {
        return metaModels.getModelRegistry(modelClass);
    }

    synchronized void init(String dbName) {

        LOGGER.debug("Starting to get metadata from the database...");
        if (staticMetadataStatus == STATIC_METADATA_LOADED || initedDbs.contains(dbName)) {
            LOGGER.debug("Metadata  is already processed, exiting...");
            return;
        } else {
            initedDbs.add(dbName);
        }

        if (staticMetadataStatus != STATIC_METADATA_CHECKED && loadStaticMetadata()) return;

        try {
            Connection c = ConnectionsAccess.getConnection(dbName);
            if(c == null){
                throw new DBException("Failed to retrieve metadata from DB, connection: '" + dbName + "' is not available");
            }
            DatabaseMetaData databaseMetaData = c.getMetaData();
            String dbType = c.getMetaData().getDatabaseProductName();
            Set<Class<? extends Model>> modelClasses = ModelFinder.getModelsForDb(dbName);
            registerModels(dbName, modelClasses, dbType);
            String[] tables = metaModels.getTableNames(dbName);

            for (String table : tables) {
                Map<String, ColumnMetadata> metaParams = fetchMetaParams(databaseMetaData, dbType, table);
                registerColumnMetadata(table, metaParams);
            }

            for (String table : tables) {
                discoverAssociationsFor(table, dbName);
            }
            processOverrides(modelClasses);
        } catch (Exception e) {
            initedDbs.remove(dbName);
            if (e instanceof InitException) {
                throw (InitException) e;
            }
            if (e instanceof DBException) {
                throw (DBException) e;
            } else {
                throw new InitException(e);
            }
        }
    }

    private boolean loadStaticMetadata() {
        try {
            Enumeration<URL> urls = Registry.instance().getClass().getClassLoader().getResources("activejdbc_metadata.json");
            staticMetadataStatus = urls.hasMoreElements() ? STATIC_METADATA_LOADED : STATIC_METADATA_CHECKED;
            while(urls.hasMoreElements()) {
                URL url = urls.nextElement();
                LogFilter.log(LOGGER, LogLevel.INFO, "Loading metadata from: {}", url.toExternalForm());
                metaModels.fromJSON(Util.read(url.openStream()));
            }
            return staticMetadataStatus == STATIC_METADATA_LOADED;
        } catch(IOException e) {
            throw new InitException(e);
        }
    }

    //instrumentation
    protected String metadataToJSON() {
        return metaModels.toJSON();
    }


    /**
     * Some databases will improve the search of metadata if you give them the schema name
     *
     * @return  may return null
     *
     */
    private String getConnectionSchema(DatabaseMetaData databaseMetaData) throws SQLException {
        try {
            return databaseMetaData.getConnection().getSchema();
        } catch (SQLException e) {
            throw e;
        } catch (Exception ignore) {} // getSchema does not exist on android.
        return null;
    }


    /**
     * Some databases will improve the search of metadata if you give them the catalog name
     *
     * @return  may return null
     *
     */
    private String getConnectionCatalog(DatabaseMetaData databaseMetaData) throws SQLException {
        try {
            return databaseMetaData.getConnection().getCatalog();
        } catch (SQLException e) {
            throw e;
        } catch (Exception ignore) {} // getCatalog does not exist on android.
        return null;
    }


    /**
     * Workarounds for some DB idiosyncrasies
     */
    private String mangleTableName(String tableName, String dbType) {
        if(dbType.toLowerCase().contains("h2")){
            // keep quoted table names as is, otherwise use uppercase
            if (!tableName.contains("\"")) {
                tableName = tableName.toUpperCase();
            } else if(tableName.startsWith("\"") && tableName.endsWith("\"")) {
                tableName = tableName.substring(1, tableName.length() - 1);
            }
        } else if(dbType.toLowerCase().contains("postgres") && tableName.startsWith("\"") && tableName.endsWith("\"")) {
            tableName = tableName.substring(1, tableName.length() - 1);
        }
        return tableName;
    }

    /**
     * Returns a hash keyed off a column name.
     */
    private Map<String, ColumnMetadata> fetchMetaParams(DatabaseMetaData databaseMetaData, String dbType, String table) throws SQLException {

        /*
         * Valid table name format: tablename or schemaname.tablename
         */
        String[] parts = table.split("\\.", 3);

        String schema = null;

        String tableName = null;

        switch (parts.length) {
            case 1:
                schema = getConnectionSchema(databaseMetaData);
                tableName = parts[0];
                break;
            case 2:
                schema = parts[0];
                tableName = parts[1];
                break;
        }

        if (Util.blank(tableName) || (schema != null && schema.trim().length() == 0)) {
            throw new DBException("invalid table name : " + table);
        }

        String catalog = getConnectionCatalog(databaseMetaData);

        tableName = mangleTableName(tableName, dbType);

        ResultSet rs = databaseMetaData.getColumns(catalog, schema, tableName, null);
        Map<String, ColumnMetadata> columns = getColumns(rs, dbType);
        rs.close();

        //try upper case table name - Oracle uses upper case
        if (columns.isEmpty()) {
            rs = databaseMetaData.getColumns(catalog, schema, tableName.toUpperCase(), null);
            columns = getColumns(rs, dbType);
            rs.close();
        }

        //if upper case not found, try lower case.
        if (columns.isEmpty()) {
            rs = databaseMetaData.getColumns(catalog, schema, tableName.toLowerCase(), null);
            columns = getColumns(rs, dbType);
            rs.close();
        }

        //if table is back-quoted, try to remove quotes - helped with MySQL
        if (columns.isEmpty() && tableName.contains("`")) {
            tableName = tableName.substring(1, tableName.length() -1);
            rs = databaseMetaData.getColumns(catalog, schema, tableName.toLowerCase(), null);
            columns = getColumns(rs, dbType);
            rs.close();
        }

        if (columns.size() > 0) {
            LogFilter.log(LOGGER, LogLevel.INFO, "Fetched metadata for table: {}", table);
        } else {
            LogFilter.log(LOGGER, LogLevel.WARNING, "Failed to retrieve metadata for table: '{}'."
                    + " Are you sure this table exists? For some databases table names are case sensitive.",
                    table);
        }

        return columns;
    }


    /**
     *
     * @param modelClasses
     * @param dbType this is a name of a DBMS as returned by JDBC driver, such as Oracle, MySQL, etc.
     */
    private void registerModels(String dbName, Set<Class<? extends Model>> modelClasses, String dbType) {
        for (Class<? extends Model> modelClass : modelClasses) {
            MetaModel mm = new MetaModel(dbName, modelClass, dbType);
            metaModels.addMetaModel(mm, modelClass);
            LogFilter.log(LOGGER, LogLevel.INFO, "Registered model: {}", modelClass);
        }
    }

    private void processOverrides(Set<Class<? extends Model>> models) {

        for(Class<? extends Model> modelClass : models){

            BelongsTo belongsToAnnotation = modelClass.getAnnotation(BelongsTo.class);
            processOverridesBelongsTo(modelClass, belongsToAnnotation);

            BelongsToParents belongsToParentAnnotation = modelClass.getAnnotation(BelongsToParents.class);
            if (belongsToParentAnnotation != null){
                for (BelongsTo belongsTo : belongsToParentAnnotation.value()){
                    processOverridesBelongsTo(modelClass, belongsTo);
                }
            }

            HasManies hasManiesAnnotation = modelClass.getAnnotation(HasManies.class);
            if (hasManiesAnnotation != null) {
                for (HasMany hasMany : hasManiesAnnotation.value()) {
                    processOverridesHasMany(modelClass, hasMany);
                }
            }

            HasMany hasManyAnnotation = modelClass.getAnnotation(HasMany.class);
            processOverridesHasMany(modelClass, hasManyAnnotation);

            Many2Manies many2ManiesAnnotation = modelClass.getAnnotation(Many2Manies.class);
            if (many2ManiesAnnotation != null) {
                for (Many2Many many2Many : many2ManiesAnnotation.value()) {
                    processManyToManyOverrides(many2Many, modelClass);
                }
            }

            Many2Many many2manyAnnotation = modelClass.getAnnotation(Many2Many.class);
            if(many2manyAnnotation != null){
                processManyToManyOverrides(many2manyAnnotation, modelClass);
            }

            BelongsToPolymorphic belongsToPolymorphic = modelClass.getAnnotation(BelongsToPolymorphic.class);
            if(belongsToPolymorphic != null){
                processPolymorphic(belongsToPolymorphic, modelClass);
            }

            UnrelatedTo unrelatedTo = modelClass.getAnnotation(UnrelatedTo .class);
            if(unrelatedTo != null){
                processUnrelatedTo(unrelatedTo, modelClass);
            }

        }
    }

    private void processUnrelatedTo(UnrelatedTo unrelatedTo, Class<? extends Model> modelClass) {
        Class<? extends Model>[] related = unrelatedTo.value();
        for (Class<? extends Model> relatedClass : related) {
            MetaModel relatedMM = metaModels.getMetaModel(relatedClass);
            MetaModel thisMM = metaModels.getMetaModel(modelClass);
            if(relatedMM != null){
                Association association = relatedMM.getAssociationForTarget(modelClass);
                relatedMM.removeAssociationForTarget(modelClass);
                if(association != null){
                    LogFilter.log(LOGGER, LogLevel.INFO, "Removed association: " + association);
                }
            }
            Association association = thisMM.getAssociationForTarget(relatedClass);
            thisMM.removeAssociationForTarget(relatedClass);
            if(association != null){
                LogFilter.log(LOGGER, LogLevel.INFO, "Removed association: " + association);
            }
        }
    }

    private void processPolymorphic(BelongsToPolymorphic belongsToPolymorphic, Class<? extends Model> modelClass ) {
        Class<? extends Model>[] parentClasses = belongsToPolymorphic.parents();
        String[] typeLabels = belongsToPolymorphic.typeLabels();

        if (typeLabels.length > 0 && typeLabels.length != parentClasses.length) {
            throw new InitException("must provide all type labels for polymorphic associations");
        }

        for (int i = 0, parentClassesLength = parentClasses.length; i < parentClassesLength; i++) {
            Class<? extends Model> parentClass = parentClasses[i];

            String typeLabel = typeLabels.length > 0 ? typeLabels[i] : parentClass.getName();

            BelongsToPolymorphicAssociation belongsToPolymorphicAssociation =
                    new BelongsToPolymorphicAssociation(modelClass, parentClass, typeLabel, parentClass.getName());
            metaModels.getMetaModel(modelClass).addAssociation(belongsToPolymorphicAssociation);


            OneToManyPolymorphicAssociation oneToManyPolymorphicAssociation =
                    new OneToManyPolymorphicAssociation(parentClass, modelClass, typeLabel);
            metaModels.getMetaModel(parentClass).addAssociation(oneToManyPolymorphicAssociation);
        }
    }

    private void processManyToManyOverrides(Many2Many many2manyAnnotation, Class<? extends Model> modelClass){

        Class<? extends Model> otherClass = many2manyAnnotation.other();

        String source = metaModels.getTableName(modelClass);
        String target = metaModels.getTableName(otherClass);
        String join = many2manyAnnotation.join();
        String sourceFKName = many2manyAnnotation.sourceFKName();
        String targetFKName = many2manyAnnotation.targetFKName();
        String otherPk;
        String thisPk;
        try {
            Method m = modelClass.getMethod("getMetaModel");
            MetaModel mm = (MetaModel) m.invoke(modelClass);
            thisPk = mm.getIdName();
            m = otherClass.getMethod("getMetaModel");
            mm = (MetaModel) m.invoke(otherClass);
            otherPk = mm.getIdName();
        } catch (Exception e) {
            throw new InitException("failed to determine PK name in many to many relationship", e);
        }

        Association many2many1 = new Many2ManyAssociation(modelClass, otherClass, join, sourceFKName, targetFKName, otherPk);
        metaModels.getMetaModel(source).addAssociation(many2many1);

        Association many2many2 = new Many2ManyAssociation(otherClass, modelClass, join, targetFKName, sourceFKName, thisPk);
        metaModels.getMetaModel(target).addAssociation(many2many2);
    }

    private void processOverridesBelongsTo(Class<? extends Model> modelClass, BelongsTo belongsToAnnotation) {
        if(belongsToAnnotation != null){
            Class<? extends Model> parentClass = belongsToAnnotation.parent();
            String foreignKeyName = belongsToAnnotation.foreignKeyName();

            if (metaModels.getMetaModel(parentClass).hasAssociation(modelClass, OneToManyAssociation.class)) {
                LogFilter.log(LOGGER, LogLevel.WARNING, "Redundant annotations used: @BelongsTo and @HasMany on a "
                    + "relationship between Model {} and Model {}.", modelClass.getName(), parentClass.getName());
                return;
            }

            Association hasMany = new OneToManyAssociation(parentClass, modelClass, foreignKeyName);
            Association belongsTo = new BelongsToAssociation(modelClass, parentClass, foreignKeyName);

            metaModels.getMetaModel(parentClass).addAssociation(hasMany);
            metaModels.getMetaModel(modelClass).addAssociation(belongsTo);
        }
	}


    private void processOverridesHasMany(Class<? extends Model> modelClass, HasMany hasManyAnnotation) {
        if(hasManyAnnotation != null){
            Class<? extends Model> childClass = hasManyAnnotation.child();
            String foreignKeyName = hasManyAnnotation.foreignKeyName();

            if (metaModels.getMetaModel(childClass).hasAssociation(modelClass, OneToManyAssociation.class)) {
                LogFilter.log(LOGGER, LogLevel.WARNING, "Redundant annotations used: @BelongsTo and @HasMany on a "
                    + "relationship between Model {} and Model {}.", modelClass.getName(), childClass.getName());
                return;
            }

            Association hasMany = new OneToManyAssociation(modelClass, childClass, foreignKeyName);
            Association belongsTo = new BelongsToAssociation(childClass, modelClass, foreignKeyName);

            metaModels.getMetaModel(modelClass).addAssociation(hasMany);
            metaModels.getMetaModel(childClass).addAssociation(belongsTo);
        }
    }

    private Map<String, ColumnMetadata> getColumns(ResultSet rs, String dbType) throws SQLException {
        Map<String, ColumnMetadata> columns = new CaseInsensitiveMap<>();
        while (rs.next()) {
            // skip h2 INFORMATION_SCHEMA table columns.
            if (!"h2".equalsIgnoreCase(dbType) || !"INFORMATION_SCHEMA".equals(rs.getString("TABLE_SCHEM"))) {
                ColumnMetadata cm = new ColumnMetadata(rs.getString("COLUMN_NAME"), rs.getString("TYPE_NAME"), rs.getInt("COLUMN_SIZE"));
                columns.put(cm.getColumnName(), cm);
            }
        }
        return columns;
    }

    private void discoverAssociationsFor(String source, String dbName) {
        discoverOne2ManyAssociationsFor(source, dbName);
        discoverMany2ManyAssociationsFor(source, dbName);
    }

    private void discoverMany2ManyAssociationsFor(String source, String dbName) {
        for (String potentialJoinTable : metaModels.getTableNames(dbName)) {
            String target = Inflector.getOtherName(source, potentialJoinTable);
            if (target != null && getMetaModel(target) != null && hasForeignKeys(potentialJoinTable, source, target)) {
                Class<? extends Model> sourceModelClass = metaModels.getModelClass(source);
                Class<? extends Model> targetModelClass = metaModels.getModelClass(target);
                Association associationSource = new Many2ManyAssociation(sourceModelClass, targetModelClass, potentialJoinTable, getMetaModel(source).getFKName(), getMetaModel(target).getFKName());
                getMetaModel(source).addAssociation(associationSource);
            }
        }
    }

    /**
     * Checks that the "join" table has foreign keys from "source" and "other" tables. Returns true
     * if "join" table exists and contains foreign keys of "source" and "other" tables, false otherwise.
     *
     * @param join   - potential name of a join table.
     * @param source name of a "source" table
     * @param other  name of "other" table.
     * @return true if "join" table exists and contains foreign keys of "source" and "other" tables, false otherwise.
     */
    private boolean hasForeignKeys(String join, String source, String other) {
        String sourceFKName = getMetaModel(source).getFKName();
        String otherFKName = getMetaModel(other).getFKName();
        MetaModel joinMM = getMetaModel(join);
        return joinMM.hasAttribute(sourceFKName) && joinMM.hasAttribute(otherFKName);
    }


    /**
     * Discover many to many associations.
     *
     * @param source name of table for which associations are searched.
     */
    private void discoverOne2ManyAssociationsFor(String source, String dbName) {

        MetaModel sourceMM = getMetaModel(source);

        for (String target : metaModels.getTableNames(dbName)) {
            MetaModel targetMM = getMetaModel(target);
            String sourceFKName = getMetaModel(source).getFKName();
            if (targetMM != sourceMM && targetMM.hasAttribute(sourceFKName)) {
                Class<? extends Model> sourceModelClass = metaModels.getModelClass(source);
                Class<? extends Model> targetModelClass = metaModels.getModelClass(target);
                targetMM.addAssociation(new BelongsToAssociation(targetModelClass, sourceModelClass, sourceFKName));
                sourceMM.addAssociation(new OneToManyAssociation(sourceModelClass, targetModelClass, sourceFKName));
            }
        }
    }



    /**
     * Returns model class for a table name, null if not found.
     *
     * @param table table name
     * @param suppressException true to suppress exception
     * @return model class for a table name, null if not found.s
     */
    protected Class<? extends Model> getModelClass(String table, boolean suppressException) {
        Class<? extends Model> modelClass = metaModels.getModelClass(table);
        if(modelClass == null && !suppressException){
            throw new InitException("failed to locate meta model for: " + table + ", are you sure this is correct table name?");
        }else{
            return modelClass;
        }
    }

    protected String getTableName(Class<? extends Model> modelClass) {

        init(MetaModel.getDbName(modelClass));
        String tableName = metaModels.getTableName(modelClass);
        if (tableName == null) {
            throw new DBException("failed to find metamodel for " + modelClass + ". Are you sure that a corresponding table  exists in DB?");
        }
        return tableName;
    }

    /**
     * Returns edges for a join. An edge is a table in a many to many relationship that is not a join.
     *
     * We have to go through all the associations here because join tables, (even if the model exists) will not
     * have associations to the edges.
     *
     * @param join name of join table;
     * @return edges for a join
     */
    protected List<String> getEdges(String join) {
        return metaModels.getEdges(join);
    }

    private void registerColumnMetadata(String table, Map<String, ColumnMetadata> metaParams) {
        metaModels.setColumnMetadata(table, metaParams);
    }

    public String getModelFile() {
        return modelFile;
    }

    /**
     * Used to override the default model file, activejdbc_models.properties.
     * Please note: After initial registration of the model classes in ActiveJDBC this method
     * will not function. That means in order to utilize this method, you must call it before
     * doing any work with models.
     *
     * Usage of this method is only advised if you know what you're doing, and understand the risks
     * of improperly using the method.
     *
     * @param modelFile The name of the file to use as your models properties file.
     */
    public void setModelFile(String modelFile) {
        this.modelFile = modelFile;
    }
}