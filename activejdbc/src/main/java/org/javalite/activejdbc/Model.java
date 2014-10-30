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

import org.javalite.activejdbc.associations.*;
import org.javalite.activejdbc.cache.QueryCache;
import org.javalite.activejdbc.validation.*;
import org.javalite.common.Convert;
import org.javalite.common.Inflector;
import org.javalite.common.XmlEntities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.math.BigDecimal;
import java.sql.Clob;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.javalite.common.Inflector.*;
import static org.javalite.common.Util.*;


/**
 * This class is a super class of all "models" and provides most functionality
 * necessary for implementation of Active Record pattern.
 */
public abstract class Model extends CallbackSupport implements Externalizable {

    private final static Logger logger = LoggerFactory.getLogger(Model.class);
    private Map<String, Object> attributes = new HashMap<String, Object>();
    private boolean frozen = false;
    private MetaModel metaModelLocal;
    private Map<Class, Model> cachedParents = new HashMap<Class, Model>();
    private Map<Class, List<Model>> cachedChildren = new HashMap<Class, List<Model>>();
    private boolean manageTime = true;

    protected Errors errors;

    protected Model() {
        errors = new Errors();
    }

    public static MetaModel getMetaModel() {
        Registry.instance().init(MetaModel.getDbName(getDaClass()));
        return Registry.instance().getMetaModel(getTableName());
    }

    protected Map<String, Object> getAttributes(){
        return attributes;
    }



    /**
     * Overrides attribute values from input map. The input map may have attributes whose name do not match the
     * attribute names (columns) of this model. Such attributes will be ignored. Those values whose names are
     * not present in the argument map, will stay untouched. The input map may have only partial list of attributes.
     *
     * @param input map with attributes to overwrite this models'. Keys are names of attributes of this model, values
     * are new values for it.
     */
    public void fromMap(Map input){

        List<String> attributeNames = getMetaModelLocal().getAttributeNames();

        for (String attrName : attributeNames) {
            Object value = input.get(attrName.toLowerCase());
            if (value == null) {
                value = input.get(attrName.toUpperCase());
            }
            if(input.containsKey(attrName.toLowerCase()) || input.containsKey(attrName.toUpperCase()))
                attributes.put(attrName.toLowerCase(), value);
        }
    }



    /**
     * Hydrates a this instance of model from a map. Only picks values from a map that match
     * this instance's attribute names, while ignoring the others.
     *
     * @param attributesMap map containing values for this instance.
     */
    protected  void hydrate(Map attributesMap) {
        List<String> attributeNames = getMetaModelLocal().getAttributeNamesSkipId();

        String idName = getMetaModelLocal().getIdName();
        Object id = attributesMap.get(idName);

        if(id != null)
            attributes.put(idName, id);

        for (String attrName : attributeNames) {

            if(attrName.equalsIgnoreCase(getMetaModelLocal().getIdName())){
                continue;//skip ID, already set.
            }

            Object value = attributesMap.get(attrName.toLowerCase());
            if (value == null) {
                value = attributesMap.get(attrName.toUpperCase());
            }

            //it is necessary to cache contents of a clob, because if a clob instance itself is cached, and accessed later,
            //it will not be able to connect back to that same connection from which it came.
            //This is only important for cached models. This will allocate a ton of memory if Clobs are large.
            //Should the Blob behavior be the same?
            //TODO: write about this in future tutorial
            if( value instanceof Clob && getMetaModelLocal().cached() ){
                this.attributes.put(attrName.toLowerCase(), Convert.toString(value));
            }else {
        		this.attributes.put(attrName, getMetaModelLocal().getDialect().overrideDriverTypeConversion(getMetaModelLocal(), attrName, value));
            }
        }
    }


    /**
     * Convenience method, sets ID value on this model, equivalent to <code>set(getIdName(), id)</code>.
     *
     * @param id value of ID
     * @return reference to self for chaining.
     */
    public <T extends Model> T setId(Object id){
        set(getIdName(), id);
        return (T) this;
    }

    /**
     * Converts to <code>java.sql.Date</code>. Expects a <code>java.sql.Date</code>,
     * <code>java.sql.Timestamp</code>, <code>java.sql.Time</code>, <code>java.util.Date</code> or
     * any object with string with format: <code>yyyy-mm-dd</code>.
     *
     * @param attribute name of attribute.
     * @param value value to convert.
     * @return  this model.
     */
    public Model setDate(String attribute, Object value) {
        return set(attribute, Convert.toSqlDate(value));
    }

    /**
     * Performs a conversion to <code>java.sql.Date</code> if necessary,
     * uses {@link Convert#toSqlDate(Object)}
     * @param attribute attribute name
     * @return instance of <code>java.sql.Date</code>
     */
    public java.sql.Date getDate(String attribute) {
        return Convert.toSqlDate(get(attribute));
    }

    /**
     * Performs a primitive conversion of <code>java.util.Date</code> to <code>java.sql.Timestamp</code>
     * based on the time value.
     *
     *
     * @param name name of field.
     * @param date date value.
     * @deprecated  use {@link #setTimestamp(String, Object)} instead.
     */
    public void setTS(String name, java.util.Date date) {
        if(date == null) {
            set(name, null);
        } else {
            set(name, new java.sql.Timestamp(date.getTime()));
        }
    }

    /**
     * Sets values for this model instance. The sequence of values must correspond to sequence of names.
     *
     * @param attributeNames names of attributes.
     * @param values values for this instance.
     */
    public void set(String[] attributeNames, Object[] values) {
        if (attributeNames == null || values == null || attributeNames.length != values.length) {
            throw new IllegalArgumentException("must pass non-null arrays of equal length");
        }

        for (int i = 0; i < attributeNames.length; i++) {
            set(attributeNames[i], values[i]);
        }
    }

    /**
     * Sets a value of an attribute.
     *
     * @param attribute name of attribute to set. Names not related to this model will be rejected (those not matching table columns).
     * @param value value of attribute. Feel free to set any type, as long as it can be accepted by your driver.
     * @return reference to self, so you can string these methods one after another.
     */
    public Model set(String attribute, Object value) {
        if(attribute.equalsIgnoreCase("created_at") && manageTime) throw new IllegalArgumentException("cannot set 'created_at'");

        getMetaModelLocal().checkAttributeOrAssociation(attribute);

        attributes.put(attribute.toLowerCase(), value);
        return this;
    }

    /**
     * Will return true if this  instance is frozen, false otherwise.
     * A frozen instance cannot use used, as it has no relation to a record in table.
     *
     * @return true if this  instance is frozen, false otherwise.
     */
    public boolean isFrozen(){
        return frozen;
    }


    /**
     * Returns names of all attributes from this model.
     * @return names of all attributes from this model.
     */
    public static List<String>  attributes(){
        return getMetaModel().getAttributeNames();
    }

    /**
     * Returns all associations of this model.
     * @return all associations of this model.
     */
    public static List<Association> associations(){
        return getMetaModel().getAssociations();
    }

    /**
     * returns true if this is a new instance, not saved yet to DB, false otherwise.
     *
     * @return true if this is a new instance, not saved yet to DB, false otherwise
     */
    public boolean isNew(){
        return getId() == null;
    }


    /**
     * Synonym for {@link #isFrozen()}. if(m.frozen()) seems to read better than classical Java convention.
     *
     * @return true if this  instance is frozen, false otherwise.
     */
    public boolean frozen(){
        return isFrozen();
    }

    /**
     * Deletes a single table record represented by this instance. This method assumes that a corresponding table
     * has only one record whose PK is the ID of this instance.
     * After deletion, this instance becomes {@link #frozen()} and cannot be used anymore until {@link #thaw()} is called.
     *
     * @return true if a record was deleted, false if not.
     */
    public boolean delete() {
        fireBeforeDelete(this);
        boolean result;
        if( 1 == new DB(getMetaModelLocal().getDbName()).exec("DELETE FROM " + getMetaModelLocal().getTableName()
                + " WHERE " + getMetaModelLocal().getIdName() + "= ?", getId())) {

            frozen = true;
            if(getMetaModelLocal().cached()){
                QueryCache.instance().purgeTableCache(getMetaModelLocal().getTableName());
            }
            purgeEdges();
            result = true;
        }
        else{
            result =  false;
        }
        fireAfterDelete(this);
        return result;
    }


    /**
     * Convenience method, will call {@link #delete()} or {@link #deleteCascade()}.
     *
     * @param cascade true to call {@link #deleteCascade()}, false to call {@link #delete()}.
     */
    public void delete(boolean cascade){
       if(cascade){
           deleteCascade();
       }else{
           delete();
       }
    }

    /**
     * Deletes this record from associated table, as well as children.
     *
     * Deletes current model and all of its child and many to many associations. This is not a high performance method, as it will
     * load every row into a model instance before deleting, effectively calling (N + 1) per table queries to the DB, one to select all
     * the associated records (per table), and one delete statement per record. Use it for small data sets.
     *
     * <p/>
     * In cases of simple one to many and polymorphic associations, things are as expected, a parent is deleted an all children are
     * deleted as well, but in more complicated cases, this method will walk entire three of associated tables, sometimes
     * coming back to the same one where it all started.
     * It will follow associations of children and their associations too; consider this a true cascade delete with all implications
     * (circular dependencies, referential integrity constraints, potential performance bottlenecks, etc.)
     * <p/>
     *
     * Imagine a situation where you have DOCTORS and PATIENTS in many to many relationship (with DOCTORS_PATIENTS table
     * as a join table), and in addition PATIENTS and PRESCRIPTIONS in one to many relationship, where a patient might
     * have many prescriptions:
     *
     <pre>
     DOCTORS
        +----+------------+-----------+-----------------+
        | id | first_name | last_name | discipline      |
        +----+------------+-----------+-----------------+
        |  1 | John       | Kentor    | otolaryngology  |
        |  2 | Hellen     | Hunt      | dentistry       |
        |  3 | John       | Druker    | oncology        |
        +----+------------+-----------+-----------------+

     PATIENTS
        +----+------------+-----------+
        | id | first_name | last_name |
        +----+------------+-----------+
        |  1 | Jim        | Cary      |
        |  2 | John       | Carpenter |
        |  3 | John       | Doe       |
        +----+------------+-----------+

     DOCTORS_PATIENTS
        +----+-----------+------------+
        | id | doctor_id | patient_id |
        +----+-----------+------------+
        |  1 |         1 |          2 |
        |  2 |         1 |          1 |
        |  3 |         2 |          1 |
        |  4 |         3 |          3 |
        +----+-----------+------------+

     PRESCRIPTIONS
        +----+------------------------+------------+
        | id | name                   | patient_id |
        +----+------------------------+------------+
        |  1 | Viagra                 |          1 |
        |  2 | Prozac                 |          1 |
        |  3 | Valium                 |          2 |
        |  4 | Marijuana (medicinal)  |          2 |
        |  5 | CML treatment          |          3 |
        +----+------------------------+------------+
     * </pre>
     *
     * Lets start with a simple example, Doctor John Druker. This doctor has one patient John Doe, and the patient has one prescription.
     * So, when an instance of this doctor model is issued statement:
     * <pre>
     *     drDruker.deleteCascade();
     * </pre>
     * , the result is as expected: the DOCTORS:ID=3 is deleted, DOCTORS_PATIENTS:ID=4 is deleted, PATIENTS:ID=3 is deleted
     * and PRESCRIPTIONS:ID=5 is deleted.
     *
     * <p/>
     * However, when doctor Kentor(#1) is deleted, the following records are also deleted:
     * <ul>
     *     <li>DOCTORS_PATIENTS:ID=1, 2 - these are links to patients</li>
     *     <li>PATIENTS:ID=1,2 these are patients themselves</li>
     *     <li>PRESCRIPTIONS:ID=1,2,3,4  - these are prescriptions of patients 1 and 2</li>
     * </ul>
     * But, in addition, since this is a many to many relationship, deleting patients 1 and 2 results in also deleting
     * doctor Hellen Hunt(#2), since she is a doctor of patient Jim Cary(#1), deleting all corresponding join links from
     * table DOCTORS_PATIENTS. So, deleting doctor Kentor, deleted most all records from related tables, leaving only these
     * records in place:
     * <ul>
     *     <li>DOCTORS:ID=3</li>
     *     <li>DOCTORS_PATIENTS:ID=4</li>
     *     <li>PATIENTS:ID=3</li>
     *     <li>PRESCRIPTIONS:ID=5</li>
     * </ul>
     * Had doctor Hellen Hunt(#2) had more patients, it would delete them too, and so on. This goes a long way to say that it
     * could be easy to be tangled up in web of associations, so be careful out there.
     *
     * <p/>
     * After deletion, this instance becomes {@link #frozen()} and cannot be used anymore until {@link #thaw()} is called.
     */
    public void deleteCascade(){
        deleteCascadeExcept();
    }

