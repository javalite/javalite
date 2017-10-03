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

import org.javalite.activejdbc.annotations.*;
import org.javalite.activejdbc.associations.*;
import org.javalite.activejdbc.dialects.Dialect;
import org.javalite.activejdbc.logging.LogFilter;
import org.javalite.activejdbc.logging.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;

import static org.javalite.common.Inflector.*;


public class MetaModel implements Serializable {
    private static final Logger LOGGER = LoggerFactory.getLogger(MetaModel.class);
    private static final ThreadLocal<HashMap<Class, String>> shardingTableNamesTL = new ThreadLocal<>();

    private Map<String, ColumnMetadata> columnMetadata;
    private final List<Association> associations = new ArrayList<>();
    private final String idName;
    private final String[] compositeKeys;
    private final String tableName, dbType, dbName;
    private final Class<? extends Model> modelClass;
    private final boolean cached;
    private final String idGeneratorCode;
    private Set<String> attributeNamesNoId;
    private final String versionColumn;
    private String[] partitionIDs = null;

    protected MetaModel(String dbName, Class<? extends Model> modelClass, String dbType) {
        this.modelClass = modelClass;
        this.idName = findIdName(modelClass);
        this.compositeKeys = findCompositeKeys(modelClass);
        this.tableName = findTableName(modelClass);
        this.dbType = dbType;
        this.cached = isCached(modelClass);
        this.dbName = dbName;
        this.idGeneratorCode = findIdGeneratorCode(modelClass);
        this.versionColumn = findVersionColumn(modelClass);
        this.partitionIDs = findPartitionIDs();
    }

    private String[] findPartitionIDs() {
        PartitionIDs partitionIDs = modelClass.getAnnotation(PartitionIDs.class);
        return partitionIDs != null ? partitionIDs.value() : null;
    }

    public boolean hasPartitionIDs(){
        return partitionIDs != null;
    }

    public String[] getPartitionIDs(){
        return partitionIDs;
    }



    static Map<Class,String> getTableNamesMap(){
        if (shardingTableNamesTL.get() == null)
            shardingTableNamesTL.set(new HashMap<Class, String>());
        return shardingTableNamesTL.get();
    }

    /**
     *
     *
     * <p>
     *      <strong>This feature is for sharding!</strong>
     *      <br>
     *      Do not use it to set table names <em>willy-nilly</em>!
     * </p>
     *
     * <p>
     *      Sets a table name for this model. The table name is attached to a current thread and will remain there
     *      until it is set with a different value or cleared with {@link #clearShardTableName()} method.
     *      Table name set with this method overrides a table name naturally mapped to this model.
     *</p>
     * <p>
     *      Method {@link #getTableName()} will return this value for all operations related to this table.
     * </p>
     *
     * @param tableName name of a table this model will read from current thread.
     */
    public void setShardTableName(String tableName){
        getTableNamesMap().put(modelClass, tableName);
    }

    /**
     * Clears sharding name of table attached to current thread.
     * The name was supposedly attached by the {@link #setShardTableName(String)}
     * method. After execution of this class, the method {@link #getTableName()} will be
     * returning the value this {@link MetaModel} was initialized with during teh bootstrap phase.
     */
    public void clearShardTableName(){
        getTableNamesMap().remove(modelClass);
    }

    private boolean isCached(Class<? extends Model> modelClass) {
        return null != modelClass.getAnnotation(Cached.class);
    }

    private String findIdName(Class<? extends Model> modelClass) {
        IdName idNameAnnotation = modelClass.getAnnotation(IdName.class);
        return idNameAnnotation == null ? "id" : idNameAnnotation.value();
    }

    private String[] findCompositeKeys(Class<? extends Model> modelClass) {
    	CompositePK compositeKeysAnnotation = modelClass.getAnnotation(CompositePK.class);
        return compositeKeysAnnotation == null ? null : compositeKeysAnnotation.value();
    }
    
