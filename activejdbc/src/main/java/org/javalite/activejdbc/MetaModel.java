/*
Copyright 2009-2010 Igor Polevoy 

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

    protected MetaModel(String dbName, String tableName, String idName, Class<T> modelClass, String dbType, boolean cached, String idGeneratorCode) {
        this.idName = idName;
        this.tableName = tableName;
        this.modelClass = modelClass;
        this.dbType = dbType;
        this.cached = cached;
        this.dbName = dbName;
        this.idGeneratorCode = idGeneratorCode;
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
        if(attributeNamesNoId == null){
            attributeNamesNoId = getAttributeNames();
            attributeNamesNoId.remove(getIdName());
        }
        return attributeNamesNoId;
    }

    /**
     * Finds all attribute names except generated like id, created_at, updated_at and record_version.
     * @return list of all attributes except id, created_at, updated_at and record_version. 
     */
    public List<String> getAttributeNamesSkipGenerated() {
        //TODO: can cache this
        List<String> attributes = getAttributeNames();
        attributes.remove(getIdName());
        attributes.remove("created_at");
        attributes.remove("updated_at");
        attributes.remove("record_version");
        return attributes;
    }


    /**
     * Finds all attribute names except those provided as arguments.
     * @return list of all attributes except those provided as arguments. 
     */
    public List<String> getAttributeNamesSkip(String ... names) {
        List<String> attributes = getAttributeNames();
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
        List<String> attrs = getAttributeNames(); 
        return attrs.contains("record_version") || attrs.contains("RECORD_VERSION");
    }

    /**
     * Retrieves all attribute names.
     *
     * @return all attribute names.
     */
    protected List<String> getAttributeNames() {
        ArrayList<String> keysList = new ArrayList<String>();

        if(columnMetadata == null || columnMetadata.size() == 0) throw new InitException("Failed to find table: " + getTableName());

        for (String key : columnMetadata.keySet()) {
            keysList.add(key);
        }
        return keysList;
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
            log(logger, "Association found: " + association);
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
        return singularize(getTableName()) + "_id";
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

        List<Association> associations = getAssociations();
        List<String> associationTargets = new ArrayList<String>();

        String message = "\n";
        for(Association association: associations){
            message += association + "\n";
            associationTargets.add(association.getTarget());
        }

        //REMOVE QUOTES - this is a TEMP SOLUTION JUST FOR PostgreSQL
        if(attributeOrAssociation.startsWith("\"") && attributeOrAssociation.endsWith("\"")){
            //WARNING: SIDE EFFECT!!
            attributeOrAssociation = attributeOrAssociation.substring(1, attributeOrAssociation.length() - 1);
        }


        if (!hasAttribute(attributeOrAssociation) && !associationTargets.contains(attributeOrAssociation)) {
            throw new IllegalArgumentException("Attribute: '" + attributeOrAssociation + "' is not defined in model: '"
                    + getModelClass() + "' and also, did not find an association by the same name, available attributes: "
                    + getAttributeNames() + "\nAvailable associations: " + message);
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
        boolean associated = false;
        for (Association association : associations) {
            Class targetClass = null;

            targetClass = Registry.instance().getModelClass(association.getTarget(), true);

            if (targetClass != null && targetClass.equals(targetModelClass)) {
                associated = true;
            }
        }
        return associated;
    }
}
