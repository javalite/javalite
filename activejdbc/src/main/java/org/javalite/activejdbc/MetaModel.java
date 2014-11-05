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

import org.javalite.activejdbc.annotations.DbName;
import org.javalite.activejdbc.annotations.VersionColumn;
import org.javalite.activejdbc.associations.Many2ManyAssociation;
import org.javalite.activejdbc.associations.OneToManyAssociation;
import org.javalite.activejdbc.associations.OneToManyPolymorphicAssociation;
import org.javalite.activejdbc.dialects.DefaultDialect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;

import static org.javalite.common.Inflector.*;
import static org.javalite.activejdbc.LogFilter.*;
import static org.javalite.common.Util.*;

public class MetaModel<T extends Model, E extends Association> implements Serializable {

    private final static Logger logger = LoggerFactory.getLogger(MetaModel.class);
    private Map<String, ColumnMetadata> columnMetadata;
    private List<Association> associations = new ArrayList<Association>();
    private String idName;
    private String tableName, dbType, dbName;
    private Class<T> modelClass;
    private boolean cached;
    private String idGeneratorCode;
    private List<String> attributeNamesNoId;
    private String versionColumn;


    protected MetaModel(String dbName, String tableName, String idName, Class<T> modelClass, String dbType, boolean cached, String idGeneratorCode) {
        this.idName = idName.toLowerCase();
        this.tableName = tableName;
        this.modelClass = modelClass;
        this.dbType = dbType;
        this.cached = cached;
        this.dbName = dbName;
        this.idGeneratorCode = idGeneratorCode;
        VersionColumn vc = modelClass.getAnnotation(VersionColumn.class);
        if(vc != null){
            versionColumn = vc.value();
        }else{
            versionColumn = "record_version";
        }
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

    public Class<T> getModelClass(){
        return modelClass;
    }

    public String getTableName() {
        return tableName;
    }

    void setColumnMetadata(Map<String, ColumnMetadata> columnMetadata){
        this.columnMetadata = columnMetadata;
    }

    protected boolean tableExists(){
        return columnMetadata != null &&  columnMetadata.size() > 0;
    }


    /**
     * Finds all attribute names except for id.
     *
     * @return all attribute names except for id.
     */
    public List<String> getAttributeNamesSkipId() {
        if (attributeNamesNoId == null) {//no one cares about unfortunate multi-threading timing with 2 instances created
            //if someone does, use DCL with volatile
            List<String> attrs = getAttributeNames();
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
    public List<String> getAttributeNamesSkipGenerated() {
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
    public List<String> getAttributeNamesSkipGenerated(boolean managed) {
        //TODO: can cache this
        List<String> attributes = getAttributeNames();
        attributes.remove(getIdName().toLowerCase());

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
    public List<String> getAttributeNamesSkip(String ... names) {
        List<String> attributes = getAttributeNames();
        for(String name:names){
            attributes.remove(name.toLowerCase());
        }
        return attributes;
    }

    /**
     * Returns true if this model supports optimistic locking, false if not
     *
     * @return true if this model supports optimistic locking, false if not
     */
    public boolean isVersioned(){
        List<String> attrs = getAttributeNames();
        return attrs.contains(versionColumn) || attrs.contains(versionColumn.toUpperCase());
    }

    /**
     * Retrieves all attribute names.
     *
     * @return all attribute names.
     */
    protected List<String> getAttributeNames() {
        if(columnMetadata == null || columnMetadata.size() == 0) throw new InitException("Failed to find table: " + getTableName());

        return new ArrayList<String>(columnMetadata.keySet());
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
    public E getAssociationForTarget(String target, Class<? extends Association> associationClass){

        Association result = null;
        for (Association association : associations) {
            if (association.getTarget().equalsIgnoreCase(target) && association.getClass().equals(associationClass)) {
                result = association; break;
            }
        }
        return (E) result;
    }


    /**
     * Returns association of this table with the target table. Will return null if there is no association.
     *
     * @param target association of this table and the target table.
     * @return association of this table with the target table. Will return null if there is no association with target
     * table and specified type.
     */
    public E getAssociationForTarget(String target){
        Association result = null;
        for (Association association : associations) {
            if (association.getTarget().equalsIgnoreCase(target)) {
                result = association; break;
            }
        }
        return (E) result;
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
    public ArrayList<Association> getAssociationsForTarget(String target){
        ArrayList<Association> result = new ArrayList<Association>();

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
        return columnMetadata != null && columnMetadata.containsKey(attribute.toLowerCase());
    }

    protected boolean hasAssociation(String table, Class<? extends Association> associationClass){
        for (Association association : associations) {
            if(association.getTarget().equalsIgnoreCase(table) &&
                    association.getClass().equals(associationClass)) return true;
        }
        return false;
    }

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

    protected List<OneToManyAssociation>  getOneToManyAssociations(List<Association> exclusions) {
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

    public DefaultDialect getDialect() {
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
            List<Association> associations = getAssociations();
            List<String> associationTargets = new ArrayList<String>();
            for (Association association : associations) {
                associationTargets.add(association.getTarget());
            }
            if (!associationTargets.contains(attributeOrAssociation)) {
                StringBuilder sb = new StringBuilder().append("Attribute: '").append(attributeOrAssociation)
                        .append("' is not defined in model: '").append(getModelClass())
                        .append("' and also, did not find an association by the same name, available attributes: ")
                        .append(getAttributeNames());
                if (!associations.isEmpty()) {
                    sb.append("\nAvailable associations:\n");
                    join(sb, getAssociations(), "\n");
                }
                throw new IllegalArgumentException(sb.toString());
            }
        }
    }

    protected static String getDbName(Class<? extends Model> modelClass) {
        DbName dbNameAnnotation = modelClass.getAnnotation(DbName.class);
        return dbNameAnnotation == null ? "default" : dbNameAnnotation.value();
    }

    /**
     * Provides column metadata map, keyed by attribute names.
     * Table columns correspond to ActiveJDBC model attributes.
     *
     * @return Provides column metadata map, keyed by attribute names.
     */
    public Map<String, ColumnMetadata> getColumnMetadata() {
        return Collections.unmodifiableMap(columnMetadata);
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
