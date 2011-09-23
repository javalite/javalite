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

import org.javalite.activejdbc.cache.QueryCache;
import java.io.Serializable;

/**
 * This class supports pagination of result sets in ActiveJDBC. This is useful for paging through
 * tables. This class does not cache resultsets, rather it will make requests to DB
 * each time {@link #getPage(int)} method is called.
 * This class is thread safe and the same instance could be used across multiple web requests and even
 * across multiple users/sessions. It is lightweight class, you can generate an instance each time you need one,
 * or you can cache an instance in a session or even servlet context. 
 *
 * @author Igor Polevoy
 */
public class Paginator implements Serializable {

    private int pageSize;
    private String query, orderBys;
    private Object[] params;
    private MetaModel metaModel;
    private int currentPage;
    private boolean fullQuery;


    /**
     * Paginator is created with parameters to jump to junk of result sets (pages). This class is useful "paging"
     * through result on a user interface (web page).
     *
     * <h4>Examples of a sub-query:</h4>
     * <ul>
     *     <li><code>"last_name like '%John%'"</code> - this is a sub-query, and the rest of the information will be filled out
     * by this class</li>
     *     <li> "*" - will search for all records, no filtering</li>
     * </ul>
     * Sub-query is used in simple cases, when filtering is done against one database.
     *
     * <h4>Full query example</h4>
     * <ul>
     *     <li>"select * from people where last_name like '%John%'"</li>
     * </ul>
     *
     * @param modelClass model class mapped to a table.
     * @param pageSize   number of items per page.
     * @param params a set of parameters if a query is parametrized (has question marks '?').
     * @param query      this is a query that will be applied every time a new page is requested; this
     * query should not contain limit, offset or order by clauses of any kind, Paginator will do this automatically.
     * This parameter can have two forms, a sub-query or a full query.
     *
     *
     */
    public Paginator(Class<? extends Model> modelClass, int pageSize, String query, Object... params) {

        try{
            Class.forName(modelClass.getName());
        }catch(Exception e){
            throw new InitException(e);
        }

        
        this.pageSize = pageSize;
        this.query = query;
        this.params = params;
        String tableName = Registry.instance().getTableName(modelClass);
        this.metaModel = Registry.instance().getMetaModel(tableName);

        this.fullQuery = query.trim().toLowerCase().startsWith("select");

    }

    /**
     * Use to set order by(s). Example: <code>"category, created_at desc"</code>
     * 
     * @param orderBys a comma-separated list of field names followed by either "desc" or "asc"  
     * @return instance to self.
     */
    public Paginator orderBy(String orderBys) {
        this.orderBys = orderBys;
        return this;
    }

    /**
     * This method will return a list of records for a specific page.
     *
     * @param pageNumber page number to return. This is indexed at 1, not 0. Any value below 1 is illegal and will
     * be rejected.
     * @return list of records that match a query make up a "page". 
     */
    public <T extends Model> LazyList<T> getPage(int pageNumber) {

        if (pageNumber < 1) throw new IllegalArgumentException("minimum page index == 1");

        try {
            LazyList<T> list = find( query, params);
            int offset = (pageNumber - 1) * pageSize; 
            list.offset(offset);
            list.limit(pageSize);
            if (orderBys != null) {
                list.orderBy(orderBys);
            }
            currentPage = pageNumber;
            return list;
        }
        catch (Exception mustNeverHappen) {
            throw new InternalException(mustNeverHappen);
        }
    }

    /**
     * Returns index of current page, or -1 if this instance has not produced a page yet.
     *
     * @return index of current page, or -1 if this instance has not produced a page yet.
     */
    public int getCurrentPage(){
        return currentPage;
    }

    /**
     * Synonym for {@link #hasPrevious()}.
     *
     * @return true if a previous page is available. 
     */
    public boolean getPrevious(){
        return hasPrevious();
    }
    public boolean hasPrevious(){
        return currentPage > 1 && currentPage <= pageCount();
    }

    /**
     * Synonym for {@link #hasNext()}.
     * 
     * @return true if a next page is available. 
     */
    public boolean getNext(){
        return hasNext();
    }

    public boolean hasNext(){
        return currentPage < pageCount();
    }
    
    public long pageCount() {
        try {
            long results = count(query, params);
            long fullPages = results / pageSize;
            return results % pageSize == 0 ? fullPages : fullPages + 1;
        } catch (Exception mustNeverHappen) {
            throw new InternalException(mustNeverHappen);
        }
    }

    private <T extends Model> LazyList<T> find(String query, Object... params) {

        if (query.equals("*") && params.length == 0) {
            return findAll();
        }

        if (query.equals("*") && params.length != 0) {
            throw new IllegalArgumentException("cannot provide parameters with query: '*', use findAll() method instead");
        }

        return fullQuery ? new LazyList(true, metaModel, this.query, params) : new LazyList(query, params, metaModel);
    }

    private <T extends Model> LazyList<T> findAll() {
        return new LazyList(null, new Object[]{}, metaModel);
    }

    private Long count(String query, Object... params) {

        //attention: this SQL is only used for caching, not for real queries.
        String sql = "SELECT COUNT(*) FROM " + metaModel.getTableName() + " WHERE " + query;

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
}
