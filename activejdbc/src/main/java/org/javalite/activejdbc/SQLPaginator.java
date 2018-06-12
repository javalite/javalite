/*
Copyright 2009-2018 Igor Polevoy

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

import org.javalite.activejdbc.dialects.Dialect;
import org.javalite.common.Convert;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * This paginator is for paging through free-form queries that can span multiple tables. It is used  to collect an arbitrary
 * number of columns form a resultset.
 * <p></p>
 *
 * <code>SQLPaginator</code> is thread-safe and the same instance could be used across multiple web requests and even
 * across multiple users/sessions.
 * You can generate an instance each time you need one, or you can cache an instance in a session or even servlet context.
 *
 * @since 2.1.
 *
 * @author Igor Polevoy
 */
public class SQLPaginator implements Serializable {


    private int pageSize;
    private int currentPage;
    private String sqlQuery;
    private Object[] params;
    private boolean suppressCounts;
    private Long count = 0L;
    private String dbName = "default";
    private String countQuery;
    private Object[] countQueryParams;
    private Dialect dialect;
    private Object[] orderBys;


    public SQLPaginator(){

        try {
            Connection c = new DB(dbName).connection();
            String dbType = c.getMetaData().getDatabaseProductName();
            dialect = Registry.instance().getConfiguration().getDialect(dbType);
        } catch (SQLException e) {
            throw new DBException(e);
        }
    }

    /**
     * This method will return a list of records for a specific page.
     *
     * @param pageNumber page number to return. This is indexed at 1, not 0. Any value below 1 is illegal and will
     *                   be rejected.
     * @return list of records that match a query make up a "page".
     */
    @SuppressWarnings("unchecked")
    public List<Map>  getPage(int pageNumber) {

        String select = dialect.formSelect(sqlQuery, orderBys, pageSize, (pageNumber - 1) * pageSize);

        currentPage = pageNumber;
        return new DB(dbName).findAll(select, params);

    }

    /**
     * Returns index of current page, or 0 if this instance has not produced a page yet.
     *
     * @return index of current page, or 0 if this instance has not produced a page yet.
     */
    public int getCurrentPage() {
        return currentPage;
    }

    /**
     * Synonym for {@link #hasPrevious()}.
     *
     * @return true if a previous page is available.
     */
    public boolean getPrevious() {
        return hasPrevious();
    }

    public boolean hasPrevious() {
        return currentPage > 1 && currentPage <= pageCount();
    }

    /**
     * Synonym for {@link #hasNext()}.
     *
     * @return true if a next page is available.
     */
    public boolean getNext() {
        return hasNext();
    }

    public boolean hasNext() {
        return currentPage < pageCount();
    }

    public long pageCount() {
        long results = getCount();
        long fullPages = results / pageSize;
        return results % pageSize == 0 ? fullPages : fullPages + 1;
    }

    /**
     * Returns total count of records based on provided criteria.
     *
     * @return total count of records based on provided criteria
     */
    public Long getCount() {
        if (count == 0L || !suppressCounts) {
                count = Convert.toLong(new DB(dbName).firstCell(countQuery, countQueryParams));
            return count;
        } else {
            return count;
        }
    }


//    ------------------ private methods below, part of the builder pattern --------------------------

    private void setCountQueryParams(Object[] countQueryParams) {
        this.countQueryParams = countQueryParams;
    }

    private void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    private void setSqlQuery(String sqlQuery) {
        this.sqlQuery = sqlQuery;
    }

    private void setParams(Object[] params) {
        this.params = params;
    }

    private void setSuppressCounts(boolean suppressCounts) {
        this.suppressCounts = suppressCounts;
    }

    private void setCount(Long count) {
        this.count = count;
    }

    private void setDbName(String dbName) {
        this.dbName = dbName;
    }

    private void setCountQuery(String countQuery) {
        this.countQuery = countQuery;
    }

    public void setOrderBys(Object[] orderBys) {
        this.orderBys = orderBys;
    }

    public Object[] getOrderBys() {
        return orderBys;
    }

    public static class PaginatorBuilder<T extends Model>{

        private SQLPaginator sqlPaginator = new SQLPaginator();

        /**
         * Page size  - number of items in a page
         *
         * @param pageSize Page size  - number of items in a page
         */
        public PaginatorBuilder pageSize(int pageSize){
            this.sqlPaginator.setPageSize(pageSize);
            return this;
        }

        /**
         * Suppress calling a count query on a table each time. If set to true,
         *                       it will call count only once. If set to false, it will call count each time
         *                       {@link #pageCount()}, {@link #getCount()} or {@link #hasNext()} called. Default is
         *                       <code>false</code>.
         *
         * @param suppressCounts suppress counts every time.
         */
        public PaginatorBuilder suppressCounts(boolean suppressCounts){
            this.sqlPaginator.setSuppressCounts(suppressCounts);
            return this;
        }


        /**
         * @param query Query that will be applied every time a new page is requested; this
         *              query should not contain limit, offset or order by clauses of any kind, Paginator will do this automatically.
         *              This parameter can have two forms, a sub-query or a full query.
         */
        public PaginatorBuilder query(String query) {
            this.sqlPaginator.setSqlQuery(query);
            return this;
        }

        /**
         * Array of parameters in case  a query is parametrized (has question marks '?').
         *
         * @param params Array of parameters in case  a query is parametrized
         */
        public PaginatorBuilder params(Object ... params){
            this.sqlPaginator.setParams(params);
            return this;
        }


        /**
         * //TODO: reject execution if count query is not provided.
         *
         * A query that is responsible for count. Example: <code>COUNT(DISTINCT(u.id)</code>.
         * Only use this method if you need something more complex than <code>COUNT(*)</code>, since
         * that is the value that us used by default.
         *
         * @param countQuery A query that is responsible for count.
         *                   Example: <code>SELECT COUNT(*) FROM people</code>
         *                   or <code>select COUNT(DISTINCT(first_name) FROM people</code>.
         */
        public PaginatorBuilder countQuery(String countQuery, Object...countParams) {
            this.sqlPaginator.setCountQuery(countQuery);
            return this;
        }

        /**
         * Array of parameters in case  a count query is parametrized (has question marks '?').
         *
         * @param params Array of parameters in case  a count query is parametrized
         */
        public PaginatorBuilder countQueryParams(Object ... params){
            this.sqlPaginator.setCountQueryParams(params);
            return this;
        }

        /**
         * Array of columns to order by. So, if you want: <code>order by first_name, last_name</code> in your SQL, pass:
         * <code></code>
         *
         * @param orderBys Array of columns to order by.
         */
        public PaginatorBuilder orderBys(Object... orderBys) {
            this.sqlPaginator.setOrderBys(orderBys);
            return this;
        }

        /**
         * Sets name of a DB. This method is optional if you only have one database.
         * It will default to <code>"default</code>.
         *
         * @param dbName - logical name of the database.
         */
        public PaginatorBuilder dbName(String dbName){
            this.sqlPaginator.setDbName(dbName);
            return this;
        }

        /**
         * Terminal method to create an instance of Paginator.
         * @return new Paginator properly configured.
         */
        public SQLPaginator get(){
            if(sqlPaginator.countQuery == null){
                throw new IllegalArgumentException("Count query missing.");
            }
            return sqlPaginator;
        }
    }

    /**
     * Use to create a paginator instance, and provide arguments as needed.
     * @return self.
     */
    public static PaginatorBuilder instance(){
        return new PaginatorBuilder();
    }

}