    /**
     * This method does everything {@link #deleteCascade()} does, but in addition allows to exclude some assosiations
     * from this action. This is necessary because {@link #deleteCascade()} method can be far too eager to delete
     * records in a database, and this is a good way to tell the model to exclude some associations from deletes.
     *
     * <p>Example:</p>
     * <code>
     *     Patient.findById(3).deleteCascadeExcept(Patient.getMetaModel().getAssociationForTarget("prescriptions"));
     * </code>
     *
     * @see {@link #deleteCascade()} - see for more information.
     * @param excludedAssociations associations
     */
    public void deleteCascadeExcept(Association ... excludedAssociations){
        List<Association> excludedAssociationsList = Arrays.asList(excludedAssociations);
        deleteMany2ManyDeep(getMetaModelLocal().getManyToManyAssociations(excludedAssociationsList));
        deleteChildrenDeep(getMetaModelLocal().getOneToManyAssociations(excludedAssociationsList));
        deleteChildrenDeep(getMetaModelLocal().getPolymorphicAssociations(excludedAssociationsList));
        delete();
    }



    private void deleteMany2ManyDeep(List<Many2ManyAssociation> many2ManyAssociations){
        List<Model>  allMany2ManyChildren = new ArrayList<Model>();
        for (Association association : many2ManyAssociations) {
            String targetTableName = association.getTarget();
            Class c = Registry.instance().getModelClass(targetTableName, false);
            if(c == null){// this model is probably not defined as a class, but the table exists!
                logger.error("ActiveJDBC WARNING: failed to find a model class for: " + targetTableName + ", maybe model is not defined for this table?" +
                        " There might be a risk of running into integrity constrain violation if this model is not defined.");
            }
            else{
                allMany2ManyChildren.addAll(getAll(c));
            }
        }

        deleteJoinsForManyToMany();
        for (Model model : allMany2ManyChildren) {
            model.deleteCascade();
        }
    }

    /**
     * Deletes this record from associated table, as well as its immediate children. This is a high performance method
     * because it does not walk through a chain of child dependencies like {@link #deleteCascade()} does, but rather issues
     * one DELETE statement per child dependency table. Also, its semantics are a bit different between than {@link #deleteCascade()}.
     * It only deletes current record and immediate children, but not their children (no grand kinds are dead as a result :)).
     * <h4>One to many and polymorphic associations</h4>
     * The current record is deleted, as well as immediate children.
     * <h4>Many to many associations</h4>
     * The current record is deleted, as well as links in a join table. Nothing else is deleted.
     * <p/>
     * After deletion, this instance becomes {@link #frozen()} and cannot be used anymore until {@link #thaw()} is called.
     */
    public void deleteCascadeShallow(){
        deleteJoinsForManyToMany();
        deleteOne2ManyChildrenShallow();
        deletePolymorphicChildrenShallow();
        delete();
    }


    private void deleteJoinsForManyToMany() {
        List<? extends Association> associations = getMetaModelLocal().getManyToManyAssociations(Collections.<Association>emptyList());
        for (Association association : associations) {
            String join = ((Many2ManyAssociation)association).getJoin();
            String sourceFK = ((Many2ManyAssociation)association).getSourceFkName();
            String query = "DELETE FROM " + join + " WHERE " + sourceFK + " = " + getId();
            new DB(getMetaModelLocal().getDbName()).exec(query);
        }
    }

    private void deleteOne2ManyChildrenShallow() {
        List<OneToManyAssociation> childAssociations = getMetaModelLocal().getOneToManyAssociations(Collections.<Association>emptyList());
        for (OneToManyAssociation association : childAssociations) {
            String  target = association.getTarget();
            String query = "DELETE FROM " + target + " WHERE " + association.getFkName() + " = ?";
            new DB(getMetaModelLocal().getDbName()).exec(query, getId());
        }
    }

    private void deletePolymorphicChildrenShallow() {
        List<OneToManyPolymorphicAssociation> polymorphics = getMetaModelLocal().getPolymorphicAssociations(new ArrayList<Association>());
        for (OneToManyPolymorphicAssociation association : polymorphics) {
            String  target = association.getTarget();
            String parentType = association.getTypeLabel();
            String query = "DELETE FROM " + target + " WHERE parent_id = ? AND parent_type = ?";
            new DB(getMetaModelLocal().getDbName()).exec(query, getId(), parentType);
        }
    }


    private void deleteChildrenDeep(List<? extends Association> childAssociations){
        for (Association association : childAssociations) {
            String targetTableName = association.getTarget();
            Class c = Registry.instance().getModelClass(targetTableName, false);
            if(c == null){// this model is probably not defined as a class, but the table exists!
                logger.error("ActiveJDBC WARNING: failed to find a model class for: " + targetTableName + ", maybe model is not defined for this table?" +
                        " There might be a risk of running into integrity constrain violation if this model is not defined.");
            }
            else{
                List<Model> dependencies = getAll(c);
                for (Model model : dependencies) {
                    model.deleteCascade();
                }
            }
        }
    }

    /**
     * Deletes some records from associated table. This method does not follow any associations.
     * If this model has one to many associations, you might end up with either orphan records in child
     * tables, or run into integrity constraint violations. However, this method if very efficient as it deletes all records
     * in one shot, without pre-loading them.
     * This method also has a side-effect: it will not mark loaded instances corresponding to deleted records as "frozen".
     * This means that such an instance would allow calling save() and saveIt() methods resulting DB errors, as you
     * would be attempting to update phantom records.
     *
     *
     * @param query narrows which records to delete. Example: <pre>"last_name like '%sen%'"</pre>.
     * @param params   (optional) - list of parameters if a query is parametrized.
     * @return number od deleted records.
     */
    public static int delete(String query, Object... params) {
        MetaModel metaModel = getMetaModel();
        int count =  params == null || params.length == 0? new DB(metaModel.getDbName()).exec("DELETE FROM " + metaModel.getTableName() + " WHERE " + query) :
        new DB(metaModel.getDbName()).exec("DELETE FROM " + metaModel.getTableName() + " WHERE " + query, params);
        if(metaModel.cached()){
            QueryCache.instance().purgeTableCache(metaModel.getTableName());
        }
        purgeEdges();
        return count;
    }

    /**
     * Returns true if record corresponding to the id passed exists in the DB.
     *
     * @param id id in question.
     * @return true if corresponding record exists in DB, false if it does not.
     */
    public static boolean exists(Object id){
        MetaModel metaModel = getMetaModel();
        return null != new DB(metaModel.getDbName()).firstCell("SELECT " + metaModel.getIdName() + " FROM " + metaModel.getTableName()
                + " WHERE " + metaModel.getIdName() + " = ?", id);
    }

    /**
     * Returns true if record corresponding to the id of this instance exists in  the DB.
     *
     * @return true if corresponding record exists in DB, false if it does not.
     */
    public boolean exists(){
        MetaModel metaModel = getMetaModelLocal();
        return null != new DB(metaModel.getDbName()).firstCell("SELECT " + metaModel.getIdName() + " FROM " + metaModel.getTableName()
                + " WHERE " + metaModel.getIdName() + " = ?", getId());
    }

    /**
     * Deletes all records from associated table. This methods does not take associations into account.
     *
     * @return number of records deleted.
     */
    public static int deleteAll() {
        MetaModel metaModel = getMetaModel();
        int count = new DB(metaModel.getDbName()).exec("DELETE FROM " + metaModel.getTableName());
        if(metaModel.cached()){
            QueryCache.instance().purgeTableCache(metaModel.getTableName());
        }

        purgeEdges();
        return count;
    }

    /**
     * Updates records associated with this model.
     *
     * This example :
     * <pre>
     *  Employee.update("bonus = ?", "years_at_company > ?", "5", "10");
     * </pre
     * In this example, employees who worked for more than 10 years, get a bonus of 5% (not so generous :)).
     *
     *
     * @param updates - what needs to be updated.
     * @param conditions specifies which records to update. If this argument is <code>null</code>, all records in table will be updated.
     * In such cases, use a more explicit {@link #updateAll(String, Object...)} method.
     * @param params list of parameters for both updates and conditions. Applied in the same order as in the arguments,
     * updates first, then conditions.
     * @return number of updated records.
     */
    public static int update(String updates, String conditions, Object ... params) {
        //TODO: validate that the number of question marks is the same as number of parameters
        return ModelDelegate.update(Model.getMetaModel(), updates, conditions, params);
    }


    /**
     * Updates all records associated with this model.
     *
     * This example :
     * <pre>
     *  Employee.updateAll("bonus = ?", "10");
     * </pre
     * In this example, all employees get a bonus of 10%.
     *
     *
     * @param updates - what needs to be updated.
     * @param params list of parameters for both updates and conditions. Applied in the same order as in the arguments,
     * updates first, then conditions.
     * @return number of updated records.
     */
    public static int updateAll(String updates, Object ... params) {
        return update(updates, null, params);
    }

