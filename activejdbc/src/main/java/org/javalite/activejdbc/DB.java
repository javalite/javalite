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

import org.javalite.activejdbc.connection_config.ConnectionDataSourceSpec;
import org.javalite.activejdbc.connection_config.ConnectionJdbcSpec;
import org.javalite.activejdbc.connection_config.ConnectionJndiSpec;
import org.javalite.activejdbc.connection_config.ConnectionSpec;
import org.javalite.activejdbc.logging.LogFilter;
import org.javalite.activejdbc.logging.LogLevel;
import org.javalite.common.Convert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.io.Closeable;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import static org.javalite.common.Util.closeQuietly;
import static org.javalite.common.Util.empty;

/**
 * This class provides a number of convenience methods for opening/closing database connections, running various
 * types of queries, and executing SQL statements. This class differs from {@link Base} such that in this class you
 * can provide a logical name for a current connection. Use this class when you have more than one database in the system.
 *
 * @author Igor Polevoy
 * @author Eric Nielsen
 */
public class DB implements Closeable{

    private static final Logger LOGGER = LoggerFactory.getLogger(DB.class);
    static final Pattern SELECT_PATTERN = Pattern.compile("^\\s*SELECT",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    static final Pattern INSERT_PATTERN = Pattern.compile("^\\s*INSERT",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    public static final String DEFAULT_NAME = "default";

    private final String name;

    /**
     * Creates a new DB object representing a connection to a DB.
     *
     * @param name logical name for a database.
     */
    public DB(String name) {
        this.name = name;
    }

    /**
     * Creates a new DB object representing a connection to a DB with default name.
     *
     * Calling this constructor is equivalent to <code>new DB(DB.DEFAULT_NAME)</code>.
     */
    public DB() {
        this.name = DEFAULT_NAME;
    }

    /**
     * Opens a new connection based on JDBC properties and attaches it to a current thread.
     *
     * @param driver class name of driver
     * @param url URL connection to DB
     * @param user user name.
     * @param password password.
     */
    public DB open(String driver, String url, String user, String password) {
        return open(driver, url, user, password, null);
    }

    /**
     * Opens a new connection in case additional driver-specific parameters need to be passed in.
     *
     * @param driver driver class name
     * @param url JDBC URL
     * @param props connection properties
     */
    public DB open(String driver, String url, Properties props) {
        return open(driver, url, null, null, props);
    }

    private DB open(String driver, String url, String user, String password, Properties properties){
        checkExistingConnection(name);
        try {
            Class.forName(driver);
            Connection connection;
            connection = properties == null ?  DriverManager.getConnection(url, user, password)
                    : DriverManager.getConnection(url, properties);
            LogFilter.log(LOGGER, LogLevel.DEBUG, "Opened connection: " + connection);
            ConnectionsAccess.attach(name, connection, url);
            return this;
        } catch (Exception e) {
            throw new InitException("Failed to connect to JDBC URL: " + url, e);
        }
    }

    /**
     * Opens a connection from JNDI based on a registered name. This assumes that there is a <code>jndi.properties</code>
     * file with proper JNDI configuration in it.
     *
     * @param jndiName name of a configured data source.
     */
    public DB open(String jndiName) {
        checkExistingConnection(name);
        try {
            Context ctx = new InitialContext();
            DataSource ds = (DataSource) ctx.lookup(jndiName);
            Connection connection = ds.getConnection();
            LogFilter.log(LOGGER, LogLevel.DEBUG, "Opened connection: " + connection);
            ConnectionsAccess.attach(name, connection, jndiName);
            return this;
        } catch (Exception e) {
            throw new InitException("Failed to connect to JNDI name: " + jndiName, e);
        }
    }


    /**
     * This method will open a connection defined in the file 'database.properties' located at
     * root of classpath. The connection picked up from the file is defined by <code>ACTIVE_ENV</code>
     * environment variable or <code>active_env</code> system property.
     * If this variable is not defined, it defaults to 'development' environment.
     *
     * <p></p>
     * If there is JUnit on classpath, this method assumes it is running under test, and defaults to 'test'.
     *
     * @see Configuration#getEnvironment()
     */
    public DB open(){
        Configuration config = Registry.instance().getConfiguration();
        ConnectionSpec spec = config.getCurrentConnectionSpec();
        if(spec == null){
            throw new DBException("Could not find configuration in a property file for environment: " + config.getEnvironment() +
                    ". Are you sure you have a database.properties file configured?");
        }
        return open(spec);
    }

    /**
     * Attaches a database connection to current thread under a name provided to constructor.
     *
     * @param connection instance of connection to attach to current thread.
     */
    public void attach(Connection connection) {
        ConnectionsAccess.attach(name, connection, "");
    }

    /**
     * Detaches a connection from current thread and returns an instance of it. This method does not close a connection.
     * Use it for cases of advanced connection management, such as integration with Spring Framework.
     *
     * @return instance of a connection detached from current thread by name passed to constructor.
     */
    public Connection detach() {
        Connection connection = ConnectionsAccess.getConnection(name);
        try {
            if (connection == null) {
                throw new DBException("cannot detach connection '" + name + "' because it is not available");
            }
            ConnectionsAccess.detach(name); // let's free the thread from connection
            StatementCache.instance().cleanStatementCache(connection);
        } catch (DBException e) {
            LogFilter.log(LOGGER, LogLevel.ERROR, "Could not close connection! MUST INVESTIGATE POTENTIAL CONNECTION LEAK!", e);
        }
        return connection;
    }

    /**
     * Opens a connection from a datasource. This methods gives a high level control while sourcing a DB connection.
     *
     * @param datasource datasource will be used to acquire a connection.
     */
    public DB open(DataSource datasource) {
        checkExistingConnection(name);
        try {
            Connection connection = datasource.getConnection();
            LogFilter.log(LOGGER, LogLevel.DEBUG, "Opened connection: " + connection);
            ConnectionsAccess.attach(name, connection, datasource.toString());
            return this;
        } catch (SQLException e) {
            throw new InitException(e);
        }
    }


    /**
     * Opens a new connection from JNDI data source by name using explicit JNDI properties. This method can be used in cases
     * when file <code>jndi.properties</code> cannot be easily updated.
     *
     * @param jndiName name of JNDI data source.
     * @param jndiProperties JNDI properties
     */
    public DB open(String jndiName, Properties jndiProperties) {
        checkExistingConnection(name);
        try {
            Context ctx = new InitialContext(jndiProperties);
            DataSource ds = (DataSource) ctx.lookup(jndiName);
            Connection connection = ds.getConnection();
            LogFilter.log(LOGGER, LogLevel.DEBUG, "Opened connection: " + connection);
            ConnectionsAccess.attach(name, connection,
                    jndiProperties.contains("url") ? jndiProperties.getProperty("url") : jndiName);
            return this;
        } catch (Exception e) {
            throw new InitException("Failed to connect to JNDI name: " + jndiName, e);
        }
    }


    /**
     * This method is used internally by framework.
     *
     * @param spec specification for a JDBC connection.
     */
    public DB open(ConnectionSpec spec) {
        checkExistingConnection(name);
        if (spec instanceof ConnectionJdbcSpec) {
            return openJdbc((ConnectionJdbcSpec) spec);
        } else if (spec instanceof ConnectionJndiSpec) {
            return openJndi((ConnectionJndiSpec) spec);
        } else if (spec instanceof ConnectionDataSourceSpec) {
            return openDataSource((ConnectionDataSourceSpec) spec);
        } else {
            throw new IllegalArgumentException("this spec not supported: " + spec.getClass());
        }
    }

    private void checkExistingConnection(String name) {
        if (null != ConnectionsAccess.getConnection(name)) {
            throw new DBException("Cannot open a new connection because existing connection is still on current thread, name: " + name + ", connection instance: " + connection()
            + ". This might indicate a logical error in your application.");
        }
    }

    /**
     * This method is used internally by framework.
     *
     * @param spec specification for a JDBC connection.
     */
    private DB openJdbc(ConnectionJdbcSpec spec) {

        if(spec.getProps()!= null){
            return open(spec.getDriver(), spec.getUrl(), spec.getProps());
        }else{
            return open(spec.getDriver(), spec.getUrl(), spec.getUser(), spec.getPassword());
        }
    }

    /**
     * This method is used internally by framework.
     *
     * @param spec specification for a JDBC connection.
     */
    private DB openJndi(ConnectionJndiSpec spec) {
        if(spec.getContext() != null){
            return openContext(spec.getContext(), spec.getDataSourceJndiName());
        }else{
            return open(spec.getDataSourceJndiName());
        }
    }

    private DB openDataSource(ConnectionDataSourceSpec spec) {
        return open(spec.getDataSource());
    }

    /**
     * This method is used internally by framework.
     *
     * @param context context.
     * @param jndiName JNDI name.
     */
    private DB openContext(InitialContext context, String jndiName) {
        try {
            DataSource ds = (DataSource) context.lookup(jndiName);
            Connection connection = ds.getConnection();
            LogFilter.log(LOGGER, LogLevel.DEBUG, "Opened connection: " + connection);
            ConnectionsAccess.attach(name, connection, jndiName);
            return this;
        } catch (Exception e) {
            throw new InitException("Failed to connect to JNDI name: " + jndiName, e);
        }
    }


    /**
     * Closes connection and detaches it from current thread.
     */
    public void close() {
        close(false);
    }

    /**
     * Closes connection and detaches it from current thread.
     * @param suppressWarning true to not display a warning in case of a problem (connection not there)
     */
    public void close(boolean suppressWarning) {
        try {
            Connection connection = ConnectionsAccess.getConnection(name);
            if(connection == null){
                throw new DBException("cannot close connection '" + name + "' because it is not available");
            }
            StatementCache.instance().cleanStatementCache(connection);
            connection.close();
            LogFilter.log(LOGGER, LogLevel.DEBUG, "Closed connection: {}", connection);
        } catch (Exception e) {
            if (!suppressWarning) {
                LOGGER.warn("Could not close connection! MUST INVESTIGATE POTENTIAL CONNECTION LEAK!", e);
            }
        } finally {
            ConnectionsAccess.detach(name); // let's free the thread from connection
        }
    }


    /**
     * Returns count of rows in table.
     *
     * @param table name of table.
     * @return count of rows in table.
     */
    public Long count(String table){
        String sql = "SELECT COUNT(*) FROM " + table ;
        return Convert.toLong(firstCell(sql));
    }

    /**
     * Runs a count query, returns a number of matching records.
     *
     * @param table table in which to count rows.
     * @param query this is a filtering query for the count. If '*' provided, all records will be counted. Example:
     * <code>"age > 65 AND department = 'accounting'"</code>
     * @param params parameters for placeholder substitution.
     * @return count number of records found in a table.
     */
    public Long count(String table, String query, Object... params) {

        if (query.trim().equals("*")) {
            if (empty(params)) {
                return count(table);
            } else {
                throw new IllegalArgumentException("cannot use '*' and parameters");
            }
        }

        String sql = "SELECT COUNT(*) FROM " + table + " WHERE " + query;
        return Convert.toLong(firstCell(sql, params));
    }

    /**
     * This method returns the value of the first column of the first row.
     * If query results have more than one column or more than one row, they will be ignored.
     *
     * @param query query
     * @param params parameters
     * @return fetched value, or null if query did not fetch anything.
     */
    public Object firstCell(final String query, Object... params) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            Object result = null;
            long start = System.currentTimeMillis();
            ps = connection().prepareStatement(query);
            setParameters(ps, params);
            rs = ps.executeQuery();
            if (rs.next()) {
                result = rs.getObject(1);
            }
            LogFilter.logQuery(LOGGER, query, params, start);
            return result;
        } catch (SQLException e) {
            throw new DBException(query, params, e);
        } finally {
            closeQuietly(rs);
            closeQuietly(ps);
        }
    }

