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

import activejdbc.associations.BelongsToAssociation;
import activejdbc.associations.Many2ManyAssociation;
import activejdbc.associations.OneToManyAssociation;
import activejdbc.cache.QueryCache;
import javalite.common.Inflector;
import javalite.common.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

/**
 * While this class is public, it is never instantiated directly. This class provides
 * a number of APIs for augmenting the query. 
 *
 * @author Igor Polevoy
 */
public class LazyList<T extends Model> extends AbstractList<T>{

    final static Logger logger = LoggerFactory.getLogger(LazyList.class);
    protected ArrayList<T> delegate = new ArrayList<T>();
    private List<String> orderBys = new ArrayList<String>();
    private boolean hydrated = false;
    private MetaModel metaModel;
    private List<String> subQueries = new ArrayList<String>();
    private String fullQuery;
    private Object[] params;
    private long limit = -1, offset = -1;
    private Map<Class<T>, Association> includes = new HashMap<Class<T>, Association>();
    
    protected LazyList(String subQuery, Object[] params, MetaModel metaModel){
        if(subQuery != null)
            subQueries.add(subQuery);
        
        this.params = params == null? new Object[]{}: params;
        this.metaModel = metaModel;
    }

    protected LazyList(MetaModel metaModel, String fullQuery, Object[] params){
        this.fullQuery = fullQuery;
        this.params = params == null? new Object[]{}: params;
        this.metaModel = metaModel;
    }

    protected LazyList(){}
    /**
     *  This method limits the number of results in the resultset.
     *  It can be used in combination wit the offset like this:
     * 
     *  <code>List&lt;Event&gt; events =  Event.find("mnemonic = ?", "GLUC").offset(101).limit(20).orderBy("history_event_id");</code>
     *  This will produce 20 records, starting from record 101. This is an efficient method, it will only retrieve records
     *  that are necessary.
     *
     * @param limit how many records to retrieve.
     * @return instance of this <code>LazyList</code>
     */
    public <E extends Model>  LazyList<E> limit(long limit){
        if(fullQuery != null) throw new IllegalArgumentException("Cannot use .limit() if using free form SQL");

        if(limit < 0) throw new IllegalArgumentException("limit cannot be negative");

        this.limit = limit;
        return (LazyList<E>) this;
    }

    /**
     * This method sets an offset of a resultset. For instance, if the offset is 101, then the resultset will skip the
     * first 100 records.
     * It can be used in combination wit the limit like this:
     *
     *  <code>List<Event>  events =  Event.find("mnemonic = ?", "GLUC").offset(101).limit(20).orderBy("history_event_id");</code>
     *  This will produce 20 records, starting from record 101. This is an efficient method, it will only retrieve records
     *  that are necessary.
     *
     * @param offset
     * @return instance of this <code>LazyList</code>
     */
    public <E extends Model>  LazyList<E> offset(long offset){
        if(fullQuery != null) throw new IllegalArgumentException("Cannot use .offset() if using free form SQL");

        if(offset < 0) throw new IllegalArgumentException("offset cannot be negative");

        this.offset = offset;
        return (LazyList<E>) this;
    }

    /**
     * Use this method to order results by a column. These methods can be chained:
     * <code>Person.find(...).orderBy("department").orderBy("age")</code>
     *
     * @param orderBy order by clause. Examples: "department", "age desc", etc.
     * @return instance of this <code>LazyList</code>
     */
    public <E extends Model>  LazyList<E> orderBy(String orderBy){
        if(fullQuery != null) throw new IllegalArgumentException("Cannot use .orderBy() if using free form SQL");

        orderBys.add(orderBy);
        return (LazyList<E>) this;
    }


