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
import org.javalite.activejdbc.conversion.Converter;
import org.javalite.activejdbc.validation.NumericValidationBuilder;
import org.javalite.activejdbc.validation.ValidationBuilder;
import org.javalite.activejdbc.validation.ValidationException;
import org.javalite.activejdbc.validation.ValidationHelper;
import org.javalite.activejdbc.validation.Validator;
import org.javalite.common.Convert;
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
import org.javalite.activejdbc.conversion.BlankToNullConverter;
import org.javalite.activejdbc.conversion.ZeroToNullConverter;

import static org.javalite.common.Inflector.*;
import static org.javalite.common.Util.*;

/**
 * This class is a super class of all "models" and provides most functionality
 * necessary for implementation of Active Record pattern.
 */
public abstract class Model extends CallbackSupport implements Externalizable {

    private final static Logger logger = LoggerFactory.getLogger(Model.class);

    private SortedMap<String, Object> attributes = new CaseInsensitiveMap<Object>();
    private boolean frozen = false;
    private MetaModel metaModelLocal;
    private ModelMetaData metaDataLocal;
    private final Map<Class, Model> cachedParents = new HashMap<Class, Model>();
    private final Map<Class, List<Model>> cachedChildren = new HashMap<Class, List<Model>>();
    private boolean manageTime = true;

    private Errors errors = new Errors();

    protected Model() {
    }

    public static MetaModel getMetaModel() {
        return Registry.instance().getMetaModel(getDaClass());
    }

    private static ModelMetaData getMetaData() {
        return Registry.instance().getMetaData(getDaClass());
    }

