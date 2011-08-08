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

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.Map;
import java.util.Properties;


public class Base {

    private static final String DEFAULT_DB_NAME = "default";

    public static void open(String driver, String url, String user, String password) {
       establish(driver, url, user, password);
       MetaModel.acquire(DEFAULT_DB_NAME, false);
    }

    /**
     * Establish(not real connect) a connection to target
     *
     * @param driver the driver
     * @param url the url
     * @param user the user name
     * @param password the password
     */
    public static void establish(String driver, String url, String user, String password){
        establish(DEFAULT_DB_NAME, driver, url, user, password);
    }

    public static void establish(String dbName, final String driver, final String url, final String user, final String password){
        ConnectionsAccess.register(dbName, new ConnectionProvider(){
            public Connection getConnection(){
                try {
                    Class.forName(driver);
                    System.out.println("Establish connection to: " + url);
                    return DriverManager.getConnection(url, user, password);
                } catch (Exception e) {
                    throw new InitException(e);
                }
            }
        });
    }

    /**
     * Another way to open connection if we need to pass some driver-specific parameters
     *
     * @param driver driver class name
     * @param url JDBC URL
     * @param props connection properties
     */
    public static void open(String driver, String url, Properties props) {
        establish(driver, url, props);
        MetaModel.acquire(DEFAULT_DB_NAME, false);
    }

    public static void establish(final String driver, final String url, final Properties props) {
        establish(DEFAULT_DB_NAME, driver, url, props);
    }

    public static void establish(String dbName, final String driver, final String url, final Properties props){
        ConnectionsAccess.register(dbName, new ConnectionProvider(){
            public Connection getConnection(){
                try {
                    Class.forName(driver);
                    return DriverManager.getConnection(url, props);
                } catch (Exception e) {
                    throw new InitException(e);
                }
            }
        });
    }

    public static void open(String jndiName) {
        establish(jndiName);
        MetaModel.acquire(DEFAULT_DB_NAME, false);
    }

    public static void establish(final String jndiName){
        establish(DEFAULT_DB_NAME, jndiName);
    }

    public static void establish(String dbName, final String jndiName){
        ConnectionsAccess.register(dbName, new ConnectionProvider() {

            public Connection getConnection() {
                try {
                    Context ctx = new InitialContext();
                    DataSource ds = (DataSource) ctx.lookup(jndiName);
                    return ds.getConnection();
                } catch (Exception e) {
                    throw new InitException(e);
                }
            }
        });
    }

    public static void open(DataSource ds) {
        establish(ds);
        MetaModel.acquire(DEFAULT_DB_NAME, false);
    }


    public static Object establish(DataSource ds){
        establish(DEFAULT_DB_NAME, ds);
        return ds;
    }

    public static void establish(String dbName, final DataSource ds){
        ConnectionsAccess.register(dbName, new ConnectionProvider() {
            public Connection getConnection() {
                try {
                   return ds.getConnection();
                } catch (Exception e) {
                    throw new InitException(e);
                }
            }
        });
    }

    /**
     * Returns connection attached to a current thread and names "default".
     *
     *
     * @return connection attached to a current thread and names "default".
     */
    public static Connection connection() {
        return new DB(DEFAULT_DB_NAME).connection();

    }

    public static void close() {
        MetaModel.release(DEFAULT_DB_NAME);
    }




    public static Long count(String table){        
        return new DB(DEFAULT_DB_NAME).count(table);
    }

    public static Long count(String table, String query, Object... params) {
        return new DB(DEFAULT_DB_NAME).count(table, query, params);
    }


    /**
     * This method returns a value of the first column of the first row.
     * This query expects only one column selected in the select statement.
     * If more than one column returned, it will throw {@link IllegalArgumentException}.
     *
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

    public static void openTransaction() {
        new DB(DEFAULT_DB_NAME).openTransaction();
    }

    public static void commitTransaction() {
        new DB(DEFAULT_DB_NAME).commitTransaction();
    }

    public static void rollbackTransaction() {
        new DB(DEFAULT_DB_NAME).rollbackTransaction();
    }

}
