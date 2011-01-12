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


package activejdbc;

import activejdbc.annotations.DbName;
import activejdbc.associations.Many2ManyAssociation;
import activejdbc.associations.OneToManyAssociation;
import activejdbc.associations.OneToManyPolymorphicAssociation;
import activejdbc.dialects.DefaultDialect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;

import static javalite.common.Inflector.*;
import static activejdbc.LogFilter.*;

public class MetaModel<T extends Model, E extends Association> implements Serializable {

    private final static Logger logger = LoggerFactory.getLogger(MetaModel.class);
    private Map<String, ColumnMetadata> columnMetadata;
    private List<Association> associations = new ArrayList<Association>();
    private String idName, idNameUpper, idNameLower;
    private String tableName, dbType, dbName;
    private Class<T> modelClass;
    private boolean cached;
    private String idGeneratorCode;

    protected MetaModel(String dbName, String tableName, String idName, Class<T> modelClass, String dbType, boolean cached, String idGeneratorCode) {
        this.idName = idName.toLowerCase();
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

    private List<String> attributeNamesNoId;

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
        List<String> attributes = getAttributeNames();
        attributes.remove(getIdName().toLowerCase());
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
            attributes.remove(name.toLowerCase());
        }
        return attributes;
    }

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
     * @return association os this table with the target table. Will return null if there is no association with target
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
     * @return association os this table with the target table. Will return null if there is no association with target
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
        final StringBuffer t = new StringBuffer();
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
     * The FK name is derived using {@link javalite.common.Inflector}: It is a singular version of this table name plus "_id".
     *
     * @return foreign key name used in relationships as a foreign key column in a child table.
     */
    public String getFKName() {
        return singularize(getTableName()).toLowerCase() + "_id";
    }

    protected List<OneToManyAssociation>  getOneToManyAssociations() {
        List<OneToManyAssociation> one2Manies = new ArrayList<OneToManyAssociation>();
        for (Association association : associations) {
            if(association.getClass().equals(OneToManyAssociation.class)){
                one2Manies.add((OneToManyAssociation)association);
            }
        }
        return one2Manies;
    }

    protected List<OneToManyPolymorphicAssociation>  getPolymorphicAssociations() {
        List<OneToManyPolymorphicAssociation> one2Manies = new ArrayList<OneToManyPolymorphicAssociation>();
        for (Association association : associations) {
            if(association.getClass().equals(OneToManyPolymorphicAssociation.class)){
                one2Manies.add((OneToManyPolymorphicAssociation)association);
            }
        }
        return one2Manies;
    }

    protected List<Many2ManyAssociation>  getManyToManyAssociations() {
        List<Many2ManyAssociation> many2Manies = new ArrayList<Many2ManyAssociation>();
        for (Association association : associations) {
            if(association.getClass().equals(Many2ManyAssociation.class)){
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

    protected void checkAttributeOrAssociation(String attribute) {

        List<Association> associations = getAssociations();

        String message = "\n";
        for(Association association: associations){
            message +=association + "\n";
        }

        if (!(getAttributeNames().contains(attribute.toUpperCase()) ||
                getAttributeNames().contains(attribute.toLowerCase())) && !associations.contains(attribute)) {
            List<String> attributeNames = getAttributeNames();
            throw new IllegalArgumentException("Attribute: '" + attribute + "' is not defined in model: '"
                    + getModelClass() + "', available attributes: " + attributeNames + "\nAvailable associations: " + message);
        }
    }

    protected static String getDbName(Class<? extends Model> modelClass) {
        DbName dbNameAnnotation = modelClass.getAnnotation(DbName.class);
        return dbNameAnnotation == null ? "default" : dbNameAnnotation.value();
    }
}