    /**
     * Use this method includes associated objects. This method will eagerly load associated models of
     * models selected by the query. For instance, if there are models <code>Author</code>, <code>Post</code>
     * and <code>Comment</code>, where <code>Author</code> has many <code>Post</code>s and <code> Post</code>
     * has many <code>Comment</code>s, then this query:
     * <pre>
     * List<Post> todayPosts = Post.where("post_date = ?", today).include(Author.class, Comment.class);
     * </pre>
     * will generate only three queries to database - one per model. All the dependencies (includes) will be
     * eagerly loaded, and iteration via the <code>todayPosts</code> list will not generate any more queries,
     * even when a post author and comments are requested. Use this with caution as this method can allocate
     * a lot of memory (obviously).
     *
     * <p/>
     *
     * This method will not follow relationships of related models, but rather only relationships of the current
     * one.  
     *
     * @param classes list of dependent classes. These classes represent models with which a current model has a
     * relationship.
     * @return instance of this <code>LazyList</code>
     */
    public <E extends Model>  LazyList<E>  include(Class<? extends Model> ... classes){
        if(includes.size() != 0) throw new IllegalArgumentException("Can't call include() more than once!");

        //lets cache included classes and associations for future processing.
        for(Class includeClass: classes){
            String table = Registry.instance().getTableName(includeClass);
            Association association = metaModel.getAssociationForTarget(table);
            if(association == null){
                throw new IllegalArgumentException("this model is not associated with " + includeClass);
            }

            includes.put(includeClass, association);
        }

        return (LazyList<E>)this;
    }

    /**
     * Converts the resultset to list of maps, where each map represents a row in the resultset keyed off column names.
     *
     * @return list of maps, where each map represents a row in the resultset keyed off column names.
     */
    public List<Map> toMaps(){
        hydrate();
        List<Map> maps = new ArrayList<Map>(delegate.size());
        for (T t : delegate) {
            maps.add(t.toMap());
        }
        return maps;
    }

    /**
     * Generates a XML document from content of this list.
     *
     * @param spaces by how many spaces to indent.
     * @param declaration true to include XML declaration at the top
     * @param attrs list of attributes to include. No arguments == include all attributes.
     * @return generated XML.
     */
    public String toXml(int spaces, boolean declaration, String ... attrs){
        String topNode = Inflector.pluralize(Inflector.underscore(metaModel.getModelClass().getSimpleName()));

        hydrate();

        StringWriter sw = new StringWriter();
        if(declaration)
            sw.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" + (spaces > 0?"\n":""));

        sw.write("<"  + topNode + ">" + (spaces > 0 ? "\n":""));
        for (T t : delegate) {
            sw.write(t.toXml(spaces, false, attrs));
        }
        sw.write("</"  + topNode + ">" + (spaces > 0 ? "\n":""));
        return sw.toString();
    }

    /**
     * Generates JSON from content of this list
     *
     * @param pretty true if you want pretty format, false if not
     * @param attrs attributes to include, not providing any will include all.
     * @return generated JSON
     */
    public String toJson(boolean pretty, String ... attrs) {
        hydrate();
        StringWriter sw = new StringWriter();
        sw.write("[" + (pretty? "\n":""));
        List<String> items = new ArrayList<String>();
        for (T t : delegate) {
            items.add(t.toJsonP(pretty, (pretty?"  ":""), attrs));
        }
        sw.write(Util.join(items, "," + (pretty?"\n":"")));
        sw.write((pretty? "\n":"") + "]" );
        return sw.toString();
    }


    /**
     * This method exists to force immediate load from DB. Example;
     * <code> Person.find("name = ?", "Smith").load();</code>.
     * It is not possible to call other methods after load(). The load() method should be the last to be called in the chain:
     * <code> Person.find("name = ?", "Smith").limit(10).load();</code>.
     * This: will generate exception: <code> Person.find("name = ?", "Smith").load().limit();</code>. 
     *
     * @return fully loaded list.
     */
    //TODO: write test, and also test for exception.
    public <E extends Model>  LazyList<E>  load(){
        
        if(hydrated) throw new DBException("load() must be the last on the chain of methods");

        hydrate();
        return (LazyList<E>) this;
    }

    protected void hydrate(){

        if(hydrated) return;

        String subQuery = Util.join(subQueries.toArray(new String[]{}), " ");

        String sql = fullQuery != null? fullQuery :
                Registry.instance().getConfiguration().getDialect(metaModel).formSelect(metaModel.getTableName(), subQuery,
                        orderBys, limit, offset);

        if(metaModel.cached()){        
            ArrayList<T> cached = (ArrayList<T>) QueryCache.instance().getItem(metaModel.getTableName(), sql, params);
            if(cached != null){
                delegate = cached;
                return;
            }
        }

        long start = System.currentTimeMillis();
        new DB(metaModel.getDbName()).find(sql, params).with(new RowListenerAdapter() {
            public void onNext(Map<String, Object> rowMap) {
                delegate.add((T) Model.instance(rowMap, metaModel));
            }
        });
        LogFilter.logQuery(logger, sql, params, start);
        if(metaModel.cached()){
            QueryCache.instance().addItem(metaModel.getTableName(), sql, params, delegate);
        }
        hydrated = true;
        processIncludes();        
    }

