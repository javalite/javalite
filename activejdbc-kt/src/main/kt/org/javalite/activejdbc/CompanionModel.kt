/*
Copyright 2018 Davy Claisse

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
package org.javalite.activejdbc

import org.javalite.activejdbc.ModelDelegate.metaModelOf
import org.javalite.activejdbc.annotations.Cached
import org.javalite.activejdbc.conversion.Converter
import org.javalite.activejdbc.validation.NumericValidationBuilder
import org.javalite.activejdbc.validation.ValidationBuilder
import org.javalite.activejdbc.validation.Validator
import java.text.DateFormat
import kotlin.reflect.KClass

/**
 * How to use this class ?
 *
 * Consider this table:
 *
 *    CREATE TABLE people (
 *          id  int(11) NOT NULL auto_increment PRIMARY KEY,
 *          first_name VARCHAR(56) NOT NULL,
 *          last_name VARCHAR(56),
 *          created_at DATETIME,
 *          updated_at DATETIME
 *    );
 *
 * The related Java code is:
 *
 *    public class Person extends Model {}
 *
 * The equivalent in Kotlin is:
 *
 *    open class Person():Model() {
 *        companion object:CompanionModel<Person>(Person::class.java)
 *    }
 *
 * Because Kotlin does not allow static method inheritance, we have to define a companion object as CompanionModel. This
 * second model requires the class of type of the entity to manage. This code is a little bit more verbose than the Java
 * one but allow a full Kotlin project with JavaLite instead of a mixed language project.
 */
open class CompanionModel<T:Model>(entityClass:Class<T>) {

    constructor(entityClass:KClass<T>):this(entityClass.java)

    private val clazz:Class<T> = entityClass


    /**
     * Adds a validator to the model.
     *
     * @param validator new validator.
     */
    fun addValidator(validator:Validator):ValidationBuilder<Validator> {
        return ModelDelegate.validateWith(modelClass(), validator)
    }


    /**
     * Returns all associations of this model.
     * @return all associations of this model.
     */
    fun associations():MutableList<Association?> {
        return ModelDelegate.associations(modelClass())
    }


    /**
     * Returns names of all attributes from this model.
     * @return names of all attributes from this model.
     */
    fun attributeNames():MutableSet<String> {
        return ModelDelegate.attributeNames(modelClass())
    }


    fun <M> belongsTo(targetClass:Class<out M>):Boolean where M:Model {
        return ModelDelegate.belongsTo(modelClass(), targetClass)
    }


    /**
     * Sets  lifecycle listeners on current model. All previous listeners will be unregistered.
     *
     * @param listeners list of lifecycle listeners
     */
    fun <M> callbackWith(vararg listeners:CallbackListener<M>) where M:Model {
        ModelDelegate.callbackWith(modelClass(), *listeners)
    }


    /**
     * Returns total count of records in table.
     *
     * @return total count of records in table.
     */
    fun count():Long {
        return ModelDelegate.count(modelClass())
    }