    protected SortedMap<String, Object> getAttributes(){
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
    public <T extends Model> T fromMap(Map input) {
        hydrate(input);
        return (T) this;
    }



    /**
     * Hydrates a this instance of model from a map. Only picks values from a map that match
     * this instance's attribute names, while ignoring the others.
     *
     * @param attributesMap map containing values for this instance.
     */
    protected void hydrate(Map<String, Object> attributesMap) {
        for (String attrName : getMetaModelLocal().getAttributeNames()) {
            Object value = null;
            boolean contains = false;
            if (attributesMap.containsKey(attrName.toLowerCase())) {
                value = attributesMap.get(attrName.toLowerCase());
                contains = true;
            } else if (attributesMap.containsKey(attrName.toUpperCase())) {
                value = attributesMap.get(attrName.toUpperCase());
                contains = true;
            }
            if (contains) {
                if (value instanceof Clob && getMetaModelLocal().cached()) {
                    this.attributes.put(attrName, Convert.toString(value));
                }else {
                    this.attributes.put(attrName, getMetaModelLocal().getDialect().overrideDriverTypeConversion(
                            getMetaModelLocal(), attrName, value));
                }
            }
        }
    }


    /**
     * Convenience method, sets ID value on this model, equivalent to <code>set(getIdName(), id)</code>.
     *
     * @param id value of ID
     * @return reference to self for chaining.
     */
    public <T extends Model> T setId(Object id) {
        return set(getIdName(), id);
    }

    /**
     * Sets attribute value as <code>java.sql.Date</code>. If a suitable {@link Converter} from the <code>Class</code>
     * of value to <code>java.sql.Date</code> exists for the specified attribute, it will be used, otherwise performs
     * a conversion using {@link Convert#toSqlDate(Object)}.
     *
     * @param attribute name of attribute.
     * @param value value to convert.
     * @return reference to this model.
     */
    public <T extends Model> T setDate(String attribute, Object value) {
        Converter<Object, java.sql.Date> converter = getMetaDataLocal().getConverterForValue(
                attribute, value, java.sql.Date.class);
        return setConverted(attribute, converter != null ? converter.convert(value) : Convert.toSqlDate(value));
    }

    /**
     * Gets attribute value as <code>java.sql.Date</code>. If a suitable {@link Converter} from the <code>Class</code>
     * of the attribute value to <code>java.sql.Date</code> exists for the specified attribute, it will be used,
     * otherwise performs a conversion using {@link Convert#toSqlDate(Object)}.
     * @param attribute name of attribute to convert
     * @return value converted to <code>java.sql.Date</code>
     */
    public java.sql.Date getDate(String attribute) {
        Object value = getUnconverted(attribute);
        Converter<Object, java.sql.Date> converter = getMetaDataLocal().getConverterForValue(
                attribute, value, java.sql.Date.class);
        return converter != null ? converter.convert(value) : Convert.toSqlDate(value);
    }

    /**
     * Performs a primitive conversion of <code>java.util.Date</code> to <code>java.sql.Timestamp</code>
     * based on the time value.
     *
     * @param name name of field.
     * @param date date value.
     * @deprecated  use {@link #setTimestamp(String, Object)} instead.
     */
    @Deprecated
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
     * Sets a value of an attribute. If a suitable {@link Converter} from the <code>Class</code> of value to
     * <code>java.lang.Object</code> exists for the specified attribute, it will be used and the converted value
     * will be set.
     *
     * @param attribute name of attribute to set. Names not related to this model will be rejected (those not matching table columns).
     * @param value value of attribute. Feel free to set any type, as long as it can be accepted by your driver.
     * @return reference to self, so you can string these methods one after another.
     */
    public <T extends Model> T set(String attribute, Object value) {
        Converter<Object, Object> converter = getMetaDataLocal().getConverterForValue(attribute, value, Object.class);
        return setConverted(attribute, converter != null ? converter.convert(value) : value);
    }

    /**
     * Sets already converted value of an attribute.
     */
    private <T extends Model> T setConverted(String attribute, Object value) {
        if (manageTime && attribute.equalsIgnoreCase("created_at")) {
            throw new IllegalArgumentException("cannot set 'created_at'");
        }
        getMetaModelLocal().checkAttributeOrAssociation(attribute);

        attributes.put(attribute, value);
        return (T) this;
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
    //TODO: return SortedSet<String>, which keeps case insensivity, since this List<String> will not be case insensitive
    public static List<String> attributes(){
        List<String> list = new ArrayList<String>();
        for (String attribute : getMetaModel().getAttributeNames()) {
            list.add(attribute.toLowerCase());
        }
        return Collections.unmodifiableList(list);
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
                + " WHERE " + getIdName() + "= ?", getId())) {

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
     * This method does everything {@link #deleteCascade()} does, but in addition allows to exclude some associations
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
                logger.error("ActiveJDBC WARNING: failed to find a model class for: {}, maybe model is not defined for this table?"
                        + " There might be a risk of running into integrity constrain violation if this model is not defined.",
                        targetTableName);
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
                logger.error("ActiveJDBC WARNING: failed to find a model class for: {}, maybe model is not defined for this table?"
                        + " There might be a risk of running into integrity constrain violation if this model is not defined.",
                        targetTableName);
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
     * @return number of deleted records.
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
    public SortedMap<String, Object> toMap(){
        SortedMap<String, Object> retVal = new TreeMap<String, Object>();
        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
            Object v = entry.getValue();
            if (v == null) {
                continue;
            }
            if (v instanceof Clob) {
                retVal.put(entry.getKey().toLowerCase(), Convert.toString(v));
            } else {
                retVal.put(entry.getKey().toLowerCase(), v);
            }
        }
        for(Class parentClass: cachedParents.keySet()){
            retVal.put(underscore(parentClass.getSimpleName()), cachedParents.get(parentClass).toMap());
        }

        for(Class childClass: cachedChildren.keySet()){
            List<Model> children = cachedChildren.get(childClass);

            List<Map> childMaps = new ArrayList<Map>(children.size());
            for(Model child:children){
                childMaps.add(child.toMap());
            }
            retVal.put(tableize(childClass.getSimpleName()), childMaps);
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
            String topTag = underscore(getClass().getSimpleName());
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
    public String toXml(boolean pretty, boolean declaration, String... attrs) {
        StringBuilder sb = new StringBuilder();

        if(declaration) {
            sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            if (pretty) { sb.append('\n'); }
        }
        toXmlP(sb, pretty, "", attrs);
        return sb.toString();
    }

    protected void toXmlP(StringBuilder sb, boolean pretty, String indent, String ... attrs) {

        String topTag = underscore(getClass().getSimpleName());
        if (pretty) { sb.append(indent); }
        sb.append('<').append(topTag).append('>');
        if (pretty) { sb.append('\n'); }

        Collection<String> attrList = (attrs != null && attrs.length > 0) ? Arrays.asList(attrs) : attributes.keySet();
        for (String name : attrList) {
            if (pretty) { sb.append("  ").append(indent); }
            name = name.toLowerCase(); // force output attribute names in lowercase
            sb.append('<').append(name).append('>');
            Object v = attributes.get(name);
            if (v != null) {
                sb.append(XmlEntities.XML.escape(Convert.toString(v)));
            }
            sb.append("</").append(name).append('>');
            if (pretty) { sb.append('\n'); }
        }
        for (Class childClass : cachedChildren.keySet()) {
            if (pretty) { sb.append("  ").append(indent); }
            String tag = pluralize(underscore(childClass.getSimpleName()));
            sb.append('<').append(tag).append('>');
            if (pretty) { sb.append('\n'); }
            for (Model child : cachedChildren.get(childClass)) {
                child.toXmlP(sb, pretty, "    " + indent);
            }
            if (pretty) { sb.append("  ").append(indent); }
            sb.append("</").append(tag).append('>');
            if (pretty) { sb.append('\n'); }
        }
        beforeClosingTag(sb, pretty, pretty ? "  " + indent : "", attrs);
        if (pretty) { sb.append(indent); }
        sb.append("</").append(topTag).append('>');
        if (pretty) { sb.append('\n'); }
    }

    /**
     * Generates a XML document from content of this model.
     *
     * @param spaces by how many spaces to indent.
     * @param declaration true to include XML declaration at the top
     * @param attrs list of attributes to include. No arguments == include all attributes.
     * @return generated XML.
     *
     * @deprecated use {@link #toXml(boolean, boolean, String...)} instead
     */
    @Deprecated
    public String toXml(int spaces, boolean declaration, String... attrs){
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
     * @deprecated use {@link #beforeClosingTag(StringBuilder, boolean, String, String...)} instead
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

    private static final DateFormat isoDateTimeFormater;
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
            name = name.toLowerCase(); // force output attribute names in lowercase
            sbAttrs.append('"').append(name).append("\":");
            Object v = attributes.get(name);
            if (v == null) {
                sbAttrs.append("null");
            } else if (v instanceof Number || v instanceof Boolean) {
                sbAttrs.append(v);
            } else if (v instanceof Date) {
                sbAttrs.append('"').append(isoDateTimeFormater.format((Date) v)).append('"');
            } else {
                sbAttrs.append('"').append(Convert.toString(v)
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
                String name = pluralize(childClass.getSimpleName()).toLowerCase();
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
     * @deprecated use {@link #beforeClosingBrace(StringBuilder, boolean, String, String...)} instead
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
    public <P extends Model> P parent(Class<P> parentClass) {
        return parent(parentClass, false);
    }

    public <P extends Model> P parent(Class<P> parentClass, boolean cache) {
        P cachedParent = parentClass.cast(cachedParents.get(parentClass));
        if (cachedParent != null) {
            return cachedParent;
        }
        MetaModel parentMM = Registry.instance().getMetaModel(parentClass);
        String parentTable = parentMM.getTableName();

        BelongsToAssociation ass = getMetaModelLocal().getAssociationForTarget(parentTable, BelongsToAssociation.class);
        BelongsToPolymorphicAssociation assP = getMetaModelLocal().getAssociationForTarget(
                parentTable, BelongsToPolymorphicAssociation.class);

        Object fkValue;
        String fkName;
        if (ass != null) {
            fkValue = get(ass.getFkName());
            fkName = ass.getFkName();
        } else if (assP != null) {
            fkValue = get("parent_id");
            fkName = "parent_id";

            if (!assP.getTypeLabel().equals(getString("parent_type"))) {
                throw new IllegalArgumentException("Wrong parent: '" + parentClass + "'. Actual parent type label of this record is: '" + getString("parent_type") + "'");
            }
        } else {
            throw new IllegalArgumentException("there is no association with table: " + parentTable);
        }

        if (fkValue == null) {
            logger.debug("Attribute: {} is null, cannot determine parent. Child record: {}", fkName, this);
            return null;
        }
        String parentIdName = parentMM.getIdName();
        String query = getMetaModelLocal().getDialect().selectStarParametrized(parentTable, parentIdName);

        if (parentMM.cached()) {
            P parent = parentClass.cast(QueryCache.instance().getItem(parentTable, query, new Object[]{fkValue}));
            if (parent != null) {
                return parent;
            }
        }

        List<Map> results = new DB(getMetaModelLocal().getDbName()).findAll(query, fkValue);
        //expect only one result here
        if (results.isEmpty()) { //this should be covered by referential integrity constraint
            return null;
        } else {
            try {
                P parent = parentClass.newInstance();
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
    public void copyTo(Model other) {
        if (!getMetaModelLocal().getTableName().equals(other.getMetaModelLocal().getTableName())) {
            throw new IllegalArgumentException("can only copy between the same types");
        }

        for (String name : getMetaModelLocal().getAttributeNamesSkipId()) {
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
    protected MetaModel getMetaModelLocal() {
        if(metaModelLocal == null) {
            metaModelLocal = getMetaModel();
        }
        return metaModelLocal;
    }


    protected void setMetamodelLocal(MetaModel metamodelLocal){
        this.metaModelLocal = metamodelLocal;
    }

    protected ModelMetaData getMetaDataLocal(){
        if (metaDataLocal == null) {
            metaDataLocal = getMetaData();
        }
        return metaDataLocal;
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
     * Returns a value for attribute. If a suitable {@link Converter} from the <code>Class</code> of the
     * attribute value to <code>java.lang.Object</code> exists for the specified attribute, it will be used.
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
     * </p>
     *
     * @param attribute name of attribute of name or related object.
     * @return value for attribute.
     */
    public Object get(String attribute) {
        if (frozen) { throw new FrozenException(this); }

        if (attribute == null) { throw new IllegalArgumentException("attribute cannot be null"); }

        // NOTE: this is a workaround for JSP pages. JSTL in cases ${item.id} does not call the getId() method, instead
        // calls item.get("id"), considering that this is a map only!
        if (attribute.equalsIgnoreCase("id") && !attributes.containsKey("id")) {
            return attributes.get(getIdName());
        }

        if (getMetaModelLocal().hasAttribute(attribute)) {
            Object value = attributes.get(attribute);
            Converter<Object, Object> converter = getMetaDataLocal().getConverterForValue(attribute, value, Object.class);
            return converter != null ? converter.convert(value) : value;
        } else {
            String getInferenceProperty = System.getProperty("activejdbc.get.inference");
            if (getInferenceProperty == null || getInferenceProperty.equals("true")) {
                Object returnValue;
                if ((returnValue = tryParent(attribute)) != null) {
                    return returnValue;
                } else if ((returnValue = tryPolymorphicParent(attribute)) != null) {
                    return returnValue;
                } else if ((returnValue = tryChildren(attribute)) != null) {
                    return returnValue;
                } else if ((returnValue = tryPolymorphicChildren(attribute)) != null) {
                    return returnValue;
                } else if ((returnValue = tryOther(attribute)) != null) {
                    return returnValue;
                } else {
                    getMetaModelLocal().checkAttributeOrAssociation(attribute);
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * Gets unconverted value of the attribute.
     */
    public Object getUnconverted(String attribute) {
        if(frozen) throw new FrozenException(this);

        if(attribute == null) throw new IllegalArgumentException("attribute cannot be null");

        return attributes.get(attribute);//this should account for nulls too!
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
     * Gets attribute value as <code>String</code>. If a suitable {@link Converter} from the <code>Class</code>
     * of the attribute value to <code>java.lang.String</code> exists for the specified attribute, it will be used,
     * otherwise performs a conversion using {@link Convert#toString(Object)}.
     *
     * @param attribute name of attribute to convert
     * @return value converted to <code>String</code>
     */
    public String getString(String attribute) {
        Object value = getUnconverted(attribute);
        Converter<Object, String> converter = getMetaDataLocal().getConverterForValue(attribute, value, String.class);
        return converter != null ? converter.convert(value) : Convert.toString(value);
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
    //TODO: use converters here?
    public byte[] getBytes(String attribute) {
        Object value = get(attribute);
        return Convert.toBytes(value);
    }

    /**
     * Gets attribute value as <code>java.math.BigDecimal</code>. If a suitable {@link Converter} from the
     * <code>Class</code> of the attribute value to <code>java.math.BigDecimal</code> exists for the specified
     * attribute, it will be used, otherwise performs a conversion using {@link Convert#toBigDecimal(Object)}.
     * @param attribute name of attribute to convert
     * @return value converted to <code>java.math.BigDecimal</code>
     */
    public BigDecimal getBigDecimal(String attribute) {
        Object value = getUnconverted(attribute);
        Converter<Object, BigDecimal> converter = getMetaDataLocal().getConverterForValue(
                attribute, value, BigDecimal.class);
        return converter != null ? converter.convert(value) : Convert.toBigDecimal(value);
    }

    /**
     * Gets attribute value as <code>Integer</code>. If a suitable {@link Converter} from the
     * <code>Class</code> of the attribute value to <code>java.lang.Integer</code> exists for the specified
     * attribute, it will be used, otherwise performs a conversion using {@link Convert#toInteger(Object)}.
     * @param attribute name of attribute to convert
     * @return value converted to <code>Integer</code>
     */
    public Integer getInteger(String attribute) {
        Object value = getUnconverted(attribute);
        Converter<Object, Integer> converter = getMetaDataLocal().getConverterForValue(attribute, value, Integer.class);
        return converter != null ? converter.convert(value) : Convert.toInteger(value);
    }

    /**
     * Gets attribute value as <code>Long</code>. If a suitable {@link Converter} from the
     * <code>Class</code> of the attribute value to <code>java.lang.Long</code> exists for the specified
     * attribute, it will be used, otherwise performs a conversion using {@link Convert#toLong(Object)}.
     * @param attribute name of attribute to convert
     * @return value converted to <code>Long</code>
     */
    public Long getLong(String attribute) {
        Object value = getUnconverted(attribute);
        Converter<Object, Long> converter = getMetaDataLocal().getConverterForValue(attribute, value, Long.class);
        return converter != null ? converter.convert(value) : Convert.toLong(value);
    }

    /**
     * Gets attribute value as <code>Short</code>. If a suitable {@link Converter} from the
     * <code>Class</code> of the attribute value to <code>java.lang.Short</code> exists for the specified
     * attribute, it will be used, otherwise performs a conversion using {@link Convert#toShort(Object)}.
     * @param attribute name of attribute to convert
     * @return value converted to <code>Short</code>
     */
    public Short getShort(String attribute) {
        Object value = getUnconverted(attribute);
        Converter<Object, Short> converter = getMetaDataLocal().getConverterForValue(attribute, value, Short.class);
        return converter != null ? converter.convert(value) : Convert.toShort(value);
    }

    /**
     * Gets attribute value as <code>Float</code>. If a suitable {@link Converter} from the
     * <code>Class</code> of the attribute value to <code>java.lang.Float</code> exists for the specified
     * attribute, it will be used, otherwise performs a conversion using {@link Convert#toFloat(Object)}.
     * @param attribute name of attribute to convert
     * @return value converted to <code>Float</code>
     */
    public Float getFloat(String attribute) {
        Object value = getUnconverted(attribute);
        Converter<Object, Float> converter = getMetaDataLocal().getConverterForValue(attribute, value, Float.class);
        return converter != null ? converter.convert(value) : Convert.toFloat(value);
    }

    /**
     * Gets attribute value as <code>java.sql.Timestamp</code>. If a suitable {@link Converter} from the
     * <code>Class</code> of the attribute value to <code>java.sql.Timestamp</code> exists for the specified attribute,
     * it will be used, otherwise performs a conversion using {@link Convert#toTimestamp(Object)}.
     *
     * @param attribute name of attribute to convert
     * @return instance of <code>Timestamp</code>
     */
    public Timestamp getTimestamp(String attribute) {
        Object value = getUnconverted(attribute);
        Converter<Object, Timestamp> converter = getMetaDataLocal().getConverterForValue(
                attribute, value, Timestamp.class);
        return converter != null ? converter.convert(value) : Convert.toTimestamp(get(attribute));
    }

    /**
     * Gets attribute value as <code>Double</code>. If a suitable {@link Converter} from the
     * <code>Class</code> of the attribute value to <code>java.lang.Double</code> exists for the specified
     * attribute, it will be used, otherwise performs a conversion using {@link Convert#toDouble(Object)}.
     * @param attribute name of attribute to convert
     * @return value converted to <code>Double</code>
     */
    public Double getDouble(String attribute) {
        Object value = getUnconverted(attribute);
        Converter<Object, Double> converter = getMetaDataLocal().getConverterForValue(attribute, value, Double.class);
        return converter != null ? converter.convert(value) : Convert.toDouble(value);
    }

    /**
     * Gets attribute value as <code>Boolean</code>. If a suitable {@link Converter} from the
     * <code>Class</code> of the attribute value to <code>java.lang.Boolean</code> exists for the specified
     * attribute, it will be used, otherwise performs a conversion using {@link Convert#toBoolean(Object)}.
     * @param attribute name of attribute to convert
     * @return value converted to <code>Boolean</code>
     */
    public Boolean getBoolean(String attribute) {
        Object value = getUnconverted(attribute);
        Converter<Object, Boolean> converter = getMetaDataLocal().getConverterForValue(attribute, value, Boolean.class);
        return converter != null ? converter.convert(value) : Convert.toBoolean(value);
    }

    /*************************** typed setters *****************************************/

    /**
     * Sets attribute value as <code>String</code>. If a suitable {@link Converter} from the
     * <code>Class</code> of value to <code>java.lang.String</code> exists for the specified attribute, it will be
     * used, otherwise performs a conversion using {@link Convert#toString(Object)}.
     *
     * @param attribute name of attribute.
     * @param value value
     * @return reference to this model.
     */
    public <T extends Model> T setString(String attribute, Object value) {
        Converter<Object, String> converter = getMetaDataLocal().getConverterForValue(attribute, value, String.class);
        return setConverted(attribute, converter != null ? converter.convert(value) : Convert.toString(value));
    }

    /**
     * Sets attribute value as <code>java.math.BigDecimal</code>. If a suitable {@link Converter} from the
     * <code>Class</code> of value to <code>java.math.BigDecimal</code> exists for the specified attribute, it will be
     * used, otherwise performs a conversion using {@link Convert#toBigDecimal(Object)}.
     *
     * @param attribute name of attribute.
     * @param value value
     * @return reference to this model.
     */
    public <T extends Model> T setBigDecimal(String attribute, Object value) {
        Converter<Object, BigDecimal> converter = getMetaDataLocal().getConverterForValue(
                attribute, value, BigDecimal.class);
        return setConverted(attribute, converter != null ? converter.convert(value) : Convert.toBigDecimal(value));
    }

    /**
     * Sets attribute value as <code>Short</code>. If a suitable {@link Converter} from the
     * <code>Class</code> of value to <code>java.lang.Short</code> exists for the specified attribute, it will be
     * used, otherwise performs a conversion using {@link Convert#toShort(Object)}.
     *
     * @param attribute name of attribute.
     * @param value value
     * @return reference to this model.
     */
    public <T extends Model> T setShort(String attribute, Object value) {
        Converter<Object, Short> converter = getMetaDataLocal().getConverterForValue(attribute, value, Short.class);
        return setConverted(attribute, converter != null ? converter.convert(value) : Convert.toShort(value));
    }

    /**
     * Converts object to <code>Integer</code> when setting.
     *
     * @param attribute name of attribute.
     * @param value value
     * @return reference to this model.
     */
    public <T extends Model> T setInteger(String attribute, Object value) {
        Converter<Object, Integer> converter = getMetaDataLocal().getConverterForValue(attribute, value, Integer.class);
        return setConverted(attribute, converter != null ? converter.convert(value) : Convert.toInteger(value));
    }

    /**
     * Converts object to <code>Long</code> when setting.
     *
     * @param attribute name of attribute.
     * @param value value
     * @return reference to this model.
     */
    public <T extends Model> T setLong(String attribute, Object value) {
        Converter<Object, Long> converter = getMetaDataLocal().getConverterForValue(attribute, value, Long.class);
        return setConverted(attribute, converter != null ? converter.convert(value) : Convert.toLong(value));
    }

    /**
     * Sets attribute value as <code>Float</code>. If a suitable {@link Converter} from the <code>Class</code>
     * of value to <code>java.lang.Float</code> exists for the specified attribute, it will be used, otherwise performs
     * a conversion using {@link Convert#toFloat(Object)}.
     *
     * @param attribute name of attribute.
     * @param value value to convert.
     * @return reference to this model.
     */
    public <T extends Model> T setFloat(String attribute, Object value) {
        Converter<Object, Float> converter = getMetaDataLocal().getConverterForValue(attribute, value, Float.class);
        return setConverted(attribute, converter != null ? converter.convert(value) : Convert.toFloat(value));
    }

    /**
     * Sets attribute value as <code>java.sql.Timestamp</code>. If a suitable {@link Converter} from the
     * <code>Class</code> of value to <code>java.sql.Timestamp</code> exists for the specified attribute, it will be
     * used, otherwise performs a conversion using {@link Convert#toTimestamp(Object)}.
     *
     * @param attribute name of attribute.
     * @param value value
     * @return reference to this model.
     */
    public <T extends Model> T setTimestamp(String attribute, Object value) {
        Converter<Object, Timestamp> converter = getMetaDataLocal().getConverterForValue(
                attribute, value, Timestamp.class);
        return setConverted(attribute, converter != null ? converter.convert(value) : Convert.toTimestamp(value));
    }

    /**
     * Sets attribute value as <code>Double</code>. If a suitable {@link Converter} from the <code>Class</code>
     * of value to <code>java.lang.Double</code> exists for the specified attribute, it will be used, otherwise performs
     * a conversion using {@link Convert#toDouble(Object)}.
     *
     * @param attribute name of attribute.
     * @param value value to convert.
     * @return reference to this model.
     */
    public <T extends Model> T setDouble(String attribute, Object value) {
        Converter<Object, Double> converter = getMetaDataLocal().getConverterForValue(attribute, value, Double.class);
        return setConverted(attribute, converter != null ? converter.convert(value) : Convert.toDouble(value));
    }

    /**
     * Sets attribute value as <code>Boolean</code>. If a suitable {@link Converter} from the
     * <code>Class</code> of value to <code>java.lang.Boolean</code> exists for the specified attribute, it will be
     * used, otherwise performs a conversion using {@link Convert#toBoolean(Object)}.
     *
     * @param attribute name of attribute.
     * @param value value
     * @return reference to this model.
     */
    public <T extends Model> T setBoolean(String attribute, Object value) {
        Converter<Object, Boolean> converter = getMetaDataLocal().getConverterForValue(attribute, value, Boolean.class);
        return setConverted(attribute, converter != null ? converter.convert(value) : Convert.toBoolean(value));
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

    public <C extends Model> LazyList<C> getAll(Class<C> clazz) {
        List<Model> children = cachedChildren.get(clazz);
        if(children != null){
            return (LazyList<C>) children;
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
    public <C extends Model> LazyList<C> get(Class<C> clazz, String query, Object ... params){
        return get(Registry.instance().getTableName(clazz), query, params);
    }

    private <C extends Model> LazyList<C> get(String targetTable, String criteria, Object ...params) {
        //TODO: interesting thought: is it possible to have two associations of the same name, one to many and many to many? For now, say no.

        OneToManyAssociation oneToManyAssociation = getMetaModelLocal().getAssociationForTarget(
                targetTable, OneToManyAssociation.class);
        Many2ManyAssociation manyToManyAssociation = getMetaModelLocal().getAssociationForTarget(
                targetTable, Many2ManyAssociation.class);
        OneToManyPolymorphicAssociation oneToManyPolymorphicAssociation = getMetaModelLocal().getAssociationForTarget(
                targetTable, OneToManyPolymorphicAssociation.class);

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
            return new LazyList<C>(true, Registry.instance().getMetaModel(targetTable), query, allParams);
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
        return new LazyList<C>(subQuery, Registry.instance().getMetaModel(targetTable), allParams);
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
        Registry.instance().removeValidator(Model.getDaClass(), validator);
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
     * @deprecated use {@link #convertWith(org.javalite.activejdbc.conversion.Converter, String...)} instead
     */
    @Deprecated
    protected static ValidationBuilder convertWith(org.javalite.activejdbc.validation.Converter converter) {
        return addValidator(converter);
    }

    /**
     * Registers a custom converter for the specified attributes.
     *
     * @param converter custom converter
     * @param attributes attribute names
     */
    protected static void convertWith(Converter converter, String... attributes) {
        getMetaData().convertWith(converter, attributes);
    }

    /**
     * Converts a named attribute to <code>java.sql.Date</code> if possible.
     * Acts as a validator if cannot make a conversion.
     *
     * @param attributeName name of attribute to convert to <code>java.sql.Date</code>.
     * @param format format for conversion. Refer to {@link java.text.SimpleDateFormat}
     * @return message passing for custom validation message.
     * @deprecated use {@link #dateFormat(String, String...) instead
     */
    @Deprecated
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
     * @deprecated use {@link #timestampFormat(String, String...) instead
     */
    @Deprecated
    protected static ValidationBuilder convertTimestamp(String attributeName, String format){
        return ValidationHelper.addTimestampConverter(getClassName(), attributeName, format);
    }

    /**
     * Registers date format for specified attributes. This format will be used to convert between
     * Date -> String -> java.sql.Date when using the appropriate getters and setters.
     *
     * <p>For example:
     * <blockquote><pre>
     * public class Person extends Model {
     *     static {
     *         dateFormat("MM/dd/yyyy", "dob");
     *     }
     * }
     *
     * Person p = new Person();
     * // will convert String -> java.sql.Date
     * p.setDate("dob", "02/29/2000");
     * // will convert Date -> String, if dob value in model is of type Date
     * String str = p.getString("dob");
     *
     * // will convert Date -> String
     * p.setString("dob", new Date());
     * // will convert String -> java.sql.Date, if dob value in model is of type String
     * Date date = p.getDate("dob");
     * </pre></blockquote>
     *
     * @param pattern pattern to use for conversion
     * @param attributes attribute names
     */
    protected static void dateFormat(String pattern, String... attributes) {
        getMetaData().dateFormat(pattern, attributes);
    }

    /**
     * Registers date format for specified attributes. This format will be used to convert between
     * Date -> String -> java.sql.Date when using the appropriate getters and setters.
     *
     * <p>See example in {@link #dateFormat(String, String...)}.
     *
     * @param format format to use for conversion
     * @param attributes attribute names
     */
    protected static void dateFormat(DateFormat format, String... attributes) {
        getMetaData().dateFormat(format, attributes);
    }

    /**
     * Registers date format for specified attributes. This format will be used to convert between
     * Date -> String -> java.sql.Timestamp when using the appropriate getters and setters.
     *
     * <p>For example:
     * <blockquote><pre>
     * public class Person extends Model {
     *     static {
     *         timestampFormat("MM/dd/yyyy hh:mm a", "birth_datetime");
     *     }
     * }
     *
     * Person p = new Person();
     * // will convert String -> java.sql.Timestamp
     * p.setTimestamp("birth_datetime", "02/29/2000 12:07 PM");
     * // will convert Date -> String, if dob value in model is of type Date or java.sql.Timestamp
     * String str = p.getString("birth_datetime");
     *
     * // will convert Date -> String
     * p.setString("birth_datetime", new Date());
     * // will convert String -> java.sql.Timestamp, if dob value in model is of type String
     * Timestamp ts = p.getTimestamp("birth_datetime");
     * </pre></blockquote>
     *
     * @param pattern pattern to use for conversion
     * @param attributes attribute names
     */
    protected static void timestampFormat(String pattern, String... attributes) {
        getMetaData().timestampFormat(pattern, attributes);
    }

    /**
     * Registers date format for specified attributes. This format will be used to convert between
     * Date -> String -> java.sql.Timestamp when using the appropriate getters and setters.
     *
     * <p>See example in {@link #timestampFormat(String, String...)}.
     *
     * @param format format to use for conversion
     * @param attributes attribute names
     */
    protected static void timestampFormat(DateFormat format, String... attributes) {
        getMetaData().timestampFormat(format, attributes);
    }

    /**
     * Registers {@link BlankToNullConverter} for specified attributes. This will convert instances of <tt>String</tt>
     * that are empty or contain only whitespaces to <tt>null</tt>.
     *
     * @param attributes attribute names
     */
    protected static void blankToNull(String... attributes) {
        getMetaData().convertWith(BlankToNullConverter.instance(), attributes);
    }

    /**
     * Registers {@link ZeroToNullConverter} for specified attributes. This will convert instances of <tt>Number</tt>
     * that are zero to <tt>null</tt>.
     *
     * @param attributes attribute names
     */
    protected static void zeroToNull(String... attributes) {
        getMetaData().convertWith(ZeroToNullConverter.instance(), attributes);
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
    public <T extends Model> T set(Object ... namesAndValues){
        ModelDelegate.setNamesAndValues(this, namesAndValues);
        return (T) this;
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

        if (subquery.trim().equals("*")) {
            if (empty(params)) {
                return findAll();
            } else {
                throw new IllegalArgumentException(
                        "cannot provide parameters with query: '*', use findAll() method instead");
            }
        }
        return new LazyList(subquery, getMetaModel(), params);
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
    @Deprecated
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
    public static <T extends Model> LazyList<T> findAll() {
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

        if(child == null) throw new IllegalArgumentException("cannot add what is null");

        //TODO: refactor this method
        String childTable = Registry.instance().getTableName(child.getClass());
        MetaModel metaModel = getMetaModelLocal();
        if (getId() != null) {

            if (metaModel.hasAssociation(childTable, OneToManyAssociation.class)) {
                OneToManyAssociation ass = metaModel.getAssociationForTarget(childTable, OneToManyAssociation.class);
                String fkName = ass.getFkName();
                child.set(fkName, getId());
                child.saveIt();//this will cause an exception in case validations fail.
            }else if(metaModel.hasAssociation(childTable, Many2ManyAssociation.class)){
                Many2ManyAssociation ass = metaModel.getAssociationForTarget(childTable, Many2ManyAssociation.class);
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

                OneToManyPolymorphicAssociation ass = metaModel.getAssociationForTarget(
                        childTable, OneToManyPolymorphicAssociation.class);
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

        if (getId() != null) {
            String childTable = Registry.instance().getTableName(child.getClass());
            MetaModel metaModel = getMetaModelLocal();
            if (metaModel.hasAssociation(childTable, OneToManyAssociation.class)
                    || metaModel.hasAssociation(childTable, OneToManyPolymorphicAssociation.class)) {
                child.delete();
            }else if(metaModel.hasAssociation(childTable, Many2ManyAssociation.class)){
                Many2ManyAssociation ass = metaModel.getAssociationForTarget(childTable, Many2ManyAssociation.class);
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
        attributes = new CaseInsensitiveMap<Object>();
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
        attributes.put(getIdName(), null);
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
        if (getId() == null) {
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
            if (!name.equalsIgnoreCase(getMetaModelLocal().getVersionColumn())) {
                if (includeId || !name.equalsIgnoreCase(getIdName())) {
                    attributeNames.add(name);
                }
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
            Object id = new DB(getMetaModelLocal().getDbName()).execInsert(query, getIdName(), values.toArray());
            if(getMetaModelLocal().cached()){
                QueryCache.instance().purgeTableCache(getMetaModelLocal().getTableName());
            }

            attributes.put(getIdName(), id);

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
        if (manageTime && getMetaModelLocal().hasAttribute("created_at")) {
            attributes.put("created_at", new Timestamp(System.currentTimeMillis()));
        }
    }

    private void doUpdatedAt() {
        if (manageTime && getMetaModelLocal().hasAttribute("updated_at")) {
            set("updated_at", new Timestamp(System.currentTimeMillis()));
        }
    }

    private boolean update() {

        doUpdatedAt();

        MetaModel metaModel = getMetaModelLocal();
        StringBuilder query = new StringBuilder().append("UPDATE ").append(metaModel.getTableName()).append(" SET ");
        SortedSet<String> attributeNames = metaModel.getAttributeNamesSkipGenerated(manageTime);
        join(query, attributeNames, " = ?, ");
        query.append(" = ?");

        List<Object> values = getAttributeValues(attributeNames);

        if (manageTime && metaModel.hasAttribute("updated_at")) {
            query.append(", updated_at = ?");
            values.add(get("updated_at"));
        }

        if(metaModel.isVersioned()){
            query.append(", ").append(getMetaModelLocal().getVersionColumn()).append(" = ?");
            values.add(getLong(getMetaModelLocal().getVersionColumn()) + 1);
        }
        query.append(" WHERE ").append(metaModel.getIdName()).append(" = ?");
        values.add(getId());
        if (metaModel.isVersioned()) {
            query.append(" AND ").append(getMetaModelLocal().getVersionColumn()).append(" = ?");
            values.add(get(getMetaModelLocal().getVersionColumn()));
        }
        int updated = new DB(metaModel.getDbName()).exec(query.toString(), values.toArray());
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

    private List<Object> getAttributeValues(SortedSet<String> attributeNames) {
        List<Object> values = new ArrayList<Object>();
        for (String attribute : attributeNames) {
            values.add(get(attribute));
        }
        return values;
    }

    static <T extends Model> T instance(Map<String, Object> m, MetaModel metaModel) {
        try {
            T instance = (T) metaModel.getModelClass().newInstance();
            instance.setMetamodelLocal(metaModel);
            instance.hydrate(m);
            return instance;
        } catch(InstantiationException e) {
            throw new InitException("Failed to create a new instance of: " + metaModel.getModelClass() + ", are you sure this class has a default constructor?");
        } catch(DBException e) {
            throw e;
        } catch(InitException e) {
            throw e;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private static Class<? extends Model> getDaClass() {
        try {
            if (Registry.instance().initialized()) {
                MetaModel mm = Registry.instance().getMetaModelByClassName(getClassName());
                return mm == null ? (Class<? extends Model>) Class.forName(getClassName()) : mm.getModelClass();
            } else {
                return (Class<? extends Model>) Class.forName(getClassName());
            }
        } catch (ClassNotFoundException e) {
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
        return get(getIdName());
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

        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ").append(getMetaModelLocal().getTableName()).append(" (");
        join(sb, attributes.keySet(), ", ");
        sb.append(") VALUES (");
        Iterator<Object> it = attributes.values().iterator();
        while (it.hasNext()) {
            Object value = it.next();
            if (value == null) {
                sb.append("NULL");
            }
            else if (value instanceof String && !formatterMap.containsKey(String.class)){
                sb.append('\'').append(value).append('\'');
            }else{
                if(formatterMap.containsKey(value.getClass())){
                    sb.append(formatterMap.get(value.getClass()).format(value));
                }else{
                    sb.append(value);
                }
            }
            if (it.hasNext()) {
                sb.append(", ");
            }
        }
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
        Object id = getId();
        if (id == null) {
            throw new NullPointerException(getIdName() + " is null, cannot convert to Long");
        }
        return Convert.toLong(id);
    }

    private static void purgeEdges(){
        ModelDelegate.purgeEdges(getMetaModel());
    }


    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(attributes);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        attributes = new CaseInsensitiveMap<Object>();
        attributes.putAll((Map<String, Object>) in.readObject());
    }
}
