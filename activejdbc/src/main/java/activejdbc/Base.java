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

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Properties;


/**
 * This class provides a number of convenience methods for opening/closing database connections, running various
 * types of queries, and executing SQL statements. This class differs from {@link activejdbc.DB} such that
 * in this class you a logical name for a connection is hard-coded to be "default". Use this class when you have
 * only one database.
 * <p/>
 * This class is a convenience wrapper of {@link activejdbc.DB}
 *
 * @author Igor Polevoy
 */
public class Base {

    private static final String DEFAULT_DB_NAME = "default";


    /**
     * Opens a new connection based on JDBC properties and attaches it to a current thread.
     *
     * @param driver class name of driver
     * @param url URL connection to DB
     * @param user user name.
     * @param password password.
     */
    public static void open(String driver, String url, String user, String password) {
       new DB(DEFAULT_DB_NAME).open(driver, url, user, password);
    }


    /**
     * Opens a new connection in case additional driver-specific parameters need to be passed in.
     *
     * @param driver driver class name
     * @param url JDBC URL
     * @param props connection properties
     */
    public static void open(String driver, String url, Properties props) {
        new DB(DEFAULT_DB_NAME).open(driver, url, props);
    }

    /**
     * Opens a connection from JNDI based on a registered name. This assumes that there is a <code>jndi.properties</code>
     * file with proper JNDI configuration in it.
     *
     * @param jndiName name of a configured data source.
     */
    public static void open(String jndiName) {
        new DB(DEFAULT_DB_NAME).open(jndiName);
    }

    /**
     * Opens a new connection from JNDI data source by name using explicit JNDI properties. This method can be used in cases
     * when file <code>jndi.properties</code> cannot be easily modified.
     *
     * @param jndiName name of JNDI data source.
     * @param jndiProperties JNDI properties
     */
    public static void open(String jndiName, Properties jndiProperties) {
        new DB(DEFAULT_DB_NAME).open(jndiName, jndiProperties);
    }

    /**
     * Opens a connection from a datasource. This methods gives a high level control while sourcing a DB connection.
     *
     * @param dataSource datasource will be used to acquire a connection.
     */
    public static void open(DataSource dataSource) {
        new DB(DEFAULT_DB_NAME).open(dataSource);
    }


    /**
     * Returns connection attached to a current thread and names "default".
     *
     * @return connection attached to a current thread and names "default".
     */
    public static Connection connection() {
        return new DB(DEFAULT_DB_NAME).connection();

    }

    /**
     * Closes connection.
     */
    public static void close() {
      new DB(DEFAULT_DB_NAME).close();
    }


    /**
     * Returns count of rows in table.
     *
     * @param table name of table.
     * @return count of rows in table.
     */
    public static Long count(String table){        
        return new DB(DEFAULT_DB_NAME).count(table);
    }

    /**
     * Runs a count query, returns a number of matching records.
     *
     * @param table table in which to count rows.
     * @param query this is a filtering query for the count. If '*' provided, all records will be counted. Example:
     * <code>"age > 65 AND department = 'accounting'"</code>
     * @param params parameters for placeholder substitution.
     * @return copunt number of records found in a table.
     */
    public static Long count(String table, String query, Object... params) {
        return new DB(DEFAULT_DB_NAME).count(table, query, params);
    }
    
    /**
     * Returns a value of the first column of the first row.
     * This query expects only one column selected in the select statement.
     * If more than one column returned, it will throw {@link IllegalArgumentException}.
     *
     * @param query query
     * @param params parameters
     * @return fetched value, or null if query did not fetch anything.
     */
    public static Object firstCell(String query, Object... params) {
        return new DB(DEFAULT_DB_NAME).firstCell(query, params);
    }