    /**
     * Returns all values of the model with all attribute names converted to lower case,
     * regardless how these names came from DB. This method is a convenience
     * method for displaying values on web pages.
     *
     * <p/>
     * If {@link LazyList#include(Class[])} method was used, and this
     * model belongs to a parent (as in many to one relationship), then the parent
     * will be eagerly loaded and also converted to a map. Parents' maps are keyed in the
     * returned map by underscored name of a parent model class name.
     * <p/>
     * For example, if this model were <code>Address</code>
     * and a parent is <code>User</code> (and user has many addresses), then the resulting map would
     * have all the attributes of the current table and another map representing a parent user with a
     * key "user" in current map.
     *
     * @return all values of the model with all attribute names converted to lower case.
     */
    public Map<String, Object> toMap(){
        Map<String, Object> retVal = new HashMap<String, Object>();
        for (String key : attributes.keySet()) {
            if(attributes.get(key) == null)
                continue;

            if(attributes.get(key) instanceof Clob){
                retVal.put(key.toLowerCase(), getString(key));
            }else{
                retVal.put(key.toLowerCase(), attributes.get(key));
            }
        }
        for(Class parentClass: cachedParents.keySet()){
            retVal.put(underscore(shortName(parentClass.getName())), cachedParents.get(parentClass).toMap());
        }

        for(Class childClass: cachedChildren.keySet()){
            List<Model> children = cachedChildren.get(childClass);

            List<Map> childMaps = new ArrayList<Map>(children.size());
            for(Model child:children){
                childMaps.add(child.toMap());
            }
            retVal.put(pluralize(underscore(shortName(childClass.getName()))), childMaps);
        }
        return retVal;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("Model: ").append(getClass().getName())
          .append(", table: '").append(getMetaModelLocal().getTableName())
          .append("', attributes: ").append(attributes);

        if (cachedParents.size() > 0) {
            sb.append(", parents: ").append(cachedParents);
        }

        if (cachedChildren.size() > 0) {
            sb.append(", children: ").append(cachedChildren);
        }
        return sb.toString();
    }


    /**
     * Parses XML into a model. It expects the same structure of XML as the method {@link #toXml(int, boolean, String...)}.
     * It ignores children and dependencies (for now) if any. This method  will parse the model attributes
     * from the XML document, and will then call {@link #fromMap(java.util.Map)} method. It does not save data into a database, just sets the
     * attributes.
     *
     * @param xml xml to read model attributes from.
     */
    public void fromXml(String xml) {

        try{
            //such dumb API!
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes()));
            String topTag = Inflector.underscore(getClass().getSimpleName());
            Element root = document.getDocumentElement();

            if(!root.getTagName().equals(topTag)){
                throw new InitException("top node has to match model name: " + topTag);
            }
            NodeList childNodes = root.getChildNodes();