    private String findTableName(Class<? extends Model> modelClass) {
        Table tableAnnotation = modelClass.getAnnotation(Table.class);
        return tableAnnotation == null ? tableize(modelClass.getSimpleName()) : tableAnnotation.value();
    }

    private String findIdGeneratorCode(Class<? extends Model> modelClass) {
        IdGenerator idGenerator = modelClass.getAnnotation(IdGenerator.class);
        return idGenerator == null ? null : idGenerator.value();
    }

    private String findVersionColumn(Class<? extends Model> modelClass) {
        VersionColumn vc = modelClass.getAnnotation(VersionColumn.class);
        return vc == null ? "record_version" : vc.value();
    }

    /**
     * @return name of the column for optimistic locking record version
     */
    public String getVersionColumn(){
        return versionColumn;
    }

    public String getIdGeneratorCode(){
        return idGeneratorCode;
    }

    public String getDbName() {
        return dbName;
    }

    public boolean cached(){
        return cached;
    }

    public Class<? extends Model> getModelClass(){
        return modelClass;
    }

    /**
     * Returns table name currently associated with this model.
     * Table name can be modified for sharding using {@link #setShardTableName(String)}
     *
     * @return table name currently associated with this model.
     */
    public String getTableName() {
        if(getTableNamesMap().containsKey(modelClass)){
            return getTableNamesMap().get(modelClass);
        }else{
            return tableName;
        }
    }

    void setColumnMetadata(Map<String, ColumnMetadata> columnMetadata){
        this.columnMetadata = columnMetadata;
    }

    protected boolean tableExists(){
        return columnMetadata != null &&  columnMetadata.isEmpty();
    }


    /**
     * Finds all attribute names except for id.
     *
     * @return all attribute names except for id.
     */
    public Set<String> getAttributeNamesSkipId() {
        if (attributeNamesNoId == null) {//no one cares about unfortunate multi-threading timing with 2 instances created
            //if someone does, use DCL with volatile
            Set<String> attributesNames = new CaseInsensitiveSet(getAttributeNames());
            attributesNames.remove(getIdName());
            attributeNamesNoId = attributesNames;
        }
        return attributeNamesNoId;
    }

    /**
     * Convenience method. Calls {@link #getAttributeNamesSkipGenerated(boolean)} and passes <code>true</code> as argument.
     *
     * @return list of all attributes except id, created_at, updated_at and record_version.
     */
    public Set<String> getAttributeNamesSkipGenerated() {
        return getAttributeNamesSkipGenerated(true);
    }

    /**
     * Finds all attribute names except managed like <code>id</code>,
     * <code>created_at</code>, <code>updated_at</code> and <code>record_version</code>, depending on argument.
     *
     * @param managed if true, time managed attributes <code>created_at</code> and <code>updated_at</code> will not be included (they are managed automatically).
     *                If false (not managed) <code>created_at</code> and <code>updated_at</code> will be included in output.
     * @return list of all attributes except <code>id</code>, <code>created_at</code>, <code>updated_at</code> and
     * <code>record_version</code>, depending on argument.
     */
    public Set<String> getAttributeNamesSkipGenerated(boolean managed) {
        //TODO: can cache this, but will need a cache for managed=true an another for managed=false
        Set<String> attributesNames = new CaseInsensitiveSet(getAttributeNamesSkipId());

        if(managed){
            attributesNames.remove("created_at");
            attributesNames.remove("updated_at");
        }

        attributesNames.remove(versionColumn);
        return attributesNames;
    }


    /**
     * Finds all attribute names except those provided as arguments.
     * @return list of all attributes except those provided as arguments.
     */
    public Set<String> getAttributeNamesSkip(String ... names) {
        Set<String> attributes = new CaseInsensitiveSet(getAttributeNames());
        for (String name : names) {
            attributes.remove(name);
        }
        return attributes;
    }

