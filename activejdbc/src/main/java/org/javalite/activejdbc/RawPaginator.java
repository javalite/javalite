/*
Copyright 2009-2016 Igor Polevoy

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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This paginator is for paging through free-form queries unrelated to models. It is used  to collect an arbitrary
 * number of columns form a table, whereas the {@link Paginator} is  tied to models, where each model
 * collects all columns from underlying table.
 * <p></p>
 *
 * This class can be more efficient because it allows to load only the columns you need to display on the UI.
 * <p></p>
 *
 * This class supports pagination of result sets in ActiveJDBC. This is useful for paging through tables. If the
 *
 * This class is thread safe and the same instance could be used across multiple web requests and even
 * across multiple users/sessions. You can generate an instance each time you need one,
 * or you can cache an instance in a session or even servlet context.
 *
 * @author Igor Polevoy
 */
public class RawPaginator implements Serializable {


    private final int pageSize;
    private final String subQuery;
    private List<String> orderBys = new ArrayList<>();
    private final Object[] params;

    private final String tableName;
    private int currentPage;

    private final String countQuery;
    private boolean suppressCounts;
    private Long count = 0L;
    private Dialect dialect;
    private String dbName;
    private String[] columns;


    /**
     * Convenience constructor. Defaults to "default" database and <code>suppressCount = false</code>
     *
     * @param tableName name of a table to use
     * @param columns   list of columns to select, or <code>null</code> to select all columns
     * @param pageSize  number of items per page.
     * @param subQuery  - sub query to select some records, such as "last_name like ?". If <code>null</code> is provided,
     *                  all records are selected without any filtering
     * @param params    a set of parameters if a query is parametrized (has question marks '?').
     */
    public RawPaginator(String tableName, String[] columns, int pageSize, String subQuery, Object... params){
        this("default", tableName, columns, pageSize, false, subQuery, params);
    }

    /**
     * Paginator is created with parameters to jump to chunks of result sets (pages). This class is useful "paging"
     * through result on a user interface (web page).
     *
     * @param dbName         name of database
     * @param tableName      name of a table to use
     * @param columns        list of columns to select, or <code>null</code> to select all columns
     * @param pageSize       number of items per page.
     * @param suppressCounts suppress calling "select count(*)... " on a table each time. If set to true,
     *                       it will call count only once. If set to false, it will call count each time
     *                       {@link #getCount()} is called from {@link #hasNext()} as well.
     * @param subQuery       - sub query to select some records, such as "last_name like ?". If <code>null</code> is provided,
     *                       all records are selected without any filtering
     * @param params         a set of parameters if a query is parametrized (has question marks '?').
     */
    public RawPaginator(String dbName, String tableName, String[] columns, int pageSize, boolean suppressCounts, String subQuery, Object... params){

        this.suppressCounts = suppressCounts;
        this.tableName = tableName;
        this.pageSize = pageSize;
        this.subQuery = subQuery;
        this.params = params;
        this.dbName = dbName;
        this.columns = columns;

        try {
            Connection c = new DB(dbName).connection();
            String dbType = c.getMetaData().getDatabaseProductName();
            dialect = Registry.instance().getConfiguration().getDialect(dbType);
            this.countQuery  = subQuery == null? dialect.selectCount(tableName) :dialect.selectCount(tableName, subQuery);

        } catch (Exception e) {
            throw new InternalException(e);
        }
    }

    /**
     * Use to set order by(s). Example: <code>paginator.orderBy("category").orderBy("tab");</code>
     *
     * @param orderBy - a single "order by" expression.
     * @return instance to self.
     */
    public RawPaginator orderBy(String orderBy) {
        this.orderBys.add(orderBy);
        return this;
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

        if (pageNumber < 1) {
            throw new IllegalArgumentException("minimum page index == 1");
        }

        String select = subQuery == null ?
                dialect.formSelect(tableName, columns, null, orderBys, pageSize, (pageNumber - 1) * pageSize) :
                dialect.formSelect(tableName, columns, subQuery, orderBys, pageSize, (pageNumber - 1) * pageSize);

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
                count = Convert.toLong(new DB(dbName).firstCell(countQuery, params));
            return count;
        } else {
            return count;
        }
    }
}