    private void processIncludes(){

        for(Class includedClass: includes.keySet()){            
            Association association = includes.get(includedClass);
            if(association instanceof BelongsToAssociation){
                processParent((BelongsToAssociation)association, includedClass);
            }else if(association instanceof OneToManyAssociation){
                processChildren((OneToManyAssociation)association, includedClass);
            }else if(association instanceof Many2ManyAssociation){
                processOther((Many2ManyAssociation)association, includedClass);
            }
        }        
    }

    private void processParent(BelongsToAssociation association, Class parentClass) {

        if(delegate.size() == 0){//no need to process parents if no models selected.
            return;
        }

        final MetaModel parentMM = Registry.instance().getMetaModel(parentClass);
        final Map<Object, Model> parentsHasByIds = new HashMap<Object, Model>();

        String fkName = association.getFkName();

        //need to remove duplicates because more than one child can belong to the same parent. 
        List parentIds = collect(fkName);
        ArrayList noDuplicateList = new ArrayList(new HashSet(parentIds));

        for(Model parent: new LazyList<Model>(parentMM.getIdName() + " IN (" + Util.join(noDuplicateList, ", ") + ")", null, parentMM)){
               parentsHasByIds.put(parent.getId(), parent);
        }

        //now that we have the parents in the has, we need to distribute them into list of children that are
        //stored in the delegate.
        for(Model child: delegate){
            Object fk = child.get(fkName);
            Model parent = parentsHasByIds.get(fk);
            if(parent == null){
                throw new OrphanRecordException("Failed to find this child's parent, seems like an orphaned record. Child model: " + child);
            }
            child.setCachedParent(parent);
        }
    }

    /**
     * Collects values from a result set that correspond to a column name.
     * For example, if a list contains collection of <code>Person</code> models, then
     * you can collect first names like this:
     * <pre>
     * List firstNames = Person.findAll().collect("first_name");
     * </pre>
     * provided that the corresponding table has a column <code>first_name</code>.
     * <p/><p/>
     * Bare in mind, that if all you need is a one column data, this method of getting it is not
     * the most efficient (because since you are using a model, you will query all columns from a table,
     * but will use only one). In these cases, you might want to consider {@link Base#firstColumn(String, Object...)} and
     * {@link DB#firstColumn(String, Object...)}.
     *
     * @param columnName name of column to collect.
     * @return list of collected values for a column.
     */
    public List collect(String columnName){
        hydrate();
        List results = new ArrayList();
        for(Model model: delegate){
            results.add(model.get(columnName));
        }

        return results;
    }

    private void processChildren(OneToManyAssociation association, Class childClass) {

        if(delegate.size() == 0){//no need to process children if no models selected.
            return;
        }

        final MetaModel childMM = Registry.instance().getMetaModel(childClass);
        final String fkName = association.getFkName();

        final Map<Object, List<Model>> childrenByParentId = new HashMap<Object, List<Model>>();

        List ids = collect(metaModel.getIdName());

        for(Model child: new LazyList<Model>(fkName + " IN (" + Util.join(ids, ", ") + ")" , null, childMM).orderBy(childMM.getIdName())){
             if(childrenByParentId.get(child.get(fkName)) == null){
                    childrenByParentId.put(child.get(fkName), new SuperLazyList<Model>());
             }
            childrenByParentId.get(child.get(fkName)).add(child);
        }

        for(T parent : delegate){
            List<Model> children = childrenByParentId.get(parent.getId());
            if(children != null){
                parent.setChildren(childClass, children);
            }
        }
    }

