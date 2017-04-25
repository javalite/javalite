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

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;
import java.util.Properties;


/**
 * This class provides a number of convenience methods for opening/closing database connections, running various
 * types of queries, and executing SQL statements. This class differs from {@link DB} such that
 * in this class you a logical name for a connection is hard-coded to be "default". Use this class when you have
 * only one database.
 * <p></p>
 * This class is a convenience wrapper of {@link DB}
 *
 * @author Igor Polevoy
 * @author Eric Nielsen
 */
public class Base {

    private Base() {}
    
    /**
     * This method will open a connection defined in the file 'database.properties' located at
     * root of classpath. The connection picked from the file is defined by <code>ACTIVE_ENV</code>
     * environment variable. If this variable is not defined, it defaults to 'development' environment.
     *
     * If there is JUnit on classpath, this method assumes it is running under test, and defaults to 'test'.
     *
     */
    public static DB open(){
        return new DB(DB.DEFAULT_NAME).open();
    }

    /**
     * Opens a new connection based on JDBC properties and attaches it to a current thread.
     *
     * @param driver class name of driver
     * @param url URL connection to DB
     * @param user user name.
     * @param password password.
     */
    public static DB open(String driver, String url, String user, String password) {
       return new DB(DB.DEFAULT_NAME).open(driver, url, user, password);
    }


    /**
     * Opens a new connection in case additional driver-specific parameters need to be passed in.
     *
     * @param driver driver class name
     * @param url JDBC URL
     * @param props connection properties
     */
    public static DB open(String driver, String url, Properties props) {
        return new DB(DB.DEFAULT_NAME).open(driver, url, props);
    }

    /**
     * Opens a connection from JNDI based on a registered name. This assumes that there is a <code>jndi.properties</code>
     * file with proper JNDI configuration in it.
     *
     * @param jndiName name of a configured data source.
     */
    public static DB open(String jndiName) {
        return new DB(DB.DEFAULT_NAME).open(jndiName);
    }

    /**
     * Opens a new connection from JNDI data source by name using explicit JNDI properties. This method can be used in cases
     * when file <code>jndi.properties</code> cannot be easily modified.
     *
     * @param jndiName name of JNDI data source.
     * @param jndiProperties JNDI properties
     */
    public static DB open(String jndiName, Properties jndiProperties) {
        return new DB(DB.DEFAULT_NAME).open(jndiName, jndiProperties);
    }

    /**
     * Opens a connection from a datasource. This methods gives a high level control while sourcing a DB connection.
     *
     * @param dataSource datasource will be used to acquire a connection.
     */
    public static DB open(DataSource dataSource) {
        return new DB(DB.DEFAULT_NAME).open(dataSource);
    }


    /**
     * Returns connection attached to a current thread and named "default".
     *
     * @return connection attached to a current thread and named "default".
     */
    public static Connection connection() {
        return new DB(DB.DEFAULT_NAME).connection();

    }


    /**
     * Use to check if there is a default connection present on current thread.
     *
     * @return true if finds default connection on current thread, false if not.
     */
    public static boolean hasConnection() {
        return new DB(DB.DEFAULT_NAME).hasConnection();
    }



    /**
     * Closes connection and detaches it from current thread.
     * @param suppressWarning true to not display a warning in case of a problem (connection not there)
     */
    public static void close(boolean suppressWarning) {
        new DB(DB.DEFAULT_NAME).close(suppressWarning);
    }

    /**
     * Closes connection and detaches it from current thread.
     */
    public static void close() {
      new DB(DB.DEFAULT_NAME).close();
    }

    /**
     * Returns count of rows in table.
     *
     * @param table name of table.
     * @return count of rows in table.
     */
    public static Long count(String table) {
        return new DB(DB.DEFAULT_NAME).count(table);
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
        return new DB(DB.DEFAULT_NAME).count(table, query, params);
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
        return new DB(DB.DEFAULT_NAME).firstCell(query, params);
    }