            Map<String, String> attributes = new HashMap<String, String>();
            for(int i = 0; i < childNodes.getLength();i++){
                Node node  = childNodes.item(i);
                if(node instanceof Element){
                    Element child = (Element) node;
                    attributes.put(child.getTagName(), child.getFirstChild().getNodeValue());//this is even dumber!
                }
            }
            fromMap(attributes);
        }catch(Exception e){
            throw  new InitException(e);
        }
    }

    /**
     * Generates a XML document from content of this model.
     *
     * @param pretty pretty format (human readable), or one line text.
     * @param declaration true to include XML declaration at the top
     * @param attrs list of attributes to include. No arguments == include all attributes.
     * @return generated XML.
     */
    public String toXml(boolean pretty, boolean declaration, String ... attrs) {
        StringBuilder sb = new StringBuilder();

        if(declaration) {
            sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            if (pretty) { sb.append('\n'); }
        }
        toXmlP(sb, pretty, "", attrs);
        return sb.toString();
    }

    protected void toXmlP(StringBuilder sb, boolean pretty, String indent, String ... attrs) {

        Map<String, Object> modelMap = toMap();

        List<String> attrList = Arrays.asList(attrs);

        String topTag = Inflector.underscore(getClass().getSimpleName());
        if (pretty) { sb.append(indent); }
        sb.append('<').append(topTag).append('>');
        if (pretty) { sb.append('\n'); }

        for(String name: modelMap.keySet()){
            Object value  = modelMap.get(name);
            if((attrList.contains(name) || attrs.length == 0) && !(value instanceof List)){
                if (pretty) { sb.append("  ").append(indent); }
                sb.append('<').append(name).append('>').append(XmlEntities.XML.escape(value.toString())).append("</").append(name).append('>');
                if (pretty) { sb.append('\n'); }
            }else if (value instanceof List){
                List<Map> children = (List<Map>)value;
                if (pretty) { sb.append("  ").append(indent); }
                sb.append('<').append(name).append('>');
                if (pretty) { sb.append('\n'); }
                for(Map child: children){
                    if (pretty) { sb.append("    ").append(indent); }
                    sb.append('<').append(Inflector.singularize(name)).append('>');
                    if (pretty) { sb.append('\n'); }
                    for(Object childKey: child.keySet()){
                        if (pretty) { sb.append("      ").append(indent); }
                        sb.append('<').append(childKey).append('>').append(XmlEntities.XML.escape(child.get(childKey).toString())).append("</").append(childKey).append('>');
                        if (pretty) { sb.append('\n'); }
                    }
                    if (pretty) { sb.append("    ").append(indent); }
                    sb.append("</").append(Inflector.singularize(name)).append('>');
                    if (pretty) { sb.append('\n'); }
                }
                if (pretty) { sb.append("  ").append(indent); }
                sb.append("</").append(name).append('>');
                if (pretty) { sb.append('\n'); }
            }
        }
        beforeClosingTag(sb, pretty, pretty ? "  " + indent : "", attrs);
        if (pretty) { sb.append(indent); }
        sb.append("</").append(topTag).append('>');
        if (pretty) { sb.append('\n'); }
    }

    @Deprecated
    public String toXml(int spaces, boolean declaration, String ... attrs){
        return toXml(spaces > 0, declaration, attrs);
    }

    /**
     * Override in a subclass to inject custom content onto XML just before the closing tag.
     *
     * <p>To keep the formatting, it is recommended to implement this method as the example below.
     *
     * <blockquote><pre>
     * if (pretty) { sb.append(ident); }
     * sb.append("&lt;test&gt;...&lt;/test&gt;");
     * if (pretty) { sb.append('\n'); }
     * </pre></blockquote>
     *
     * @param sb to write content to.
     * @param pretty pretty format (human readable), or one line text.
     * @param indent indent at current level
     * @param attrs list of attributes to include
     */
    public void beforeClosingTag(StringBuilder sb, boolean pretty, String indent, String... attrs) {
        StringWriter writer = new StringWriter();
        beforeClosingTag(indent.length(), writer, attrs);
        sb.append(writer.toString());
    }

    /**
     * Override in a subclass to inject custom content onto XML just before the closing tag.
     *
     * @param spaces number of spaces of indent
     * @param writer to write content to.
     * @param attrs list of attributes to include
     *
     * @deprecated Use {@link #beforeClosingTag(StringBuilder, boolean, String, String...)} instead
     */
    @Deprecated
    public void beforeClosingTag(int spaces, StringWriter writer, String ... attrs) {
        // do nothing
    }

    /**
     * Generates a JSON document from content of this model.
     *
     * @param pretty pretty format (human readable), or one line text.
     * @param attrs  list of attributes to include. No arguments == include all attributes.
     * @return generated JSON.
     */
    public String toJson(boolean pretty, String... attrs) {
        StringBuilder sb = new StringBuilder();
        toJsonP(sb, pretty, "", attrs);
        return sb.toString();
    }

    private static DateFormat isoDateTimeFormater;
    static {
        //TODO missing time zone
        //TODO trim time if T00:00:00
        isoDateTimeFormater = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    }
    protected void toJsonP(StringBuilder sb, boolean pretty, String indent, String... attrs) {
        if (pretty) { sb.append(indent); }
        sb.append('{');

        StringBuilder sbAttrs = new StringBuilder();
        Collection<String> attrList = (attrs != null && attrs.length > 0) ? Arrays.asList(attrs) : attributes.keySet();
        for (String name : attrList) {
            if (sbAttrs.length() > 0) { sbAttrs.append(','); }
            if (pretty) { sbAttrs.append("\n  ").append(indent); }
            sbAttrs.append('"').append(name).append("\":");
            Object v = get(name);
            if (v == null) {
                sbAttrs.append("null");
            } else if (v instanceof Number || v instanceof Boolean) {
                sbAttrs.append(v);
            } else if (v instanceof Date) {
                sbAttrs.append('"').append(isoDateTimeFormater.format((Date) v)).append('"');
            } else {
                sbAttrs.append('"').append(v.toString()
                        .replace("\\", "\\\\")  // \
                        .replace("\"", "\\\"")  // "
                        .replace("\b", "\\b")   // \b
                        .replace("\f", "\\f")   // \f
                        .replace("\n", "\\n")   // \n
                        .replace("\r", "\\r")   // \r
                        .replace("\t", "\\t")   // \t
                ).append('"');
            }
        }
        sb.append(sbAttrs);

        if (cachedChildren != null && cachedChildren.size() > 0) {

            sb.append(',');
            if (pretty) { sb.append("\n  ").append(indent); }
            sb.append("\"children\":{");

            List<Class> childClasses = new ArrayList<Class>();
            childClasses.addAll(cachedChildren.keySet());

            for (int i = 0; i < childClasses.size(); i++) {
                if (i > 0) { sb.append(','); }
                Class childClass = childClasses.get(i);
                String name = Inflector.pluralize(childClass.getSimpleName()).toLowerCase();
                if (pretty) { sb.append("\n    ").append(indent); }
                sb.append('"').append(name).append("\":[");

                List<Model> child = cachedChildren.get(childClass);
                for (int j = 0; j < child.size(); j++) {
                    if (j > 0) { sb.append(','); }
                    if (pretty) { sb.append('\n'); }
                    child.get(j).toJsonP(sb, pretty, (pretty ? "      " + indent : ""));
                }

                if (pretty) { sb.append("\n    ").append(indent); }
                sb.append(']');
            }
            if (pretty) { sb.append("\n  ").append(indent); }
            sb.append('}');
        }

        beforeClosingBrace(sb, pretty, pretty ? "  " + indent : "", attrs);
        if (pretty) { sb.append('\n').append(indent); }
        sb.append('}');
    }

    /**
     * Override in subclasses in order to inject custom content into Json just before the closing brace.
     *
     * <p>To keep the formatting, it is recommended to implement this method as the example below.
     *
     * <blockquote><pre>
     * sb.append(',');
     * if (pretty) { sb.append('\n').append(indent); }
     * sb.append("\"test\":\"...\"");
     * </pre></blockquote>
     *
     * @param sb to write custom content to
     * @param pretty pretty format (human readable), or one line text.
     * @param indent indent at current level
     * @param attrs list of attributes to include
     */
    public void beforeClosingBrace(StringBuilder sb, boolean pretty, String indent, String... attrs) {
        StringWriter writer = new StringWriter();
        beforeClosingBrace(pretty, indent, writer);
        sb.append(writer.toString());
    }

    /**
     * Override in subclasses in order to inject custom content into Json just before the closing brace.
     *
     * @param pretty pretty format (human readable), or one line text.
     * @param indent indent at current level
     * @param writer writer to write custom content to
     */
    @Deprecated
    public void beforeClosingBrace(boolean pretty, String indent, StringWriter writer) {
        // do nothing
    }

    /**
     * Returns parent of this model, assuming that this table represents a child.
     * This method may return <code>null</code> in cases when you have orphan record and
     * referential integrity is not enforced in DBMS with a foreign key constraint.
     *
     * @param parentClass   class of a parent model.
     * @return instance of a parent of this instance in the "belongs to"  relationship.
     */
    public <T extends Model> T parent(Class<T> parentClass) {
        return parent(parentClass, false);
    }

    public <T extends Model> T parent(Class<T> parentClass, boolean cache) {
        T cachedParent = parentClass.cast(cachedParents.get(parentClass));
        if (cachedParent != null) {
            return cachedParent;
        }
        MetaModel parentMM = Registry.instance().getMetaModel(parentClass);
        String parentTable = parentMM.getTableName();

        BelongsToAssociation ass = (BelongsToAssociation)getMetaModelLocal().getAssociationForTarget(parentTable, BelongsToAssociation.class);
        BelongsToPolymorphicAssociation assP = (BelongsToPolymorphicAssociation)getMetaModelLocal()
            .getAssociationForTarget(parentTable, BelongsToPolymorphicAssociation.class);

        String fkValue;
        String fkName;
        if (ass != null) {
            fkValue = getString(ass.getFkName());
            fkName = ass.getFkName();
        } else if (assP != null) {
            fkValue = getString("parent_id");
            fkName = "parent_id";

            if (!assP.getTypeLabel().equals(getString("parent_type"))) {
                throw new IllegalArgumentException("Wrong parent: '" + parentClass + "'. Actual parent type label of this record is: '" + getString("parent_type") + "'");
            }
        } else {
            throw new IllegalArgumentException("there is no association with table: " + parentTable);
        }

        if (fkValue == null) {
            logger.debug("Attribute:  "  + fkName + " is null, cannot determine parent. Child record: " + this);
            return null;
        }
        String parentIdName = parentMM.getIdName();
        String query = getMetaModelLocal().getDialect().selectStarParametrized(parentTable, parentIdName);

        T parent;
        if (parentMM.cached()) {
            parent = parentClass.cast(QueryCache.instance().getItem(parentTable, query, new Object[]{fkValue}));
            if (parent != null) {
                return parent;
            }
        }

        List<Map> results = new DB(getMetaModelLocal().getDbName()).findAll(query, fkValue);
        //expect only one result here
        if (results.size() == 0) { //this should be covered by referential integrity constraint
            return null;
        } else {
            try {
                parent = parentClass.newInstance();
                parent.hydrate(results.get(0));
                if (parentMM.cached()) {
                    QueryCache.instance().addItem(parentTable, query, new Object[]{fkValue}, parent);
                }
                if (cache) {
                    setCachedParent(parent);
                }
                return parent;
            } catch (Exception e) {
                throw new InitException(e.getMessage(), e);
            }
        }
    }

    protected void setCachedParent(Model parent) {
        if (parent != null) {
            cachedParents.put(parent.getClass(), parent);
        }
    }


    /**
     * Sets multiple parents on this instance. Basically this sets a correct value of a foreign keys in a
     * parent/child relationship. This only works for one to many and polymorphic associations.
     *
     * @param parents - collection of potential parents of this instance. Its ID values must not be null.
     */
    public void setParents(Model... parents){
        for (Model parent : parents) {
            setParent(parent);
        }
    }


    /**
     * Sets a parent on this instance. Basically this sets a correct value of a foreign key in a
     * parent/child relationship. This only works for one to many and polymorphic associations.
     * The act of setting a parent does not result in saving to a database.
     *
     * @param parent potential parent of this instance. Its ID value must not be null.
     */
    public void setParent(Model parent) {
        if (parent == null || parent.getId() == null) {
            throw new IllegalArgumentException("parent cannot ne null and parent ID cannot be null");
        }
        List<Association> associations = getMetaModelLocal().getAssociations();
        for (Association association : associations) {
            if (association instanceof BelongsToAssociation && association.getTarget().equals(parent.getMetaModelLocal().getTableName())) {
                set(((BelongsToAssociation)association).getFkName(), parent.getId());
                return;
            }
            if(association instanceof BelongsToPolymorphicAssociation && association.getTarget().equals(parent.getMetaModelLocal().getTableName())){
                set("parent_id", parent.getId());
                set("parent_type", ((BelongsToPolymorphicAssociation)association).getTypeLabel());
                return;
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Class: ").append(parent.getClass()).append(" is not associated with ").append(this.getClass())
                .append(", list of existing associations:\n");
        join(sb, getMetaModelLocal().getAssociations(), "\n");
        throw new IllegalArgumentException(sb.toString());
    }

    /**
     * Copies all attribute values (except for ID, created_at and updated_at) from this instance to the other.
     *
     * @param other target model.
     */
    public <T extends Model> void copyTo(T other) {
        if (!getMetaModelLocal().getTableName().equals(other.getMetaModelLocal().getTableName())) {
            throw new IllegalArgumentException("can only copy between the same types");
        }

        List<String> attrs = getMetaModelLocal().getAttributeNamesSkip(getMetaModelLocal().getIdName());
        for (String name : attrs) {
            other.getAttributes().put(name, get(name));
        }
    }

    /**
     * Copies all attribute values (except for ID, created_at and updated_at) from this instance to the other.
     *
     * @param other target model.
     */
    public void copyFrom(Model other) {
        other.copyTo(this);
    }

    /**
     * This method should be called from all instance methods for performance.
     *
     * @return
     */
    protected MetaModel<?, ?> getMetaModelLocal(){
        if(metaModelLocal == null)
            metaModelLocal = getMetaModel();

        return metaModelLocal;
    }


    protected void setMetamodelLocal(MetaModel metamodelLocal){
        this.metaModelLocal = metamodelLocal;
    }

    /**
     * Re-reads all attribute values from DB.
     *
     */
    public void refresh() {
        Model fresh = findById(getId());

        if(fresh == null)
            throw new StaleModelException("Failed to refresh self because probably record with " +
                    "this ID does not exist anymore. Stale model: " + this);

        fresh.copyTo(this);
    }

    /**
     * Returns a value for attribute.
     *
     * <h3>Infer relationship from name of argument</h3>
     * Besides returning direct attributes of this model, this method is also
     * aware of relationships and can return collections based on naming conventions. For example, if a model User has a
     * one to many relationship with a model Address, then the following code will work:
     * <pre>
     * Address address = ...;
     * User user = (User)address.get("user");
     * </pre>
     * Conversely, this will also work:
     * <pre>
     * List&lt;Address&gt; addresses = (List&lt;Address&gt;)user.get(&quot;addresses&quot;);
     * </pre>
     *
     * The same would also work for many to many relationships:
     * <pre>
     * List&lt;Doctor&gt; doctors = (List&lt;Doctor&gt;)patient.get(&quot;doctors&quot;);
     * ...
     * List&lt;Patient&gt; patients = (List&lt;Patient&gt;)doctor.get(&quot;patients&quot;);
     * </pre>
     *
     * This methods will try to infer a name if a table by using {@link org.javalite.common.Inflector} to try to
     * convert it to singular and them plural form, an attempting to see if this model has an appropriate relationship
     * with another model, if one is found. This method of finding of relationships is best used in templating
     * technologies, such as JSPs. For standard cases, please use {@link #parent(Class)}, and {@link #getAll(Class)}.
     *
     * <h3>Suppressing inference for performance</h3>
     * <p>
     *     In some cases, the inference of relationships might take a toll on performance, and if a project is not using
     *     the getter method for inference, than it is wise to turn it off with a system property <code>activejdbc.get.inference</code>:
     *
     *     <pre>
     *         -Dactivejdbc.get.inference = false
     *     </pre>
     *     If inference is turned off, only a value of the attribute is returned.
     *
     * </p>
     *
     * @param attribute name of attribute of name or related object.
     * @return value for attribute.
     */
    public Object get(String attribute) {
        if(frozen) throw new FrozenException(this);

        if(attribute == null) throw new IllegalArgumentException("attribute cannot be null");

        // NOTE: this is a workaround for JSP pages. JSTL in cases ${item.id} does not call the getId() method, instead
        //calls item.get("id"), considering that this is a map only!
        if(!attributes.containsKey("id") && attribute.equalsIgnoreCase("id")){
            String idName = getMetaModelLocal().getIdName();
            return attributes.get(idName.toLowerCase());
        }

        Object returnValue;
        String attributeName = attribute.toLowerCase();

        String getInferenceProperty = System.getProperty("activejdbc.get.inference");

        boolean  getInference = getInferenceProperty  == null || getInferenceProperty.equals("true");

        if (getInference) {
            if ((getMetaModelLocal().hasAttribute(attributeName))) {
                return attributes.get(attributeName);//this should account for nulls too!
            } else if ((returnValue = tryParent(attributeName)) != null) {
                return returnValue;
            } else if ((returnValue = tryPolymorphicParent(attributeName)) != null) {
                return returnValue;
            } else if ((returnValue = tryChildren(attributeName)) != null) {
                return returnValue;
            } else if ((returnValue = tryPolymorphicChildren(attributeName)) != null) {
                return returnValue;
            } else if ((returnValue = tryOther(attributeName)) != null) {
                return returnValue;
            } else {
                getMetaModelLocal().checkAttributeOrAssociation(attributeName);
                return null;
            }
        } else {
            return attributes.get(attributeName);
        }
    }


    private Object tryPolymorphicParent(String parentTable){
        MetaModel parentMM = inferTargetMetaModel(parentTable);
        if(parentMM == null){
            return null;
        }else
            return getMetaModelLocal().hasAssociation(parentMM.getTableName(), BelongsToPolymorphicAssociation.class) ?
                parent(parentMM.getModelClass()): null;
    }

    private Object tryParent(String parentTable){
        MetaModel parentMM = inferTargetMetaModel(parentTable);
        if(parentMM == null){
            return null;
        }else
            return getMetaModelLocal().hasAssociation(parentMM.getTableName(), BelongsToAssociation.class) ?
                parent(parentMM.getModelClass()): null;
    }

    private Object tryPolymorphicChildren(String childTable){
        MetaModel childMM = inferTargetMetaModel(childTable);
        if(childMM == null){
            return null;
        }else
            return getMetaModelLocal().hasAssociation(childMM.getTableName(), OneToManyPolymorphicAssociation.class) ?
                getAll(childMM.getModelClass()): null;
    }

    private Object tryChildren(String childTable){
        MetaModel childMM = inferTargetMetaModel(childTable);
        if(childMM == null){
            return null;
        }else
            return getMetaModelLocal().hasAssociation(childMM.getTableName(), OneToManyAssociation.class) ?
                getAll(childMM.getModelClass()): null;
    }

    private Object tryOther(String otherTable){
        MetaModel otherMM = inferTargetMetaModel(otherTable);
        if(otherMM == null){
            return null;
        }else
            return getMetaModelLocal().hasAssociation(otherMM.getTableName(), Many2ManyAssociation.class) ?
                getAll(otherMM.getModelClass()): null;
    }

    private MetaModel inferTargetMetaModel(String targetTableName){
        String targetTable = singularize(targetTableName);
        MetaModel targetMM = Registry.instance().getMetaModel(targetTable);
        if(targetMM == null){
            targetTable = pluralize(targetTableName);
            targetMM = Registry.instance().getMetaModel(targetTable);
        }
        return targetMM != null? targetMM: null;
    }

    /*************************** typed getters *****************************************/
    /**
     * Get any value as string.
     *
     * @param attribute name of attribute.
     * @return attribute value as string.
     */
    public String getString(String attribute) {
        Object value = get(attribute);
        return Convert.toString(value);
    }

    /**
     * Gets a value as bytes. If the column is Blob, bytes are
     * read directly, if not, then the value is converted to String first, then
     * string bytes are returned. Be careful out there,  this will read entire
     * Blob onto memory.
     *
     * @param attribute name of attribute
     * @return value as bytes.
     */
    public byte[] getBytes(String attribute) {
        Object value = get(attribute);
        return Convert.toBytes(value);
    }

    /**
     * Converts Model value to <code>BigDecimal</code>
     *
     * @see {@link Convert#toBigDecimal}
     * @param attribute name of attribute to convert
     * @return converted value
     */
    public BigDecimal getBigDecimal(String attribute) {
        return Convert.toBigDecimal(get(attribute));
    }

    /**
     * Converts Model value to <code>Integer</code>
     *
     * @see {@link Convert#toInteger}
     * @param attribute name of attribute to convert
     * @return converted value
     */
    public Integer getInteger(String attribute) {
        return Convert.toInteger(get(attribute));
    }

    /**
     * Converts Model value to <code>Long</code>
     *
     * @see {@link Convert#toLong(Object)}
     * @param attribute name of attribute to convert
     * @return converted value
     */
    public Long getLong(String attribute) {
        return Convert.toLong(get(attribute));
    }

    /**
     * Converts Model value to <code>Short</code>
     *
     * @see {@link Convert#toShort(Object)}
     * @param attribute name of attribute to convert
     * @return converted value
     */
    public Short getShort(String attribute) {
       return Convert.toShort(get(attribute));
    }

    /**
     * Converts Model value to <code>Float</code>
     *
     * @see {@link Convert#toFloat(Object)}
     * @param attribute name of attribute to convert
     * @return converted value
     */
    public Float getFloat(String attribute) {
        return Convert.toFloat(get(attribute));
    }

    /**
     * If the value is instance of java.sql.Timestamp, returns it, else tries to convert the
     * value to Timestamp using {@link Timestamp#valueOf(String)}.
     * This method might trow <code>IllegalArgumentException</code> if fails at conversion.
     *
     * @see {@link Timestamp#valueOf(String)}
     * @param attribute attribute name
     * @return instance of Timestamp.
     */
    public Timestamp getTimestamp(String attribute) {
        return Convert.toTimestamp(get(attribute));
    }

    /**
     * Converts Model value to <code>Double</code>
     *
     * @see {@link Convert#toDouble(Object)}
     * @param attribute name of attribute to convert
     * @return converted value
     */
    public Double getDouble(String attribute) {
        return Convert.toDouble(get(attribute));
    }


    /**
     * Returns true if the value is any numeric type and has a value of 1, or
     * if string type has a value of 'y', 't', 'true' or 'yes'. Otherwise, return false.
     *
     * @param attribute attribute name
     * @return true if the value is any numeric type and has a value of 1, or
     * if string type has a value of 'y', 't', 'true' or 'yes'. Otherwise, return false.
     */
    public Boolean getBoolean(String attribute) {
        return Convert.toBoolean(get(attribute));
    }

    /*************************** typed setters *****************************************/

    /**
     * Converts object to string when setting.
     *
     * @param attribute name of attribute.
     * @param value value
     * @return reference to this model.
     */
    public Model setString(String attribute, Object value) {
        return set(attribute, value.toString());

    }

    /**
     * Converts object to BigDecimal when setting.
     *
     * @param attribute name of attribute.
     * @param value value
     * @return reference to this model.
     */
    public Model setBigDecimal(String attribute, Object value) {
        return set(attribute, Convert.toBigDecimal(value));
    }

    /**
     * Converts object to <code>Integer</code> when setting.
     *
     * @param attribute name of attribute.
     * @param value value
     * @return reference to this model.
     */
    public Model setInteger(String attribute, Object value) {
        return set(attribute, Convert.toInteger(value));
    }

    /**
     * Converts object to <code>Long</code> when setting.
     *
     * @param attribute name of attribute.
     * @param value value
     * @return reference to this model.
     */
    public Model setLong(String attribute, Object value) {
        return set(attribute, Convert.toLong(value));
    }

    /**
     * Converts object to <code>Float</code> when setting.
     *
     * @param attribute name of attribute.
     * @param value value
     * @return reference to this model.
     */
    public Model setFloat(String attribute, Object value) {
        return set(attribute, Convert.toFloat(value));
    }

    /**
     *
     * Converts object to <code>java.sql.Timestamp</code> when setting.
     * If the value is instance of java.sql.Timestamp, just sets it, else tries to convert the value to Timestamp using
     * Timestamp.valueOf(String). This method might trow IllegalArgumentException if fails at conversion.
     *
     * @param attribute name of attribute.
     * @param value value
     * @return reference to this model.
     */
    public Model setTimestamp(String attribute, Object value) {
        return set(attribute, Convert.toTimestamp(value));
    }

    /**
     * Converts object to <code>Double</code> when setting.
     *
     * @param attribute name of attribute.
     * @param value value
     * @return reference to this model.
     */
    public Model setDouble(String attribute, Object value) {
        return set(attribute, Convert.toDouble(value));
    }



    /**
     * Sets to <code>true</code> if the value is any numeric type and has a value of 1, or if string type has a
     * value of 'y', 't', 'true' or 'yes'. Otherwise, sets to false.
     *
     * @param attribute name of attribute.
     * @param value value to convert.
     * @return  this model.
     */
    public Model setBoolean(String attribute, Object value) {
        return set(attribute, Convert.toBoolean(value));
    }

    /**
     * This methods supports one to many, many to many relationships as well as polymorphic associations.
     * <p/>
     * In case of one to many, the <code>clazz</code>  must be a class of a child model, and it will return a
     * collection of all children.
     * <p/>
     * In case of many to many, the <code>clazz</code>  must be a class of a another related model, and it will return a
     * collection of all related models.
     * <p/>
     * In case of polymorphic, the <code>clazz</code>  must be a class of a polymorphically related model, and it will return a
     * collection of all related models.
     *
     *
     * @param clazz class of a child model for one to many, or class of another model, in case of many to many or class of child in case of
     * polymorphic
     *
     * @return list of children in case of one to many, or list of other models, in case many to many.
     */

    public <T extends Model> LazyList<T> getAll(Class<T> clazz) {
        List<Model> children = cachedChildren.get(clazz);
        if(children != null){
            return (LazyList<T>) children;
        }

        String tableName = Registry.instance().getTableName(clazz);
        if(tableName == null) throw new IllegalArgumentException("table: " + tableName + " does not exist for model: " + clazz);

        return get(tableName, null);
    }


    /**
     * Provides a list of child models in one to many, many to many and polymorphic associations, but in addition also allows to filter this list
     * by criteria.
     *
     * <p/>
     * <strong>1.</strong> For one to many, the criteria is against the child table.
     *
     * <p/>
     * <strong>2.</strong> For polymorphic association, the criteria is against the child table.
     *
     * <p/>
     * <strong>3.</strong> For many to many, the criteria is against the join table.
     * For example, if you have table PROJECTS, ASSIGNMENTS and PROGRAMMERS, where a project has many programmers and a programmer
     * has many projects, and ASSIGNMENTS is a join table, you can write code like this, assuming that the ASSIGNMENTS table
     * has a column <code>duration_weeks</code>:
     *
     * <pre>
     * List<Project> threeWeekProjects = programmer.get(Project.class, "duration_weeks = ?", 3);
     * </pre>
     * where this list will contain all projects to which this programmer is assigned for 3 weeks.
     *
     * @param clazz related type
     * @param query sub-query for join table.
     * @param params parameters for a sub-query
     * @return list of relations in many to many
     */
    public <T extends Model> LazyList<T> get(Class<T> clazz, String query, Object ... params){
        return get(Registry.instance().getTableName(clazz), query, params);
    }

    private <T extends Model> LazyList<T> get(String targetTable, String criteria, Object ...params) {
        //TODO: interesting thought: is it possible to have two associations of the same name, one to many and many to many? For now, say no.

        OneToManyAssociation oneToManyAssociation = (OneToManyAssociation)getMetaModelLocal().getAssociationForTarget(targetTable, OneToManyAssociation.class);
        Many2ManyAssociation manyToManyAssociation = (Many2ManyAssociation)getMetaModelLocal().getAssociationForTarget(targetTable, Many2ManyAssociation.class);

        OneToManyPolymorphicAssociation oneToManyPolymorphicAssociation = (OneToManyPolymorphicAssociation)getMetaModelLocal().getAssociationForTarget(targetTable, OneToManyPolymorphicAssociation.class);

        String additionalCriteria =  criteria != null? " AND ( " + criteria + " ) " : "";
        String subQuery;
        if (oneToManyAssociation != null) {
            subQuery = oneToManyAssociation.getFkName() + " = ? " + additionalCriteria;
        } else if (manyToManyAssociation != null) {
            String targetId = Registry.instance().getMetaModel(targetTable).getIdName();
            String joinTable = manyToManyAssociation.getJoin();

            String query = "SELECT " + targetTable + ".* FROM " + targetTable + ", " + joinTable +
                " WHERE " + targetTable + "." + targetId + " = " + joinTable + "." + manyToManyAssociation.getTargetFkName() +
                " AND " + joinTable + "." + manyToManyAssociation.getSourceFkName() + " = ? " + additionalCriteria;

            Object[] allParams = new Object[params.length + 1];
            allParams[0] = getId();
            for (int i = 0; i < params.length; i++) {
                allParams[i + 1] = params[i];
            }
            return new LazyList<T>(true, Registry.instance().getMetaModel(targetTable), query, allParams);
        } else if (oneToManyPolymorphicAssociation != null) {
            subQuery = "parent_id = ? AND " + " parent_type = '" + oneToManyPolymorphicAssociation.getTypeLabel() + "'" + additionalCriteria;
        } else {
            throw new NotAssociatedException(getMetaModelLocal().getTableName(), targetTable);
        }

        Object[] allParams = new Object[params.length + 1];
        allParams[0] = getId();
        for (int i = 0; i < params.length; i++) {
            allParams[i + 1] = params[i];
        }
        return new LazyList<T>(subQuery, Registry.instance().getMetaModel(targetTable), allParams);
    }

    protected static NumericValidationBuilder validateNumericalityOf(String... attributes) {
        return ValidationHelper.addNumericalityValidators(getClassName(), ModelDelegate.toLowerCase(attributes));
    }

    /**
     * Adds a validator to the model.
     *
     * @param validator new validator.
     * @return
     */
    public static ValidationBuilder addValidator(Validator validator){
        return ValidationHelper.addValidator(getClassName(), validator);
    }

    /**
     * Adds a new error to the collection of errors. This is a convenience method to be used from custom validators.
     *
     * @param key - key wy which this error can be retrieved from a collection of errors: {@link #errors()}.
     * @param value - this is a key of the message in the resource bundle.
     * @see {@link Messages}.
     */
    public void addError(String key, String value){
        errors.put(key, value);
    }

    public static void removeValidator(Validator validator){
        Registry.instance().removeValidator(Model.<Model>getDaClass(), validator);
    }

    public static List<Validator> getValidators(Class<Model> daClass){
        return Registry.instance().getValidators(daClass.getName());
    }


    /**
     * Validates an attribite format with a ree hand regular expression.
     *
     * @param attribute attribute to validate.
     * @param pattern regexp pattern which must match  the value.
     * @return
     */
    protected static ValidationBuilder validateRegexpOf(String attribute, String pattern) {
        return ValidationHelper.addRegexpValidator(getClassName(), attribute.toLowerCase(), pattern);
    }

    /**
     * Validates email format.
     *
     * @param attribute name of atribute that holds email value.
     * @return
     */
    protected static ValidationBuilder validateEmailOf(String attribute) {
        return ValidationHelper.addEmailValidator(getClassName(), attribute.toLowerCase());
    }

    /**
     * Validates range. Accepted types are all java.lang.Number subclasses:
     * Byte, Short, Integer, Long, Float, Double BigDecimal.
     *
     * @param attribute attribute to validate - should be within range.
     * @param min min value of range.
     * @param max max value of range.
     * @return
     */
    protected static ValidationBuilder validateRange(String attribute, Number min, Number max) {
        return ValidationHelper.addRangeValidator(getClassName(), attribute.toLowerCase(), min, max);
    }

    /**
     * The validation will not pass if the value is either an empty string "", or null.
     *
     * @param attributes list of attributes to validate.
     * @return
     */
    protected static ValidationBuilder validatePresenceOf(String... attributes) {
        return ValidationHelper.addPresenceValidators(getClassName(), ModelDelegate.toLowerCase(attributes));
    }

    /**
     * Add a custom validator to the model.
     *
     * @param validator  custom validator.
     */
    protected static ValidationBuilder validateWith(Validator validator) {
        return addValidator(validator);
    }

    /**
     * Adds a custom converter to the model.
     *
     * @param converter custom converter
     */
    protected static ValidationBuilder convertWith(Converter converter) {
        return addValidator(converter);
    }

    /**
     * Converts a named attribute to <code>java.sql.Date</code> if possible.
     * Acts as a validator if cannot make a conversion.
     *
     * @param attributeName name of attribute to convert to <code>java.sql.Date</code>.
     * @param format format for conversion. Refer to {@link java.text.SimpleDateFormat}
     * @return message passing for custom validation message.
     */
    protected static ValidationBuilder convertDate(String attributeName, String format){
        return ValidationHelper.addDateConverter(getClassName(), attributeName, format);
    }

    /**
     * Converts a named attribute to <code>java.sql.Timestamp</code> if possible.
     * Acts as a validator if cannot make a conversion.
     *
     * @param attributeName name of attribute to convert to <code>java.sql.Timestamp</code>.
     * @param format format for conversion. Refer to {@link java.text.SimpleDateFormat}
     * @return message passing for custom validation message.
     */
    protected static ValidationBuilder convertTimestamp(String attributeName, String format){
        return ValidationHelper.addTimestampConverter(getClassName(), attributeName, format);
    }

    public static boolean belongsTo(Class<? extends Model> targetClass) {
        String targetTable = Registry.instance().getTableName(targetClass);
        MetaModel metaModel = getMetaModel();
        return (null != metaModel.getAssociationForTarget(targetTable, BelongsToAssociation.class) ||
                null != metaModel.getAssociationForTarget(targetTable, Many2ManyAssociation.class));
    }


     public static void addCallbacks(CallbackListener ... listeners){
         for(CallbackListener listener: listeners ){
             Registry.instance().addListener(getDaClass(), listener);
         }
    }

    /**
     * This method performs validations and then returns true if no errors were generated, otherwise returns false.
     *
     * @return true if no errors were generated, otherwise returns false.
     */
    public boolean isValid(){
        validate();
        return !hasErrors();
    }

    /**
     * Executes all validators attached to this model.
     */
    public void validate() {
        fireBeforeValidation(this);
        errors = new Errors();
        List<Validator> theValidators = Registry.instance().getValidators(getClass().getName());
        if(theValidators != null){
            for (Validator validator : theValidators) {
                validator.validate(this);
            }
        }
        fireAfterValidation(this);
    }

    public boolean hasErrors() {
        return errors != null && errors.size() > 0;
    }

    /**
     * Binds a validator to an attribute if validation fails. 
     *
     * @param errorKey key of error in errors map. Usually this is a name of attribute,
     * but not limited to that, can be anything.
     * 
     * @param validator -validator that failed validation.
     */
    public void addValidator(Validator validator, String errorKey) {
        if(!errors.containsKey(errorKey))
            errors.addValidator(errorKey, validator);
    }

    /**
     * Provides an instance of <code>Errors</code> object, filled with error messages after validation.
     *
     * @return an instance of <code>Errors</code> object, filled with error messages after validation.
     */
    public Errors errors() {
        return errors;
    }

    /**
     * Provides an instance of localized <code>Errors</code> object, filled with error messages after validation.
     *
     * @param locale locale.
     * @return an instance of localized <code>Errors</code> object, filled with error messages after validation.
     */
    public Errors errors(Locale locale) {
        errors.setLocale(locale);
        return errors;
    }

    /**
     * This is a convenience method to create a model instance already initialized with values.
     * Example:
     * <pre>
     * Person p = Person.create("name", "Sam", "last_name", "Margulis", "dob", "2001-01-07");
     * </pre>
     *
     * The first (index 0) and every other element in the array is an attribute name, while the second (index 1) and every
     * other is a corresponding value.
     *
     * This allows for better readability of code. If you just read this aloud, it will become clear.
     *
     * @param namesAndValues names and values. elements at indexes 0, 2, 4, 8... are attribute names, and elements at
     * indexes 1, 3, 5... are values. Element at index 1 is a value for attribute at index 0 and so on.
     * @return newly instantiated model.
     */
    public static <T extends Model> T create(Object ... namesAndValues){

        if(namesAndValues.length %2 != 0) throw new IllegalArgumentException("number of arguments must be even");

        try{

            Model m = getDaClass().newInstance();
            ModelDelegate.setNamesAndValues(m, namesAndValues);
            return (T) m;
        }
        catch(IllegalArgumentException e){throw e;}
        catch(ClassCastException e){throw new  IllegalArgumentException("All even arguments must be strings");}
        catch(DBException e){throw e;}
        catch (Exception e){throw new InitException("Model '" + getClassName() + "' must provide a default constructor. Table:", e);}
    }


    /**
     * This is a convenience method to set multiple values to a model.
     * Example:
     * <pre>
     * Person p = ...
     * Person p = p.set("name", "Sam", "last_name", "Margulis", "dob", "2001-01-07");
     * </pre>
     *
     * The first (index 0) and every other element in the array is an attribute name, while the second (index 1) and every
     * other is a corresponding value.
     *
     * This allows for better readability of code. If you just read this aloud, it will become clear.
     *
     * @param namesAndValues names and values. elements at indexes 0, 2, 4, 8... are attribute names, and elements at
     * indexes 1, 3, 5... are values. Element at index 1 is a value for attribute at index 0 and so on.
     * @return newly instantiated model.
     */
    public Model set(Object ... namesAndValues){
        ModelDelegate.setNamesAndValues(this, namesAndValues);
        return this;
    }

    /**
     * This is a convenience method to {@link #create(Object...)}. It will create a new model and will save it
     * to DB. It has the same semantics as {@link #saveIt()}.
     *
     * @param namesAndValues names and values. elements at indexes 0, 2, 4, 8... are attribute names, and elements at
     * indexes 1, 3, 5... are values. Element at index 1 is a value for attribute at index 0 and so on.
     * @return newly instantiated model which also has been saved to DB.
     */
    public static <T extends Model> T createIt(Object ... namesAndValues){
        T m = (T)create(namesAndValues);
        m.saveIt();
        return m;
    }

    public static <T extends Model> T findById(Object id) {
        if(id == null) return null;

        MetaModel mm = getMetaModel();
        LazyList<T> l = new LazyList<T>(mm.getIdName() + " = ?", mm, id).limit(1);
        return l.size() > 0 ? l.get(0) : null;

    }

    /**
     * Finder method for DB queries based on table represented by this model. Usually the SQL starts with:
     *
     * <code>"select * from table_name where " + subquery</code> where table_name is a table represented by this model.
     *
     * Code example:
     * <pre>
     *
     * List<Person> teenagers = Person.where("age &gt ? and age &lt ?", 12, 20);
     * // iterate...
     *
     * //same can be achieved (since parameters are optional):
     * List<Person> teenagers = Person.where("age &gt 12 and age &lt 20");
     * //iterate
     * </pre>
     *
     * Limit, offset and order by can be chained like this:
     *
     * <pre>
     * List<Person> teenagers = Person.where("age &gt ? and age &lt ?", 12, 20).offset(101).limit(20).orderBy(age);
     * //iterate
     * </pre>
     *
     * This is a great way to build paged applications.
     *
     *
     * @param subquery this is a set of conditions that normally follow the "where" clause. Example:
     * <code>"department = ? and dob > ?"</code>. If this value is "*" and no parameters provided, then {@link #findAll()} is executed.
     * @param params list of parameters corresponding to the place holders in the subquery.
     * @return instance of <code>LazyList<Model></code> containing results.
     */
    public static <T extends Model> LazyList<T> where(String subquery, Object... params) {
        return find(subquery, params);
    }



    /**
     * Synonym of {@link #where(String, Object...)}
     *
     * @param subquery this is a set of conditions that normally follow the "where" clause. Example:
     * <code>"department = ? and dob &gt ?"</code>. If this value is "*" and no parameters provided, then {@link #findAll()} is executed.
     * @param params list of parameters corresponding to the place holders in the subquery.
     * @return instance of <code>LazyList<Model></code> containing results.
     */
    public static <T extends Model> LazyList<T> find(String subquery, Object... params) {

        if(subquery.trim().equals("*") && params.length == 0){
            return findAll();
        }

        if(subquery.equals("*") && params.length != 0){
            throw new IllegalArgumentException("cannot provide parameters with query: '*', use findAll() method instead");
        }

        return  new LazyList(subquery, getMetaModel(), params);
    }


    /**
     * Synonym of {@link #first(String, Object...)}.
     *
     * @param subQuery selection criteria, example:
     * <pre>
     * Person johnTheTeenager = Person.findFirst("name = ? and age &gt 13 and age &lt 19 order by age", "John")
     * </pre>
     * Sometimes a query might be just a clause like this:
     * <pre>
     * Person oldest = Person.findFirst("order by age desc")
     * </pre>
     * @param params list of parameters if question marks are used as placeholders
     * @return a first result for this condition. May return null if nothing found.
     */
    public static <T extends Model> T findFirst(String subQuery, Object... params) {
        LazyList<T> results = new LazyList<T>(subQuery, getMetaModel(), params).limit(1);
        return  results.size() > 0 ? results.get(0) : null;
    }


    /**
     * Returns a first result for this condition. May return null if nothing found.
     * If last result is needed, then order by some field and call this nethod:
     *
     * Synonym of {@link #findFirst(String, Object...)}.
     * <pre>
     * //first:
     * Person youngestTeenager= Person.first("age &gt 12 and age &lt 20 order by age");
     *
     * //last:
     * Person oldestTeenager= Person.first("age &gt 12 and age &lt 20 order by age desc");
     * </pre>
     *
     *
     * @param subQuery selection criteria, example:
     * <pre>
     * Person johnTheTeenager = Person.first("name = ? and age &lt 13 order by age", "John")
     * </pre>
     * Sometimes a query might be just a clause like this:
     * <pre>
     * Person p = Person.first("order by age desc")
     * </pre>
     * @param params list of parameters if question marks are used as placeholders
     * @return a first result for this condition. May return null if nothing found.
     */
    public static <T extends Model> T first(String subQuery, Object... params) {
        return (T)findFirst(subQuery, params);
    }


    /**
     * This method is for processing really large result sets. Results found by this method are never cached.
     *
     * @param query query text.
     * @param listener this is a call back implementation which will receive instances of models found.
     * @deprecated use {@link #findWith(ModelListener, String, Object...)}.
     */
    public static void find(String query, final ModelListener listener) {
        findWith(listener, query);
    }


    /**
     * This method is for processing really large result sets. Results found by this method are never cached.
     *
     * @param listener this is a call back implementation which will receive instances of models found.
     * @param query sub-query (content after "WHERE" clause)
     * @param params optional parameters for a query.
     */
    public static void findWith(final ModelListener listener, String query, Object ... params) {
        long start = System.currentTimeMillis();
        final MetaModel metaModel = getMetaModel();
        String sql = metaModel.getDialect().selectStar(metaModel.getTableName(), query);

        new DB(metaModel.getDbName()).find(sql, params).with( new RowListenerAdapter() {
            @Override
            public void onNext(Map<String, Object> row) {
                listener.onModel(instance(row, metaModel));
            }
        });
        LogFilter.logQuery(logger, sql, null, start);
    }


    /**
     * Free form query finder. Example:
     * <pre>
     * List<Rule> rules = Rule.findBySQL("select rule.*, goal_identifier from rule, goal where goal.goal_id = rule.goal_id order by goal_identifier asc, rule_type desc");
     * </pre>
     * Ensure that the query returns all columns associated with this model, so that the resulting models could hydrate itself properly.
     * Returned columns that are not part of this model will be ignored, but can be used for caluses like above.
     *
     * @param fullQuery free-form SQL.
     * @param params parameters if query is parametrized.
     * @param <T> - class that extends Model.
     * @return list of models representing result set.
     */
    public static <T extends Model> LazyList<T> findBySQL(String fullQuery, Object... params) {
        return  new LazyList<T>(false, getMetaModel(), fullQuery,  params);
     }

    /**
     * This method returns all records from this table. If you need to get a subset, look for variations of "find()".
     *
     * @return result list
     */
    public static  <T extends Model> LazyList<T>   findAll() {
        return new LazyList(null, getMetaModel());
    }

    /**
     * Adds a new child dependency. This method works for all three association types:
     * <ul>
     * <li>One to many - argument model should be a child in the relationship. This method will immediately set it's
     * ID as a foreign key on the child and will then save the child.</li>
     * <li>Many to many - argument model should be the other model in the relationship. This method will check if the
     * added child already has an ID. If the child does have an ID, then the method will create a link in the join
     * table. If the child does not have an ID, then this method saves the child first, then creates a record in the
     * join table linking this model instance and the child instance.</li>
     * <li>Polymorphic - argument model should be  a polymorphic child of this model. This method will set the
     * <code>parent_id</code> and <code>parent_type</code> as appropriate and then will then save the child.</li>
     * </ul>
     *
     * This method will throw a {@link NotAssociatedException} in case a model that has no relationship is passed.
     *
     * @param child instance of a model that has a relationship to the current model.
     * Either one to many or many to many relationships are accepted.
     */
    public void add(Model child) {

        //TODO: refactor this method
        String childTable = Registry.instance().getTableName(child.getClass());
        MetaModel metaModel = getMetaModelLocal();
        if (getId() != null) {

            if (metaModel.hasAssociation(childTable, OneToManyAssociation.class)) {
                OneToManyAssociation ass = (OneToManyAssociation)metaModel.getAssociationForTarget(childTable, OneToManyAssociation.class);
                String fkName = ass.getFkName();
                child.set(fkName, getId());
                child.saveIt();//this will cause an exception in case validations fail.
            }else if(metaModel.hasAssociation(childTable, Many2ManyAssociation.class)){
                Many2ManyAssociation ass = (Many2ManyAssociation) metaModel.getAssociationForTarget(childTable, Many2ManyAssociation.class);
                String join = ass.getJoin();
                String sourceFkName = ass.getSourceFkName();
                String targetFkName = ass.getTargetFkName();
                if(child.getId() == null)
                    child.saveIt();

                MetaModel joinMM = Registry.instance().getMetaModel(join);
                if(joinMM == null){
                    new DB(metaModel.getDbName()).exec("INSERT INTO " + join + " ( " + sourceFkName + ", " + targetFkName + " ) VALUES ( " + getId()+ ", " + child.getId() + ")");
                }else{
                    //TODO: write a test to cover this case:
                    //this is for Oracle, many 2 many, and all annotations used, including @IdGenerator. In this case,
                    //it is best to delegate generation of insert to a model (sequences, etc.)
                    try{
                        Model joinModel = (Model)joinMM.getModelClass().newInstance();
                        joinModel.set(sourceFkName, getId());
                        joinModel.set(targetFkName, child.getId());
                        joinModel.saveIt();
                    }
                    catch(InstantiationException e){
                        throw new InitException("failed to create a new instance of class: " + joinMM.getClass()
                                + ", are you sure this class has a default constructor?", e);
                    }
                    catch(IllegalAccessException e){throw new InitException(e);}
                    finally {
                        QueryCache.instance().purgeTableCache(join);
                        QueryCache.instance().purgeTableCache(metaModel.getTableName());
                        QueryCache.instance().purgeTableCache(childTable);
                    }
                }
             }else if(metaModel.hasAssociation(childTable, OneToManyPolymorphicAssociation.class)){

                OneToManyPolymorphicAssociation ass = (OneToManyPolymorphicAssociation)metaModel
                        .getAssociationForTarget(childTable, OneToManyPolymorphicAssociation.class);
                child.set("parent_id", getId());
                child.set("parent_type", ass.getTypeLabel());
                child.saveIt();

            }else
                throw new NotAssociatedException(metaModel.getTableName(), childTable);
        } else {
            throw new IllegalArgumentException("You can only add associated model to an instance that exists in DB. Save this instance first, then you will be able to add dependencies to it.");
        }
    }


    /**
     * Removes associated child from this instance. The child model should be either in belongs to association (including polymorphic) to this model
     * or many to many association.
     *
     * <h3>One to many and polymorphic associations</h3>
     * This method will simply call <code>child.delete()</code> method. This will render the child object frozen.
     *
     * <h3>Many to many associations</h3>
     * This method will remove an associated record from the join table, and will do nothing to the child model or record.
     *
     * <p/>
     * This method will throw a {@link NotAssociatedException} in case a model that has no relationship is passed.
     *
     * @param child model representing a "child" as in one to many or many to many association with this model.
     */
    public void remove(Model child){

        if(child == null) throw new IllegalArgumentException("cannot remove what is null");

        if(child.frozen() || child.getId() == null) throw new IllegalArgumentException("Cannot remove a child that does " +
                "not exist in DB (either frozen, or ID not set)");

        String childTable = Registry.instance().getTableName(child.getClass());
        MetaModel metaModel = getMetaModelLocal();
        if (getId() != null) {
            if (metaModel.hasAssociation(childTable, OneToManyAssociation.class)
                    || metaModel.hasAssociation(childTable, OneToManyPolymorphicAssociation.class)) {
                child.delete();
            }else if(metaModel.hasAssociation(childTable, Many2ManyAssociation.class)){
                Many2ManyAssociation ass = (Many2ManyAssociation)metaModel.getAssociationForTarget(childTable, Many2ManyAssociation.class);
                String join = ass.getJoin();
                String sourceFkName = ass.getSourceFkName();
                String targetFkName = ass.getTargetFkName();
                new DB(metaModel.getDbName()).exec("DELETE FROM " + join + " WHERE " + sourceFkName + " = ? AND "
                        + targetFkName + " = ?", getId(), child.getId());
            }else
                throw new NotAssociatedException(metaModel.getTableName(), childTable);
        } else {
            throw new IllegalArgumentException("You can only add associated model to an instance that exists in DB. " +
                    "Save this instance first, then you will be able to add dependencies to it.");
        }
    }

    /**
     * This method will not exit silently like {@link #save()}, it instead will throw {@link org.javalite.activejdbc.validation.ValidationException}
     * if validations did not pass.
     *
     * @return  true if the model was saved, false if you set an ID value for the model, but such ID does not exist in DB.
     */
    public boolean saveIt() {
        boolean result = save();
        purgeEdges();
        if(errors.size() > 0){
            throw new ValidationException(this);
        }
        return result;
    }



    /**
     * Resets all data in this model, including the ID.
     * After this method, this instance is equivalent to an empty, just created instance.
     */
    public void reset() {
        attributes = new HashMap<String, Object>();
    }

    /**
     * Unfreezes this model. After this method it is possible again to call save() and saveIt() methods.
     * This method will erase the value of ID on this instance, while preserving all other attributes' values.
     *
     * If a record was deleted, it is frozen and cannot be saved. After it is thawed, it can be saved again, but it will
     * generate a new insert statement and create a new record in the table with all the same attribute values.
     *
     * <p/><p/>
     * Synonym for {@link #defrost()}.
     */
    public void thaw(){
        attributes.put(getMetaModelLocal().getIdName(), "");//makes it blank
        frozen = false;
    }

    /**
     * Synonym for {@link #thaw()}.
     */
    public void defrost(){
        thaw();
    }

    /**
     * This method will save data from this instance to a corresponding table in the DB.
     * It will generate insert SQL if the model is new, or update if the model exists in the DB.
     * This method will execute all associated validations and if those validations generate errors,
     * these errors are attached to this instance. Errors are available by {#link #errors() } method.
     * The <code>save()</code> method is mostly for web applications, where code like this is written:
     * <pre>
     * if(person.save())
     *      //show page success
     * else{
     *      request.setAttribute("errors", person.errors());
     *      //show errors page, or same page so that user can correct errors.
     *   }
     * </pre>
     *
     * In other words, this method will not throw validation exceptions. However, if there is a problem in the DB, then
     * there can be a runtime exception thrown.
     *
     * @return true if a model was saved and false if values did not pass validations and the record was not saved.
     * False will also be returned if you set an ID value for the model, but such ID does not exist in DB.
     */
    public boolean save() {
        if(frozen) throw new FrozenException(this);

        fireBeforeSave(this);

        validate();
        if (hasErrors()) {
            return false;
        }

        boolean result;
        if (blank(getId())) {
            result = doInsert();
        } else {
            result = update();
        }
        fireAfterSave(this);
        return result;
    }

    /**
     * Returns total count of records in table.
     *
     * @return total count of records in table.
     */
    public static Long count() {
        MetaModel metaModel = getMetaModel();
        String sql = "SELECT COUNT(*) FROM " + metaModel.getTableName();
        Long result;
        if(metaModel.cached()){
         result = (Long)QueryCache.instance().getItem(metaModel.getTableName(), sql, null);
            if(result == null)
            {
                result = new DB(metaModel.getDbName()).count(metaModel.getTableName());
                QueryCache.instance().addItem(metaModel.getTableName(), sql, null, result);
            }
        }else{
            result = new DB(metaModel.getDbName()).count(metaModel.getTableName());
        }
        return result;
    }

    /**
     * Returns count of records in table under a condition.
     *
     * @param query query to select records to count.
     * @param params parameters (if any) for the query.
     * @return count of records in table under a condition.
     */
    public static Long count(String query, Object... params) {

        MetaModel metaModel = getMetaModel();

        //attention: this SQL is only used for caching, not for real queries.
        String sql = "SELECT COUNT(*) FROM " + metaModel.getTableName() + " where " + query;

        Long result;
        if(metaModel.cached()){
            result = (Long)QueryCache.instance().getItem(metaModel.getTableName(), sql, params);
            if(result == null){
                result = new DB(metaModel.getDbName()).count(metaModel.getTableName(), query, params);
                QueryCache.instance().addItem(metaModel.getTableName(), sql, params, result);
            }
        }else{
            result = new DB(metaModel.getDbName()).count(metaModel.getTableName(), query, params);
        }
        return result;
    }


    /**
     * @return attributes names that have been set by client code.
     */
    private List<String> getValueAttributeNames(boolean includeId) {
        List<String> attributeNames = new ArrayList<String>();

        for(String name: attributes.keySet()){
            if(includeId){
                if (!name.equalsIgnoreCase(getMetaModelLocal().getVersionColumn()))
                    attributeNames.add(name);
            }else{
                if (!name.equalsIgnoreCase(getMetaModelLocal().getVersionColumn()) && !name.equalsIgnoreCase(getMetaModelLocal().getIdName()))
                    attributeNames.add(name);
            }
        }
        return attributeNames;
      }


    private boolean doInsert() {

        fireBeforeCreate(this);
        doCreatedAt();
        doUpdatedAt();

        //TODO: need to invoke checkAttributes here too, and maybe rely on MetaModel for this.

        List<String> valueAttributes = getValueAttributeNames(false);

        List<Object> values = new ArrayList<Object>();
        for (String attribute : valueAttributes) {
            values.add(this.attributes.get(attribute));
        }
        String query = getMetaModelLocal().getDialect().createParametrizedInsert(getMetaModelLocal(), valueAttributes);
        try {
            Object id = new DB(getMetaModelLocal().getDbName()).execInsert(query, getMetaModelLocal().getIdName(), values.toArray());
            if(getMetaModelLocal().cached()){
                QueryCache.instance().purgeTableCache(getMetaModelLocal().getTableName());
            }

            attributes.put(getMetaModelLocal().getIdName(), id);

            fireAfterCreate(this);

            if(getMetaModelLocal().isVersioned()){
                set(getMetaModelLocal().getVersionColumn(), 1);
            }

            return true;
        } catch (DBException e) {
            throw e;
        } catch (Exception e) {
            throw new DBException(e.getMessage(), e);
        }
    }


    /**
     * This method will save a model as new. In other words, it will not try to guess if this is a
     * new record or a one that exists in the table. It does not have "belt and suspenders", it will
     * simply generate and execute insert statement, assuming that developer knows what he/she is doing.
     *
     * @return true if model was saved, false if not
     */
    public boolean insert() {

        fireBeforeCreate(this);
        doCreatedAt();
        doUpdatedAt();

        List<String> valueAttributes = getValueAttributeNames(true);

        List<Object> values = new ArrayList<Object>();
        for (String attribute : valueAttributes) {
            values.add(this.attributes.get(attribute));
        }
        String query = getMetaModelLocal().getDialect().createParametrizedInsertIdUnmanaged(getMetaModelLocal(), valueAttributes);
        try {
            long recordsUpdated = new DB(getMetaModelLocal().getDbName()).exec(query, values.toArray());
            if(getMetaModelLocal().cached()){
                QueryCache.instance().purgeTableCache(getMetaModelLocal().getTableName());
            }

            fireAfterCreate(this);

            if(getMetaModelLocal().isVersioned()){
                set(getMetaModelLocal().getVersionColumn(), 1);
            }

            return recordsUpdated == 1;
        } catch (DBException e) {
            throw e;
        } catch (Exception e) {
            throw new DBException(e.getMessage(), e);
        }
    }

    private void doCreatedAt() {
        if(getMetaModelLocal().hasAttribute("created_at") && manageTime){
            //clean just in case.
            attributes.remove("created_at");
            attributes.remove("CREATED_AT");
            attributes.put("created_at", new Timestamp(System.currentTimeMillis()));
        }
    }

    private void doUpdatedAt() {
        if(getMetaModelLocal().hasAttribute("updated_at") && manageTime){
            //clean just in case.
            attributes.remove("updated_at");
            attributes.remove("UPDATED_AT");
            set("updated_at", new Timestamp(System.currentTimeMillis()));
        }
    }

    private boolean update() {

        doUpdatedAt();

        MetaModel metaModel = getMetaModelLocal();
        String query = "UPDATE " + metaModel.getTableName() + " SET ";
        List<String> names = metaModel.getAttributeNamesSkipGenerated(manageTime);
        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i);
            query += name + "= ?";
            if (i < names.size() - 1) {
                query += ", ";
            }
        }

        List values = getAttributeValuesSkipGenerated(names);

        if(metaModel.hasAttribute("updated_at")){
            query += ", updated_at = ? ";
            values.add(get("updated_at"));
        }

        if(metaModel.isVersioned()){
            query += ", " + getMetaModelLocal().getVersionColumn() + " = ? ";
            values.add(getLong(getMetaModelLocal().getVersionColumn()) + 1);
        }
        query += " where " + metaModel.getIdName() + " = ?";
        query += metaModel.isVersioned()? " and " + getMetaModelLocal().getVersionColumn() + " = ?" :"";
        values.add(getId());
        if(metaModel.isVersioned()){
            values.add((get(getMetaModelLocal().getVersionColumn())));
        }
        int updated = new DB(metaModel.getDbName()).exec(query, values.toArray());
        if(metaModel.isVersioned() && updated == 0){
            throw new StaleModelException("Failed to update record for model '" + getClass() +
                    "', with " + getIdName() + " = " + getId() + " and " + getMetaModelLocal().getVersionColumn() + " = " + get(getMetaModelLocal().getVersionColumn()) +
                    ". Either this record does not exist anymore, or has been updated to have another record_version.");
        }else if(metaModel.isVersioned()){
            set(getMetaModelLocal().getVersionColumn(), getLong(getMetaModelLocal().getVersionColumn()) + 1);
        }
        if(metaModel.cached()){
            QueryCache.instance().purgeTableCache(metaModel.getTableName());
        }
        return updated > 0;
    }

    private List getAttributeValuesSkipGenerated(List<String> names) {
        List values = new ArrayList();
        for (String name : names) {
            values.add(get(name));
        }
        return values;
    }

    static <T extends Model> T instance(Map m, MetaModel metaModel) {
        try {
            T instance = (T) metaModel.getModelClass().newInstance();
            instance.setMetamodelLocal(metaModel);
            instance.hydrate(m);
            return instance;
        }
        catch(InstantiationException e){
            throw new InitException("Failed to create a new instance of: " + metaModel.getModelClass() + ", are you sure this class has a default constructor?");
        }
        catch(DBException e){
            throw e;
        }
        catch(InitException e){
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private static <T extends Model> Class<T> getDaClass() {
        try {
            if (Registry.instance().initialized()) {
                MetaModel mm = Registry.instance().getMetaModelByClassName(getClassName());
                return mm == null ? (Class<T>) Class.forName(getClassName()) : mm.getModelClass();
            } else {
                return (Class<T>) Class.forName(getClassName());
            }
        } catch (Exception e) {
            throw new DBException(e.getMessage(), e);
        }
    }

    private static String getClassName() {
        return new ClassGetter().getClassName();
    }

    public static String getTableName() {
        return Registry.instance().getTableName(getDaClass());
    }

    public Object getId() {
        return get(getMetaModelLocal().getIdName());
    }

    public String getIdName() {
        return getMetaModelLocal().getIdName();
    }

    protected void setChildren(Class childClass, List<Model> children) {
        cachedChildren.put(childClass, children);
    }

    /**
     * Turns off automatic management of time-related attributes <code>created_at</code> and <code>updated_at</code>.
     * If management of time attributes is turned off,
     *
     * @param manage if true, the attributes are managed by the model. If false, they are managed by developer.
     */
    public void manageTime(boolean manage) {
        this.manageTime = manage;
    }

    static class ClassGetter extends SecurityManager {
        public String getClassName() {
            Class[] classes = getClassContext();
            for (Class clazz : classes) {
                if (Model.class.isAssignableFrom(clazz) && clazz != null && !clazz.equals(Model.class)) {
                    return clazz.getName();
                }
            }
            throw new InitException("failed to determine Model class name, are you sure models have been instrumented?");
        }
    }

    /**
     * Generates INSERT SQL based on this model. Uses single quotes for all string values.
     * Example:
     * <pre>
     *
     * String insert = u.toInsert();
     * //yields this output:
     * //INSERT INTO users (id, first_name, email, last_name) VALUES (1, 'Marilyn', 'mmonroe@yahoo.com', 'Monroe');
     * </pre>
     *
     * @return INSERT SQL based on this model.
     */
    public String toInsert(){
        return toInsert("'", "'");
    }


    /**
     * Generates INSERT SQL based on this model.
     * For instance, for Oracle, the left quote is: "q'{" and the right quote is: "}'".
     * The output will also use single quotes for <code>java.sql.Timestamp</code> and <code>java.sql.Date</code> types.
     *
     * Example:
     * <pre>
     * String insert = u.toInsert("q'{", "}'");
     * //yields this output
     * //INSERT INTO users (id, first_name, email, last_name) VALUES (1, q'{Marilyn}', q'{mmonroe@yahoo.com}', q'{Monroe}');
     * </pre>
     * @param leftStringQuote - left quote for a string value, this can be different for different databases.
     * @param rightStringQuote - left quote for a string value, this can be different for different databases.
     * @return SQL INSERT string;
     */
    public String toInsert(String leftStringQuote, String rightStringQuote){
        return toInsert(new SimpleFormatter(java.sql.Date.class, "'", "'"),
                        new SimpleFormatter(Timestamp.class, "'", "'"),
                        new SimpleFormatter(String.class, leftStringQuote, rightStringQuote));
    }

    /**
     * TODO: write good JavaDoc, use code  inside method above
     *
     * @param formatters
     * @return
     */
    public String toInsert(Formatter... formatters){
        HashMap<Class, Formatter> formatterMap = new HashMap<Class, Formatter>();

        for(Formatter f: formatters){
            formatterMap.put(f.getValueClass(), f);
        }

        List<String> names = new ArrayList<String>(attributes.keySet());
        Collections.sort(names);
        List<Object> values = new ArrayList();

        for(String name: names){
            Object value = get(name);
            if(value == null){
                values.add("NULL");
            }
            else if (value instanceof String && !formatterMap.containsKey(String.class)){
                values.add("'" + value + "'");
            }else{
                if(formatterMap.containsKey(value.getClass())){
                    values.add(formatterMap.get(value.getClass()).format(value));
                }else{
                    values.add(value);
                }
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ").append(getMetaModelLocal().getTableName()).append(" (");
        join(sb, names, ", ");
        sb.append(") VALUES (");
        join(sb, values, ", ");
        sb.append(')');
        return sb.toString();
    }

    /**
     * Use to force-purge cache associated with this table. If this table is not cached, this method has no side effect.
     */
    public static void purgeCache(){
        MetaModel mm = getMetaModel();
        if(mm.cached()){
            QueryCache.instance().purgeTableCache(mm.getTableName());
        }
    }


    /**
     * Convenience method: converts ID value to Long and returns it.
     *
     * @return value of attribute corresponding to <code>getIdName()</code>, converted to Long.
     */
    public Long getLongId() {
        Object id = get(getIdName());
        if (id == null) {
            throw new NullPointerException(getIdName() + " is null, cannot convert to Long");
        }
        return Convert.toLong(id);
    }

    private static void purgeEdges(){
        ModelDelegate.purgeEdges(getMetaModel());
    }


    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(attributes);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        attributes = (Map<String, Object>) in.readObject();
    }
}