    private void processOther(Many2ManyAssociation association, Class childClass) {
        if(delegate.size() == 0){//no need to process other if no models selected.
            return;
        }

        final MetaModel childMM = Registry.instance().getMetaModel(childClass);
        final Map<Object, List<Model>> childrenByParentId = new HashMap<Object, List<Model>>();

        List ids = collect(metaModel.getIdName());
        
        String sql =  "SELECT " + association.getTarget() + ".*, t." + association.getSourceFkName() + " AS the_parent_record_id FROM " + association.getTarget() +
        " INNER JOIN " + association.getJoin() + " t ON " + association.getTarget() + "." + association.getTargetPk() + " = t." + association.getTargetFkName() + " WHERE (t." + association.getSourceFkName()
                + "  IN (" + Util.join(ids, ", ") + "))";


        List<Map> childResults = new DB(childMM.getDbName()).findAll(sql);

        for(Map res: childResults){
            Model child = Model.instance(res, childMM);
            Object parentId = res.get("the_parent_record_id");
            if(childrenByParentId.get(parentId) == null){
                    childrenByParentId.put(parentId, new SuperLazyList<Model>());
             }
            childrenByParentId.get(parentId).add(child);
        }
        
        for(T parent : delegate){
            List<Model> children = childrenByParentId.get(parent.getId());
            if(children != null){
                parent.setChildren(childClass, children);
            }
        }
    }

    public T get(int index) {
        hydrate();
        return delegate.get(index);
    }

    public int size() {
        hydrate();
        return delegate.size();
    }

    public boolean isEmpty() {
        hydrate();
        return delegate.isEmpty();
    }

    public boolean contains(Object o) {
        hydrate();
        return delegate.contains(o);
    }

    public Iterator<T> iterator() {
        hydrate();
        return delegate.iterator();
    }

    public Object[] toArray() {
        hydrate();
        return delegate.toArray();
    }

    public <T> T[] toArray(T[] a) {
        hydrate();
        return delegate.toArray(a);
    }

    public boolean add(T o) {
        throw new UnsupportedOperationException("this operation is not supported, cannot manipulate DB results");
    }

    public boolean remove(Object o) {
        throw new UnsupportedOperationException("this operation is not supported, cannot manipulate DB results");
    }

    public boolean containsAll(Collection c) {
        return delegate.containsAll(c);
    }

    public boolean addAll(Collection c) {
        throw new UnsupportedOperationException("this operation is not supported, cannot manipulate DB results");
    }

    public boolean addAll(int index, Collection c) {
        throw new UnsupportedOperationException("this operation is not supported, cannot manipulate DB results");
    }

    public boolean removeAll(Collection c) {
        throw new UnsupportedOperationException("this operation is not supported, cannot manipulate DB results");
    }

    public boolean retainAll(Collection c) {
        throw new UnsupportedOperationException("this operation is not supported, cannot manipulate DB results");
    }

    public void clear() {
        throw new UnsupportedOperationException("this operation is not supported, cannot manipulate DB results");
    }

    public T set(int index, T element) {
        throw new UnsupportedOperationException("this operation is not supported, cannot manipulate DB results");
    }

    public void add(int index, T element) {
        throw new UnsupportedOperationException("this operation is not supported, cannot manipulate DB results");
    }

    public T remove(int index) {
        throw new UnsupportedOperationException("this operation is not supported, cannot manipulate DB results");
    }

    public int indexOf(Object o) {
        hydrate();
        return delegate.indexOf(o);
    }

    public int lastIndexOf(Object o) {
        hydrate();
        return delegate.lastIndexOf(o);
    }

    public ListIterator<T> listIterator() {
        hydrate();
        return delegate.listIterator();
    }

    public ListIterator<T> listIterator(int index) {
        hydrate();
        return delegate.listIterator(index);
    }

    public List<T> subList(int fromIndex, int toIndex) {
        hydrate();
        return delegate.subList(fromIndex, toIndex);
    }

    /**
     * This is only to test caching.
     * @return
     */
    @Override
    public int hashCode() {
        hydrate();
        return delegate.hashCode();    
    }

    @Override
    public String toString() {
        hydrate();
        return delegate.toString();    
    }

    /**
     * Dumps contents of this list to <code>System.out</code>.
     */
    public void dump(){
        dump(System.out);
    }

    /**
     * Dumps content of list to a stream. Use for debugging. 
     * @param out
     */
    public void dump(OutputStream out){
        hydrate();
        PrintWriter p = new PrintWriter(out);
        for(Model m : delegate){
            p.write(m.toString() + "\n");
        }
        p.flush();
    }
}