    /**
     * Returns true if this model supports optimistic locking, false if not
     *
     * @return true if this model supports optimistic locking, false if not
     */
    public boolean isVersioned(){
        return columnMetadata != null && columnMetadata.containsKey(versionColumn);
    }

    /**
     * Retrieves all attribute names.
     *
     * @return all attribute names.
     */
    protected Set<String> getAttributeNames() {
        if(columnMetadata == null || columnMetadata.isEmpty()) throw new InitException("Failed to find table: " + getTableName());
        return Collections.unmodifiableSet(columnMetadata.keySet());
    }

    public String getIdName() {
        return idName;
    }

	/**
	 * Returns optional composite primary key class
	 * 
	 * @return composite primary key class
	 */
	public String[] getCompositeKeys() {
		return compositeKeys;
	}
    
    /**
     * Returns association of this table with the target table. Will return null if there is no association.
     *
     * @param targetModelClass association of this model and the target model.
     * @param associationClass class of association in requested.
     * @return association of this table with the target table. Will return null if there is no association with target
     * table and specified type.
     */
    public <A extends Association> A getAssociationForTarget(Class<? extends Model> targetModelClass, Class<A> associationClass){
        Association result = null;
        for (Association association : associations) {
            if (association.getClass().equals(associationClass) && association.getTargetClass().equals(targetModelClass)) {
                result = association; break;
            }
        }
        return (A) result;
    }


    /**
     * Returns association of this table with the target table. Will return null if there is no association.
     *
     * @param targetClass association of this model and the target model.
     * @return association of this table with the target table. Will return null if there is no association with target
     * table and specified type.
     */
    public <A extends Association> A getAssociationForTarget(Class<? extends Model> targetClass){
        Association result = null;
        for (Association association : associations) {
            if (association.getTargetClass().equals(targetClass)) {
                result = association; break;
            }
        }
        return (A) result;
    }


    /**
     * Returns associations of this table with the target table. It is possible
     * to have more than one association to a target table if a target table is the same as source. Usually this
     * happens when tree structures are stored in the same table (category has many categories).
     *
     * @param targetModelClass association of this model and the target model.
     * @return list of associations of this table with the target table. Will return empty list if none found.
     * table and specified type.
     */
    public List<Association> getAssociationsForTarget(Class<? extends Model> targetModelClass) {
        List<Association> result = new ArrayList<>();

        for (Association association : associations) {
            if (association.getTargetClass().equals(targetModelClass)) {
                result.add(association);
            }
        }
        return result;
    }

    protected void addAssociation(Association association) {
        if (!associations.contains(association)) {
            LogFilter.log(LOGGER, LogLevel.INFO, "Association found: {}", association);
            associations.add(association);
        }
    }

    /**
     * returns true if this attribute is present in this meta model. This method i case insensitive.
     *
     * @param attribute attribute name, case insensitive.
     * @return true if this attribute is present in this meta model, false of not.
     */
    boolean hasAttribute(String attribute) {
        if(columnMetadata != null){
            if(columnMetadata.containsKey(attribute)){
                return true;
            }else if(attribute.startsWith("\"") && attribute.endsWith("\"")){
                return columnMetadata.containsKey(attribute.substring(1, attribute.length() - 1));
            }
        }
        return false;
    }

    protected boolean hasAssociation(Class<? extends Model> targetClass, Class<? extends Association> associationClass){
        for (Association association : associations) {
            if(association.getTargetClass().equals(targetClass) &&
                    association.getClass().equals(associationClass)) return true;
        }
        return false;
    }

    @Override
    public String toString() {
        final StringBuilder t = new StringBuilder();
        t.append("MetaModel: ").append(tableName).append(", ").append(modelClass).append("\n");
        if(columnMetadata != null){
            for (Entry<String, ColumnMetadata> metadata : columnMetadata.entrySet()) {
                t.append(metadata.getValue()).append(", ");
            }
        }

        return t.toString();
    }