    /**
     * This method returns entire resultset as one list. Do not use it for large result sets.
     * Example:
     * <code>
     *  List<Map<String, Object>> people = Base.findAll("select * from people where first_name = ?", "John");
     *  for(Map person: people)
     *      System.out.println(person.get("first_name"));
     * </code>
     *
     * @param query raw SQL query. This query is parametrized.
     * @param params list of parameters for a parametrized query.
     * @return entire result set corresponding to the query.
     */
    public static List<Map> findAll(String query, Object ... params) {
        return new DB(DEFAULT_DB_NAME).findAll(query, params);
    }

    /**
     * This method returns entire resultset as one list. Do not use it for large result sets.
     * Example:
     * <pre>
     *  List ssns = Base.firstColumn("select ssn from people where first_name = ?", "John");
     *  for(Object ssn: ssns)
     *      System.out.println(ssn);
     * </pre>
     *
     * This methods expects a query which selects only one column from a table/view. It will throw an exception if more than one
     * columns are fetched in a result set.
     *
     * @param query raw SQL query. This query is parametrized.
     * @param params list of parameters for a parametrized query.
     * @return entire result set corresponding to the query.
     */
    public static List firstColumn(String query, Object ... params) {
        return new DB(DEFAULT_DB_NAME).firstColumn(query, params);
    }

    /**
     * This method returns entire resultset as one list. Do not use it for large result sets.
     *
     * @param query raw SQL query. This query is not parametrized.
     * @return entire result set corresponding to the query.
     */
    public static List<Map> findAll(String query) {
        return new DB(DEFAULT_DB_NAME).findAll(query);
    }

    /**
     * Executes a raw query and returns an instance of {@link activejdbc.RowProcessor}. Use it in the following pattern:
     * <pre>
     * Base.find("select first_name, last_name from really_large_table").with(new RowListenerAdapter() {
            public void onNext(Map row) {
                ///write your code here
                Object o1 = row.get("first_name");
                Object o2 = row.get("last_name");
            }
        });
     </pre>
     *
     * @param query raw SQL.
     * @param params list of parameters if query is parametrized.
     * @return instance of <code>RowProcessor</code> which has with() method for convenience.
     */
    public static RowProcessor find(String query, Object ... params) {
      return new DB(DEFAULT_DB_NAME).find(query, params);       
    }

    /**
     * Executes a raw query and calls instance of <code>RowListener</code> with every row found.
     * Use this method for very large result sets.
     *
     * @param sql raw SQL query.
     * @param listener client listener implementation for processing individual rows.
     */
    public static void find(String sql, RowListener listener) {
        new DB(DEFAULT_DB_NAME).find(sql, listener);        
    }


    /**
     * Executes DML. Use it for inserts and updates.
     *
     * @param query raw DML.
     * @return number of rows afected by query.
     */
    public static int exec(String query){
        return new DB(DEFAULT_DB_NAME).exec(query);
    }


    /**
     * Executes parametrized DML - will contain question marks as placeholders.
     *
     * @param query query to execute - will contain question marks as placeholders.
     * @param params  query parameters.
     * @return number of records affected.
     */
    public static int exec(String query, Object ... params){
        return new DB(DEFAULT_DB_NAME).exec(query, params);
    }
    

    /**
     * This method is specific for inserts.
     * 
     * @param query SQL for inserts.
     * @param autoIncrementColumnName name of a column that is auto-incremented.
     * @param params list of parameter values.
     * @return new value of auto-incremented column that is uniquely identifying a new record inserted. May return -1 if this
     * functionality is not supported by DB or driver.
     */
    static long execInsert(String query, String autoIncrementColumnName, Object... params) {
        return new DB(DEFAULT_DB_NAME).execInsert(query, autoIncrementColumnName, params);
    }

    /**
     * Opens local transaction.
     */
    public static void openTransaction() {
        new DB(DEFAULT_DB_NAME).openTransaction();
    }

    /**
     * Commits local transaction.
     */
    public static void commitTransaction() {
        new DB(DEFAULT_DB_NAME).commitTransaction();
    }

    /**
     * Rolls back local transaction.
     */
    public static void rollbackTransaction() {
        new DB(DEFAULT_DB_NAME).rollbackTransaction();
    }
}
