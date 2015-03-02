/*
Copyright 2009-2015 Igor Polevoy

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;

import static org.javalite.activejdbc.LogFilter.*;
import static org.javalite.common.Inflector.*;
import static org.javalite.common.Util.*;


public class MetaModel implements Serializable {
    private final static Logger logger = LoggerFactory.getLogger(MetaModel.class);

    private SortedMap<String, ColumnMetadata> columnMetadata;
    private final List<Association> associations = new ArrayList<Association>();
    private final String idName;
    private final String tableName, dbType, dbName;
    private final Class<? extends Model> modelClass;
    private final boolean cached;
    private final String idGeneratorCode;
    private SortedSet<String> attributeNamesNoId;
    private final String versionColumn;

    protected MetaModel(String dbName, Class<? extends Model> modelClass, String dbType) {
        this.modelClass = modelClass;
        this.idName = findIdName(modelClass);
        this.tableName = findTableName(modelClass);
        this.dbType = dbType;
        this.cached = isCached(modelClass);
        this.dbName = dbName;
        this.idGeneratorCode = findIdGeneratorCode(modelClass);
        this.versionColumn = findVersionColumn(modelClass);
    }

    private boolean isCached(Class<? extends Model> modelClass) {
        return null != modelClass.getAnnotation(Cached.class);
    }

    private String findIdName(Class<? extends Model> modelClass) {
        IdName idNameAnnotation = modelClass.getAnnotation(IdName.class);
        return idNameAnnotation == null ? "id" : idNameAnnotation.value();
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

    public String getTableName() {
        return tableName;
    }

    void setColumnMetadata(SortedMap<String, ColumnMetadata> columnMetadata){
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
    public SortedSet<String> getAttributeNamesSkipId() {
        if (attributeNamesNoId == null) {//no one cares about unfortunate multi-threading timing with 2 instances created
            //if someone does, use DCL with volatile
            SortedSet<String> attrs = new TreeSet<String>(getAttributeNames());
            attrs.remove(getIdName());
            attributeNamesNoId = attrs;
        }
        return attributeNamesNoId;
    }

    /**
     * Convenience method. Calls {@link #getAttributeNamesSkipGenerated(boolean)} and passes <code>true</code> as argument.
     *
     * @return list of all attributes except id, created_at, updated_at and record_version.
     */
    public SortedSet<String> getAttributeNamesSkipGenerated() {
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
    public SortedSet<String> getAttributeNamesSkipGenerated(boolean managed) {
        //TODO: can cache this, but will need a cache for managed=true an another for managed=false
        SortedSet<String> attributes = new TreeSet(getAttributeNamesSkipId());

        if(managed){
            attributes.remove("created_at");
            attributes.remove("updated_at");
        }

        attributes.remove(versionColumn);
        return attributes;
    }


    /**
     * Finds all attribute names except those provided as arguments.
     * @return list of all attributes except those provided as arguments.
     */
    public SortedSet<String> getAttributeNamesSkip(String ... names) {
        SortedSet<String> attributes = new TreeSet<String>(getAttributeNames());
        for(String name:names){
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
    protected SortedSet<String> getAttributeNames() {
        if(columnMetadata == null || columnMetadata.isEmpty()) throw new InitException("Failed to find table: " + getTableName());
        return Collections.unmodifiableSortedSet((SortedSet<String>) columnMetadata.keySet());
    }

    public String getIdName() {
        return idName;
    }

    /**
     * Returns association of this table with the target table. Will return null if there is no association.
     * 
     * @param target association of this table and the target table.
     * @param associationClass class of association in requested.
     * @return association of this table with the target table. Will return null if there is no association with target
     * table and specified type.
     */
    public <A extends Association> A getAssociationForTarget(String target, Class<A> associationClass){
        Association result = null;
        for (Association association : associations) {
            if (association.getClass().equals(associationClass) && association.getTarget().equalsIgnoreCase(target)) {
                result = association; break;
            }
        }
        return (A) result;
    }


    /**
     * Returns association of this table with the target table. Will return null if there is no association.
     *
     * @param target association of this table and the target table.
     * @return association of this table with the target table. Will return null if there is no association with target
     * table and specified type.
     */
    public <A extends Association> A getAssociationForTarget(String target){
        Association result = null;
        for (Association association : associations) {
            if (association.getTarget().equalsIgnoreCase(target)) {
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
     * @param target association of this table and the target table.
     * @return list of associations of this table with the target table. Will return empty list if none found.
     * table and specified type.
     */
    public List<Association> getAssociationsForTarget(String target) {
        List<Association> result = new ArrayList<Association>();

        for (Association association : associations) {
            if (association.getTarget().equalsIgnoreCase(target)) {
                result.add(association);
            }
        }
        return result;
    }

    protected void addAssociation(Association association) {
        if (!associations.contains(association)) {
            log(logger, "Association found: {}", association);
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
        return columnMetadata != null && columnMetadata.containsKey(attribute);
    }

    protected boolean hasAssociation(String table, Class<? extends Association> associationClass){
        for (Association association : associations) {
            if(association.getTarget().equalsIgnoreCase(table) &&
                    association.getClass().equals(associationClass)) return true;
        }
        return false;
    }

    @Override
    public String toString() {
        final StringBuilder t = new StringBuilder();
        t.append("MetaModel: ").append(tableName).append(", ").append(modelClass).append("\n");
        if(columnMetadata != null){
            for (String key : columnMetadata.keySet()) {
                t.append(columnMetadata.get(key)).append(", ");
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
        List<OneToManyAssociation> one2Manies = new ArrayList<OneToManyAssociation>();
        for (Association association : associations) {
            if(association.getClass().equals(OneToManyAssociation.class) && !exclusions.contains(association)){
                one2Manies.add((OneToManyAssociation)association);
            }
        }
        return one2Manies;
    }

    protected List<OneToManyPolymorphicAssociation>  getPolymorphicAssociations(List<Association> exclusions) {
        List<OneToManyPolymorphicAssociation> one2Manies = new ArrayList<OneToManyPolymorphicAssociation>();
        for (Association association : associations) {
            if(association.getClass().equals(OneToManyPolymorphicAssociation.class) && !exclusions.contains(association)){
                one2Manies.add((OneToManyPolymorphicAssociation)association);
            }
        }
        return one2Manies;
    }

    protected List<Many2ManyAssociation>  getManyToManyAssociations(List<Association> excludedAssociations) {
        List<Many2ManyAssociation> many2Manies = new ArrayList<Many2ManyAssociation>();
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
     * Checks if this model has a named attribute or association whose target has the same name as argument.
     * Throws <code>IllegalArgumentException</code> in case it does not find either one.
     *
     * @param attributeOrAssociation name  of attribute or association target.
     */
    protected void checkAttributeOrAssociation(String attributeOrAssociation) {
        if (!hasAttribute(attributeOrAssociation)) {
            boolean contains = false;
            for (Association association : associations) {
                if (association.getTarget().equalsIgnoreCase(attributeOrAssociation)) {
                    contains = true;
                    break;
                }
            }
            if (!contains) {
                StringBuilder sb = new StringBuilder().append("Attribute: '").append(attributeOrAssociation)
                        .append("' is not defined in model: '").append(getModelClass())
                        .append("' and also, did not find an association by the same name, available attributes: ")
                        .append(getAttributeNames());
                if (!associations.isEmpty()) {
                    sb.append("\nAvailable associations:\n");
                    join(sb, associations, "\n");
                }
                throw new IllegalArgumentException(sb.toString());
            }
        }
    }

    protected static String getDbName(Class<? extends Model> modelClass) {
        DbName dbNameAnnotation = modelClass.getAnnotation(DbName.class);
        return dbNameAnnotation == null ? Base.DEFAULT_DB_NAME : dbNameAnnotation.value();
    }

    /**
     * Provides column metadata map, keyed by attribute names.
     * Table columns correspond to ActiveJDBC model attributes.
     *
     * @return Provides column metadata map, keyed by attribute names.
     */
    public SortedMap<String, ColumnMetadata> getColumnMetadata() {
        if(columnMetadata == null || columnMetadata.isEmpty()) throw new InitException("Failed to find table: " + getTableName());
        return Collections.unmodifiableSortedMap(columnMetadata);
    }


    /**
     * Checks if there is association to the target model class.,
     *
     * @param targetModelClass class of a model that will be checked for association from current model.
     * @return true if any association exists such that the current model is a source and targetModelClass is a target.
     */
    public boolean isAssociatedTo(Class<? extends Model> targetModelClass) {
        for (Association association : associations) {
            Class targetClass = Registry.instance().getModelClass(association.getTarget(), true);
            if (targetClass != null && targetClass.equals(targetModelClass)) {
                return true;
            }
        }
        return false;
    }
}