    /**
     * FK name is a foreign key name used in relationships as a foreign key column in a child table (table represented by this
     * instance is a parent table).
     * The FK name is derived using {@link org.javalite.common.Inflector}: It is a singular version of this table name plus "_id".
     *
     * @return foreign key name used in relationships as a foreign key column in a child table.
     */
    public String getFKName() {
        return singularize(getTableName()).toLowerCase() + "_id";
    }

    protected List<OneToManyAssociation> getOneToManyAssociations(List<Association> exclusions) {
        List<OneToManyAssociation> one2Manies = new ArrayList<>();
        for (Association association : associations) {
            if(association.getClass().equals(OneToManyAssociation.class) && !exclusions.contains(association)){
                one2Manies.add((OneToManyAssociation)association);
            }
        }
        return one2Manies;
    }

    protected List<OneToManyPolymorphicAssociation>  getPolymorphicAssociations(List<Association> exclusions) {
        List<OneToManyPolymorphicAssociation> one2Manies = new ArrayList<>();
        for (Association association : associations) {
            if(association.getClass().equals(OneToManyPolymorphicAssociation.class) && !exclusions.contains(association)){
                one2Manies.add((OneToManyPolymorphicAssociation)association);
            }
        }
        return one2Manies;
    }

    protected List<Many2ManyAssociation>  getManyToManyAssociations(List<Association> excludedAssociations) {
        List<Many2ManyAssociation> many2Manies = new ArrayList<>();
        for (Association association : associations) {
            if(association.getClass().equals(Many2ManyAssociation.class) && !excludedAssociations.contains(association)){
                many2Manies .add((Many2ManyAssociation)association);
            }
        }
        return many2Manies ;
    }

    public String getDbType(){
        return dbType;
    }

    public Dialect getDialect() {
        return Registry.instance().getConfiguration().getDialect(this);
    }

    protected List<Association> getAssociations(){
        return Collections.unmodifiableList(associations);
    }

    /**
     * Checks if this model has a named attribute that has the same name as argument.
     *
     * Throws <code>IllegalArgumentException</code> in case it does not find it.
     *
     * @param attribute name  of attribute or association target.
     */
    protected void checkAttribute(String attribute) {
        if (!hasAttribute(attribute)) {
            String sb = "Attribute: '" + attribute + "' is not defined in model: '" + getModelClass() + ". "
                    + "Available attributes: " +getAttributeNames();
            throw new IllegalArgumentException(sb);
        }
    }

    protected static String getDbName(Class<? extends Model> modelClass) {
        DbName dbNameAnnotation = modelClass.getAnnotation(DbName.class);
        return dbNameAnnotation == null ? DB.DEFAULT_NAME : dbNameAnnotation.value();
    }

    /**
     * Provides column metadata map, keyed by attribute names.
     * Table columns correspond to ActiveJDBC model attributes.
     *
     * @return Provides column metadata map, keyed by attribute names.
     */
    public Map<String, ColumnMetadata> getColumnMetadata() {
        if(columnMetadata == null || columnMetadata.isEmpty()) throw new InitException("Failed to find table: " + getTableName());
        return Collections.unmodifiableMap(columnMetadata);
    }


    /**
     * Checks if there is association to the target model class.,
     *
     * @param targetModelClass class of a model that will be checked for association from current model.
     * @return true if any association exists such that the current model is a source and targetModelClass is a target.
     */
    public boolean isAssociatedTo(Class<? extends Model> targetModelClass) {

        if(targetModelClass == null){
            throw  new NullPointerException();
        }

        for (Association association : associations) {
            if (association.getTargetClass().equals(targetModelClass)) {
                return true;
            }
        }
        return false;
    }

    public void removeAssociationForTarget(Class<? extends Model> modelClass) {
        Association association = getAssociationForTarget(modelClass);
        if(association != null){
            associations.remove(association);
        }
    }
}