    /**
     * This method returns entire resultset as one list. Do not use it for large result sets.
     * Example:
     * <pre>
     * List&lt;Map&gt; people = Base.findAll(&quot;select * from people where first_name = ?&quot;, &quot;John&quot;);
     * for (Map person : people)
     *     System.out.println(person.get("first_name"));
     * </pre>
     *
     * @param query raw SQL query. This query is parametrized.
     * @param params list of parameters for a parametrized query.
     * @return entire result set corresponding to the query.
     */
    public static List<Map> findAll(String query, Object... params) {
        return new DB(DB.DEFAULT_NAME).findAll(query, params);
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
    public static List firstColumn(String query, Object... params) {
        return new DB(DB.DEFAULT_NAME).firstColumn(query, params);
    }

    /**
     * This method returns entire resultset as one list. Do not use it for large result sets.
     *
     * @param query raw SQL query. This query is not parametrized.
     * @return entire result set corresponding to the query.
     */
    public static List<Map> findAll(String query) {
        return new DB(DB.DEFAULT_NAME).findAll(query);
    }

    /**
     * Executes a raw query and returns an instance of {@link RowProcessor}. Use it in the following pattern:
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
      return new DB(DB.DEFAULT_NAME).find(query, params);       
    }

    /**
     * Executes a raw query and calls instance of <code>RowListener</code> with every row found.
     * Use this method for very large result sets.
     *
     * @param sql raw SQL query.
     * @param listener client listener implementation for processing individual rows.
     */
    public static void find(String sql, RowListener listener) {
        new DB(DB.DEFAULT_NAME).find(sql, listener);        
    }


    /**
     * Executes DML. Use it for inserts and updates.
     *
     * @param query raw DML.
     * @return number of rows afected by query.
     */
    public static int exec(String query){
        return new DB(DB.DEFAULT_NAME).exec(query);
    }


    /**
     * Executes parametrized DML - will contain question marks as placeholders.
     *
     * @param query query to execute - will contain question marks as placeholders.
     * @param params  query parameters.
     * @return number of records affected.
     */
    public static int exec(String query, Object ... params){
        return new DB(DB.DEFAULT_NAME).exec(query, params);
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
    static Object execInsert(String query, String autoIncrementColumnName, Object... params) {
        return new DB(DB.DEFAULT_NAME).execInsert(query, autoIncrementColumnName, params);
    }

    /**
     * Opens local transaction.
     */
    public static void openTransaction() {
        new DB(DB.DEFAULT_NAME).openTransaction();
    }

    /**
     * Commits local transaction.
     */
    public static void commitTransaction() {
        new DB(DB.DEFAULT_NAME).commitTransaction();
    }

    /**
     * Rolls back local transaction.
     */
    public static void rollbackTransaction() {
        new DB(DB.DEFAULT_NAME).rollbackTransaction();
    }

    /**
     * Creates a <code>java.sql.PreparedStatement</code> to be used in batch executions later.
     *
     * @param parametrizedStatement Example of a statement: <code>INSERT INTO employees VALUES (?, ?)</code>.
     * @return instance of <code>java.sql.PreparedStatement</code> with compiled query.
     */
    public static PreparedStatement startBatch(String parametrizedStatement) {
        return new DB(DB.DEFAULT_NAME).startBatch(parametrizedStatement);
    }

    /**
     * Adds a batch statement using given <code>java.sql.PreparedStatement</code> and parameters.
     * @param ps <code>java.sql.PreparedStatement</code> to add batch to.
     * @param parameters parameters for the query in <code>java.sql.PreparedStatement</code>. Parameters will be
     * set on the statement in the same order as provided here.
     */
    public static void addBatch(PreparedStatement ps, Object... parameters) {
        new DB(DB.DEFAULT_NAME).addBatch(ps, parameters);
    }

    /**
     * Executes a batch on <code>java.sql.PreparedStatement</code>.
     *
     * @param ps <code>java.sql.PreparedStatement</code> to execute batch on.
     *
     * @return an array of update counts containing one element for each command in the batch.
     * The elements of the array are ordered according to the order in which commands were added to the batch.
     *
     * @see <a href="http://docs.oracle.com/javase/7/docs/api/java/sql/Statement.html#executeBatch()">Statement#executeBatch()</a>
     */
    public static int[] executeBatch(PreparedStatement ps) {
        return new DB(DB.DEFAULT_NAME).executeBatch(ps);
    }


    /**
     * Attaches a database connection to current thread under a default name.
     *
     * @param connection instance of connection to attach to current thread.
     */
    public static void attach(Connection connection) {
        new DB(DB.DEFAULT_NAME).attach(connection);
    }

    /**
     * Detaches a default connection from current thread and returns an instance of it. This method does not close a connection.
     * Use it for cases of advanced connection management, such as integration with Spring Framework.
     *
     * @return instance of a default connection detached from current thread by name passed to constructor.
     */
    public static Connection detach() {
        return new DB(DB.DEFAULT_NAME).detach();
    }
}