    /**
     * Returns count of records in table under a condition.
     *
     * @param query query to select records to count.
     * @param params parameters (if any) for the query.
     * @return count of records in table under a condition.
     */
    fun count(query:String, vararg params:Any?):Long {
        return ModelDelegate.count(modelClass(), query, *params)
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
    fun create(vararg namesAndValues:Any?):T {
        return ModelDelegate.create(modelClass(), *namesAndValues)
    }


    /**
     * This is a convenience method to {@link #create(Object...)}. It will create a new model and will save it
     * to DB. It has the same semantics as {@link #saveIt()}.
     *
     * @param namesAndValues names and values. elements at indexes 0, 2, 4, 8... are attribute names, and elements at
     * indexes 1, 3, 5... are values. Element at index 1 is a value for attribute at index 0 and so on.
     * @return newly instantiated model which also has been saved to DB.
     */
    fun createIt(vararg namesAndValues:Any?):T {
        return ModelDelegate.createIt(modelClass(), *namesAndValues)
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
    fun delete(query:String, vararg params:Any?):Int {
        return ModelDelegate.delete(modelClass(), query, *params)
    }


    /**
     * Deletes all records from associated table. This methods does not take associations into account.
     *
     * @return number of records deleted.
     */
    fun deleteAll():Int {
        return ModelDelegate.deleteAll(modelClass())
    }


    /**
     * Returns true if record corresponding to the id passed exists in the DB.
     *
     * @param id id in question.
     * @return true if corresponding record exists in DB, false if it does not.
     */
    fun exists(id:Any?):Boolean {
        return ModelDelegate.exists(modelClass(), id)
    }


    /**
     * Synonym of {@link #where(String, Object...)}
     *
     * @param subquery this is a set of conditions that normally follow the "where" clause. Example:
     * <code>"department = ? and dob &gt ?"</code>. If this value is "*" and no parameters provided, then {@link #findAll()} is executed.
     * @param params list of parameters corresponding to the place holders in the subquery.
     * @return instance of <code>LazyList<Model></code> containing results.
     */
    fun find(subquery:String, vararg params:Any?):LazyList<T> {
        return ModelDelegate.where(modelClass(), subquery, *params)
    }


    /**
     * This method returns all records from this table. If you need to get a subset, look for variations of "find()".
     *
     * @return result list
     */
    fun findAll():LazyList<T> {
        return ModelDelegate.findAll(modelClass())
    }


    /**
     * Composite PK values in exactly the same order as specified  in {@link CompositePK}.
     *
     * @param values  Composite PK values in exactly the same order as specified  in {@link CompositePK}.
     * @return instance of a found model, or null if nothing found.
     * @see CompositePK
     */
    fun findByCompositeKeys(vararg values:Any?):T? {
        return ModelDelegate.findByCompositeKeys(modelClass(), *values)
    }


    fun findById(id:Any?):T? {
        return ModelDelegate.findById(modelClass(), id)
    }


    /**
     * Free form query finder. Example:
     * <pre>
     * List<Rule> rules = Rule.findBySQL("select rule.*, goal_identifier from rule, goal where goal.goal_id = rule.goal_id order by goal_identifier asc, rule_type desc");
     * </pre>
     * Ensure that the query returns all columns associated with this model, so that the resulting models could hydrate themselves properly.
     * Returned columns that are not part of this model will be ignored, but can be used for clauses like above.
     *
     * @param fullQuery free-form SQL.
     * @param params parameters if query is parametrized.
     * @param <T> - class that extends Model.
     * @return list of models representing result set.
     */
    fun findBySQL(fullQuery:String, vararg params:Any?):LazyList<T> {
        return ModelDelegate.findBySql(modelClass(), fullQuery, *params)
    }


    /**
     * This is a convenience method to fetch existing model from db or to create and insert new record.
     * @param namesAndValues names and values. elements at indexes 0, 2, 4, 8... are attribute names, and elements at
     * indexes 1, 3, 5... are values. Element at index 1 is a value for attribute at index 0 and so on.
     *
     *@return Model fetched from the db or newly created and saved instance.
     */
    fun findOrCreateIt(vararg namesAndValues:Any?):T? {
        return ModelDelegate.findOrCreateIt(modelClass(), *namesAndValues)
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
    fun findFirst(subQuery:String, vararg params:Any?):T? {
        return ModelDelegate.findFirst(modelClass(), subQuery, *params)
    }


    /**
     * This method is for processing really large result sets. Results found by this method are never cached.
     *
     * @param listener this is a call back implementation which will receive instances of models found.
     * @param query sub-query (content after "WHERE" clause)
     * @param params optional parameters for a query.
     */
    fun findWith(listener:ModelListener<T>, query:String, vararg params:Any?) {
        ModelDelegate.findWith(modelClass(), listener, query, *params)
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
    fun first(subQuery:String, vararg params:Any?):T? {
        return ModelDelegate.findFirst(modelClass(), subQuery, *params)
    }


    /**
     * <p>
     * Provides {@link MetaModel} object related to this model class.
     * </p>
     * Synonym of {@link #metaModel()}.
     *
     * @return {@link MetaModel} object related to this model class.
     */
    fun getMetaModel():MetaModel {
        return metaModelOf(modelClass())
    }


    /**
     * Returns name of corresponding table.
     *
     * @return name of corresponding table.
     */
    fun getTableName():String {
        return ModelDelegate.tableNameOf(modelClass())
    }


    fun getValidators():MutableList<Validator> {
        return ModelDelegate.validatorsOf(clazz)
    }


    fun getValidators(klass:Class<T>):MutableList<Validator> {
        return ModelDelegate.validatorsOf(klass)
    }


    /**
     * @return true if this models has a {@link Cached} annotation.
     */
    fun isCached():Boolean {
        return modelClass().getAnnotation(Cached::class.java) != null
    }


    /**
     * Synonym of {@link #getMetaModel()}.
     *
     * @return {@link MetaModel} of this model.
     */
    fun metaModel():MetaModel {
        return metaModelOf(modelClass())
    }


    fun modelClass():Class<T> {
        return clazz
    }


    /**
     * Use to force-purge cache associated with this table. If this table is not cached, this method has no side effect.
     */
    fun purgeCache() {
        ModelDelegate.purgeCache(modelClass())
    }


    /**
     * Removes a validator from model.
     *
     * @param validator validator to remove. It needs to be an exact reference validator instance to
     *                  remove. If argument was not added to this model before, this method will
     *                  do nothing.
     */
    fun removeValidator(validator:Validator) {
        ModelDelegate.removeValidator(modelClass(), validator)
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
    fun update(updates:String, conditions:String, vararg params:Any?):Int {
        return ModelDelegate.update(modelClass(), updates, conditions, *params)
    }


    /**
     * Updates all records associated with this model.
     *
     * This example :
     * <pre>
     *  Employee.updateAll("bonus = ?", "10");
     * </pre>
     * In this example, all employees get a bonus of 10%.
     *
     *
     * @param updates - what needs to be updated.
     * @param params list of parameters for both updates and conditions. Applied in the same order as in the arguments,
     * updates first, then conditions.
     * @return number of updated records.
     */
    fun updateAll(updates:String, vararg params:Any?):Int {
        return ModelDelegate.updateAll(modelClass(), updates, *params)
    }


    /**
     * Finder method for DB queries based on table represented by this model. Usually the SQL starts with:
     *
     * <code>"select * from table_name where " + subQuery</code> where table_name is a table represented by this model.
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
     * @param subQuery this is a set of conditions that normally follow the "where" clause. Example:
     * <code>"department = ? and dob > ?"</code>. If this value is "*" and no parameters provided, then {@link #findAll()} is executed.
     * @param params list of parameters corresponding to the place holders in the subQuery.
     * @return instance of <code>LazyList<Model></code> containing results.
     */
    fun where(subQuery:String, vararg params:Any?):LazyList<T> {
        return ModelDelegate.where(modelClass(), subQuery, *params)
    }


    /**
     * Registers {@link BlankToNullConverter} for specified attributes. This will convert instances of <tt>String</tt>
     * that are empty or contain only whitespaces to <tt>null</tt>.
     *
     * @param attributeNames attribute names
     */
    protected fun blankToNull(vararg attributeNames:String?) {
        ModelDelegate.blankToNull(modelClass(), *attributeNames)
    }


    /**
     * Registers a custom converter for the specified attributes.
     *
     * @param converter custom converter
     * @param attributeNames attribute names
     */
    protected fun convertWith(converter:Converter<Any, Any>, vararg attributeNames:String?) {
        ModelDelegate.convertWith(modelClass(), converter, *attributeNames)
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
     * @param attributeNames attribute names
     */
    protected fun dateFormat(pattern:String, vararg attributeNames:String?) {
        ModelDelegate.dateFormat(modelClass(), pattern, *attributeNames)
    }


    /**
     * Registers date format for specified attributes. This format will be used to convert between
     * Date -> String -> java.sql.Date when using the appropriate getters and setters.
     *
     * <p>See example in {@link #dateFormat(String, String...)}.
     *
     * @param format format to use for conversion
     * @param attributeNames attribute names
     */
    protected fun dateFormat(format:DateFormat, vararg attributeNames:String?) {
        ModelDelegate.dateFormat(modelClass(), format, *attributeNames)
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
     * @param attributeNames attribute names
     */
    protected fun timestampFormat(pattern:String, vararg attributeNames:String?) {
        ModelDelegate.timestampFormat(modelClass(), pattern, *attributeNames)
    }


    /**
     * Registers date format for specified attributes. This format will be used to convert between
     * Date -> String -> java.sql.Timestamp when using the appropriate getters and setters.
     *
     * <p>See example in {@link #timestampFormat(String, String...)}.
     *
     * @param format format to use for conversion
     * @param attributeNames attribute names
     */
    protected fun timestampFormat(format:DateFormat, vararg attributeNames:String?) {
        ModelDelegate.timestampFormat(modelClass(), format, *attributeNames)
    }


    /**
     * Validates email format.
     *
     * @param attributeName name of attribute that holds email value.
     */
    protected fun validateEmailOf(attributeName:String):ValidationBuilder<Validator> {
        return ModelDelegate.validateEmailOf(modelClass(), attributeName)
    }


    protected fun validateNumericalityOf(vararg attributeNames:String?):NumericValidationBuilder {
        return ModelDelegate.validateNumericalityOf(modelClass(), *attributeNames)
    }


    /**
     * The validation will not pass if the value is either an empty string "", or null.
     *
     * @param attributeNames list of attributes to validate.
     */
    protected fun validatePresenceOf(vararg attributeNames:String?):ValidationBuilder<Validator> {
        return ModelDelegate.validatePresenceOf(modelClass(), *attributeNames)
    }


    /**
     * Validates range. Accepted types are all java.lang.Number subclasses:
     * Byte, Short, Integer, Long, Float, Double BigDecimal.
     *
     * @param attributeName attribute to validate - should be within range.
     * @param min min value of range.
     * @param max max value of range.
     */
    protected fun validateRange(attributeName:String, min:Number, max:Number):ValidationBuilder<Validator> {
        return ModelDelegate.validateRange(modelClass(), attributeName, min, max)
    }


    /**
     * Validates an attribite format with a ree hand regular expression.
     *
     * @param attributeName attribute to validate.
     * @param pattern regexp pattern which must match  the value.
     */
    protected fun validateRegexpOf(attributeName:String, pattern:String):ValidationBuilder<Validator> {
        return ModelDelegate.validateRegexpOf(modelClass(), attributeName, pattern)
    }


    /**
     * Add a custom validator to the model.
     *
     * @param validator  custom validator.
     */
    protected fun validateWith(validator:Validator):ValidationBuilder<Validator> {
        return ModelDelegate.validateWith(modelClass(), validator)
    }


    /**
     * Registers {@link ZeroToNullConverter} for specified attributes. This will convert instances of <tt>Number</tt>
     * that are zero to <tt>null</tt>.
     *
     * @param attributeNames attribute names
     */
    protected fun zeroToNull(vararg attributeNames:String?) {
        ModelDelegate.zeroToNull(modelClass(), *attributeNames)
    }

}