    /**
     * Alias to {@link #findAll(String, Object...)}
     */
    public List<Map> all(String query, Object ... params) {
        return findAll(query, params);
    }

    /**
     * This method returns entire resultset as one list. Do not use it for large result sets.
     * Example:
     * <pre>
     * <code>List&lt;Map&lt;String, Object&gt;&gt; people = Base.findAll(&quot;select * from people where first_name = ?&quot;, &quot;John&quot;);
     *  for(Map person: people)
     *      System.out.println(person.get(&quot;first_name&quot;));
     * </code>
     * </pre>
     *
     * @param query raw SQL query. This query is parametrized.
     * @param params list of parameters for a parametrized query.
     * @return entire result set corresponding to the query.
     */
    public List<Map> findAll(String query, Object ... params) {

        final List<Map> results = new ArrayList<>();
        long start = System.currentTimeMillis();
        find(query, params).with(new RowListenerAdapter() {
            @Override public void onNext(Map<String, Object> row) {
                results.add(row);
            }
        });
        LogFilter.logQuery(LOGGER, query, params, start);
        return results;
    }

    /**
     * This method returns entire resultset with one column as a list. Do not use it for large result sets.
     * Example:
     * <pre>
     *  List ssns = Base.firstColumn("select ssn from people where first_name = ?", "John");
     *  for(Object ssn: ssns)
     *      System.out.println(ssn);
     * </pre>
     *
     * This method collects the value of the first column of each row. If query results have more than one column, the
     * remainder will be ignored.
     *
     * @param query raw SQL query. This query can be parametrized.
     * @param params list of parameters for a parametrized query.
     * @return entire result set corresponding to the query.
     */
    public List firstColumn(String query, Object... params) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            List<Object> results = new ArrayList<>();
            long start = System.currentTimeMillis();
            ps = connection().prepareStatement(query);
            setParameters(ps, params);
            rs = ps.executeQuery();
            while (rs.next()) {
                results.add(rs.getObject(1));
            }
            LogFilter.logQuery(LOGGER, query, params, start);
            return results;
        } catch (SQLException e) {
            throw new DBException(query, params, e);
        } finally {
            closeQuietly(rs);
            closeQuietly(ps);
        }
    }

    /**
     * Alias to {@link #findAll(String)}
     */
    public List<Map> all(String query) {
        return findAll(query);
    }

    /**
     * This method returns entire resultset as one list. Do not use it for large result sets.
     *
     * @param query raw SQL query. This query is not parametrized.
     * @return entire result set corresponding to the query.
     */
    public List<Map> findAll(String query) {

        final ArrayList<Map> results = new ArrayList<>();
        long start = System.currentTimeMillis();
        find(query).with(new RowListenerAdapter() {
            @Override public void onNext(Map<String, Object> row) {
                results.add(row);
            }
        });

        LogFilter.logQuery(LOGGER, query, null, start);
        return results;
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
    public RowProcessor find(String query, Object ... params) {

        //TODO: count ? signs and number of params, throw exception if do not match

        if(query.indexOf('?') == -1 && params.length != 0) throw new IllegalArgumentException("you passed arguments, but the query does not have placeholders: (?)");

        //TODO: cache prepared statements here too
        PreparedStatement ps;
        ResultSet rs;
        try {
            ps = createStreamingPreparedStatement(query);
            setParameters(ps, params);
            rs = ps.executeQuery();
            return new RowProcessor(rs, ps);

        } catch (SQLException e) { throw new DBException(query, params, e); }
    }

    private PreparedStatement createStreamingPreparedStatement(String query) throws SQLException {
        Connection conn = connection();
        PreparedStatement res;
        if ("mysql".equalsIgnoreCase(conn.getMetaData().getDatabaseProductName())) {
            res = conn.prepareStatement(query, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            res.setFetchSize(Integer.MIN_VALUE);
        } else {
            res = conn.prepareStatement(query);
        }
        return res;
    }

    /**
     * Executes a raw query and calls instance of <code>RowListener</code> with every row found.
     * Use this method for very large result sets.
     *
     * @param sql raw SQL query.
     * @param listener client listener implementation for processing individual rows.
     */
    public void find(String sql, RowListener listener) {

        Statement s = null;
        ResultSet rs = null;
        try {
            s = createStreamingStatement();
            rs = s.executeQuery(sql);
            RowProcessor p = new RowProcessor(rs, s);
            p.with(listener);
        } catch (SQLException e) {
            throw new DBException(sql, null, e);
        } finally {
            closeQuietly(rs);
            closeQuietly(s);
        }
    }

    private Statement createStreamingStatement() throws SQLException {
        Connection conn = connection();
        Statement res;
        if ("mysql".equalsIgnoreCase(conn.getMetaData().getDatabaseProductName())) {
            res = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            res.setFetchSize(Integer.MIN_VALUE);
        } else {
            res = conn.createStatement();
        }
        return res;
    }

    /**
     * Executes DML. Use it for inserts and updates.
     *
     * @param query raw DML.
     * @return number of rows afected by query.
     */
    public int exec(String query){
        long start = System.currentTimeMillis();
        Statement s = null;
        try {
            s = connection().createStatement();
            int count = s.executeUpdate(query);
            LogFilter.logQuery(LOGGER, query, null, start);
            return count;
        } catch (SQLException e) {
            logException("Query failed: " + query, e);
            throw new DBException(query, null, e);
        } finally {
            closeQuietly(s);
        }
    }


    /**
     * Executes parametrized DML - will contain question marks as placeholders.
     *
     * @param query query to execute - will contain question marks as placeholders.
     * @param params  query parameters.
     * @return number of records affected.
     */
    public int exec(String query, Object... params){

        if(query.indexOf('?') == -1) throw new IllegalArgumentException("query must be parametrized");

        long start = System.currentTimeMillis();
        PreparedStatement ps = null;
        try {
            ps = connection().prepareStatement(query);
            setParameters(ps, params);
            int count = ps.executeUpdate();
            LogFilter.logQuery(LOGGER, query, params, start);
            return count;
        } catch (SQLException e) {
            logException("Failed query: " + query, e);
            throw new DBException(query, params, e);
        } finally {
            closeQuietly(ps);
        }

    }


    /**
     * This method is specific for inserts.
     *
     * @param query SQL for inserts.
     * @param autoIncrementColumnName name of a column that is auto-incremented.
     * @param params list of parameter values.
     * @return new value of auto-incremented column that is uniquely identifying a new record inserted. May return -1 if this
     * functionality is not supported by DB or driver. Returns null if inserted rows count if not 1.
     */
    Object execInsert(String query, String autoIncrementColumnName, Object... params) {
        if (!INSERT_PATTERN.matcher(query).find())
            throw new IllegalArgumentException("this method is only for inserts");

        long start = System.currentTimeMillis();
        PreparedStatement ps;
        try {
            Connection connection = connection();
            ps = StatementCache.instance().getPreparedStatement(connection, query);
            if(ps == null){
                ps = connection.prepareStatement(query, new String[]{autoIncrementColumnName});
                StatementCache.instance().cache(connection, query, ps);
            }
            for (int index = 0; index < params.length;) {
                Object param = params[index++];
                if (param instanceof byte[]) {
                    byte[] bytes = (byte[]) param;
                    try {
                        Blob blob = connection.createBlob();
                        if (blob == null) { // SQLite
                            ps.setBytes(index, bytes);
                        } else {
                            blob.setBytes(1, bytes);
                            ps.setBlob(index, blob);
                        }
                    } catch (AbstractMethodError | SQLException e) {// net.sourceforge.jtds.jdbc.ConnectionJDBC2.createBlob is abstract :)
                        ps.setObject(index, param);
                    }
                } else {
                    ps.setObject(index, param);
                }
            }

            if (ps.executeUpdate() != 1) {
                return null;
            }

            ResultSet rs = null;
            try{
                rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    Object id = rs.getObject(1);
                    LogFilter.logQuery(LOGGER, query, params, start);
                    return id;
                } else {
                    return -1;
                }
            } catch (SQLException e) {
                LOGGER.error("Failed to find out the auto-incremented value, returning -1, query: {}", query, e);
                return -1;
            } finally {
                closeQuietly(rs);
            }
        } catch (SQLException e) {
            throw new DBException(query, params, e);
        } finally {
            // don't close ps as it could have come from the cache!
            //TODO: close ps if not cached?
        }
    }

    private void logException(String message, Exception e) {
        if (LOGGER.isErrorEnabled() && Convert.toBoolean(System.getProperty("activejdbc.log_exception")))
            LOGGER.error(message, e);
    }

    /**
     * Opens local transaction.
     */
    public  void openTransaction() {
        try {
            Connection c = ConnectionsAccess.getConnection(name);
            if (c == null) {
                throw new DBException("Cannot open transaction, connection '" + name + "' not available");
            }
            c.setAutoCommit(false);
            LogFilter.log(LOGGER, LogLevel.DEBUG, "Transaction opened");
        } catch (SQLException ex) {
            throw new DBException(ex.getMessage(), ex);
        }
    }


    /**
     * Commits local transaction.
     */
    public void commitTransaction() {
        try {
            Connection c = ConnectionsAccess.getConnection(name);
            if (c == null) {
                throw new DBException("Cannot commit transaction, connection '" + name + "' not available");
            }
            c.commit();
            LogFilter.log(LOGGER, LogLevel.DEBUG, "Transaction committed");
        } catch (SQLException ex) {
            throw new DBException(ex.getMessage(), ex);
        }
    }

    /**
     * Rolls back local transaction.
     */
    public void rollbackTransaction() {
        try {
            Connection c = ConnectionsAccess.getConnection(name);
            if (c == null) {
                throw new DBException("Cannot rollback transaction, connection '" + name + "' not available");
            }
            c.rollback();
            LogFilter.log(LOGGER, LogLevel.DEBUG, "Transaction rolled back");
        } catch (SQLException ex) {
            throw new DBException(ex.getMessage(), ex);
        }
    }

    /**
     * Provides connection from current thread.
     *
     * @return connection from current thread.
     */
    public Connection connection() {
        Connection connection = ConnectionsAccess.getConnection(name);
        if (connection == null) {
            throw new DBException("there is no connection '" + name + "' on this thread, are you sure you opened it?");
        }
        return connection;
    }

    /**
     * Use to check if there is a connection present on current thread.
     *
     * @return true if finds connection on current thread, false if not.
     */
    public boolean hasConnection() {
        return null != ConnectionsAccess.getConnection(name);
    }

    /**
     * Synonym of {@link #connection()} for people who like getters.
     *
     * @return connection from current thread.
     */
    public Connection getConnection(){
        return connection();
    }


    /**
     * Provides a names' list of current connections.
     * 
     * @return a names' list of current connections.
     */
    public static List<String> getCurrrentConnectionNames(){
        return new ArrayList<>(ConnectionsAccess.getConnectionMap().keySet());
    }

    /**
     * Closes all current connections.
     */
    public static void closeAllConnections(){
        List<String> names = getCurrrentConnectionNames();
        for(String name: names){
            new DB(name).close();
        }
    }


    /**
     * Provides connections available on current thread.
     *
     * @return  connections available on current thread.
     */
    public static Map<String, Connection> connections(){
        return ConnectionsAccess.getConnectionMap();
    }

    /**
     * Creates a <code>java.sql.PreparedStatement</code> to be used in batch executions later.
     *
     * @param parametrizedStatement Example of a statement: <code>INSERT INTO employees VALUES (?, ?)</code>.
     * @return instance of <code>java.sql.PreparedStatement</code> with compiled query.
     */
    public PreparedStatement startBatch(String parametrizedStatement){
        try {
            return connection().prepareStatement(parametrizedStatement);
        } catch (SQLException e) {
            throw new DBException(e);
        }
    }

    /**
     * Adds a batch statement using given <code>java.sql.PreparedStatement</code> and parameters.
     * @param ps <code>java.sql.PreparedStatement</code> to add batch to.
     * @param params parameters for the query in <code>java.sql.PreparedStatement</code>. Parameters will be
     * set on the statement in the same order as provided here.
     */
    public void addBatch(PreparedStatement ps, Object... params) {
        try {
            setParameters(ps, params);
            ps.addBatch();
        } catch (SQLException e) {
            throw new DBException(e);
        }
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
    public int[] executeBatch(PreparedStatement ps){
        try {
            int[] counters = ps.executeBatch();
            ps.clearParameters();
            return counters;
        } catch (SQLException e) {
            throw new DBException(e);
        }
    }

    private void setParameters(PreparedStatement ps, Object... params) throws SQLException {
        for (int index = 0; index < params.length;) {
            Object param = params[index++];
            ps.setObject(index, param);
        }
    }
}
