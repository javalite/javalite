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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import static activejdbc.LogFilter.*;

/**
 * @author Igor Polevoy
 */
public class DB {
    
    private String dbName;
    final static Logger logger = LoggerFactory.getLogger(DB.class);

    public DB(String dbName){
        this.dbName = dbName;
    }

    public void open(String driver, String url, String user, String password) {
        try {
            Class.forName(driver);
            Connection connection = DriverManager.getConnection(url, user, password);
            ConnectionsAccess.attach(dbName, connection);
        } catch (Exception e) {
            throw new InitException(e);
        }
    }

    /**
     * Another way to open connection if we need to pass some driver-specific parameters
     * @param driver driver class name
     * @param url JDBC URL
     * @param props connection properties
     */
    public void open(String driver, String url, Properties props) {
        try {
            Class.forName(driver);
            Connection connection = DriverManager.getConnection(url, props);
            ConnectionsAccess.attach(dbName, connection);
        } catch (Exception e) {
            throw new InitException(e);
        }
    }

    public void open(String jndiName) {
        try {
            Context ctx = new InitialContext();
            DataSource ds = (DataSource) ctx.lookup(jndiName);
            Connection connection = ds.getConnection();
            ConnectionsAccess.attach(dbName, connection);
        } catch (Exception e) {
            throw new InitException(e);
        }
    }

    public void open(String jndiName, Properties jndiProperties) {
        try {
            Context ctx = new InitialContext(jndiProperties);
            DataSource ds = (DataSource) ctx.lookup(jndiName);
            Connection connection = ds.getConnection();
            ConnectionsAccess.attach(dbName, connection);
        } catch (Exception e) {
            throw new InitException(e);
        }
    }


    public void open(ConnectionSpec spec){
        if(spec instanceof ConnectionJdbcSpec){
            openJdbc((ConnectionJdbcSpec)spec);
        }else if(spec instanceof ConnectionJndiSpec){
            openJndi((ConnectionJndiSpec)spec);
        }else{
            throw new IllegalArgumentException("this spec not supported: " + spec.getClass());
        }
    }

    private void openJdbc(ConnectionJdbcSpec spec) {

        if(spec.getProps()!= null){
            open(spec.getDriver(), spec.getUrl(), spec.getProps());
        }else{
            open(spec.getDriver(), spec.getUrl(), spec.getUser(), spec.getPassword());
        }
    }

    private void openJndi(ConnectionJndiSpec spec) {
        if(spec.getContext() != null){
            openContext(spec.getContext(), spec.getDataSourceJndiName());
        }else{
            open(spec.getDataSourceJndiName());
        }
    }

    private void openContext(InitialContext context, String jndiName) {
        try {         
            DataSource ds = (DataSource) context.lookup(jndiName);
            Connection connection = ds.getConnection();
            ConnectionsAccess.attach(dbName, connection);
        } catch (Exception e) {
            throw new InitException(e);
        }
    }

    public void close() {
        try {
            Connection connection = ConnectionsAccess.getConnectionMap().get(dbName);
            StatementCache.instance().cleanStatementCache(connection);
            connection.close();
            log(logger, "Closed connection: " + connection);
            ConnectionsAccess.getConnectionMap().remove(dbName);
        } catch (Exception e) {
            logException("Could not close connection", e);
        }
    }

    
    public Long count(String table){
        String sql = "SELECT COUNT(*) FROM " + table ;
        return Converter.toLong(firstCell(sql));
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
    public Long count(String table, String query, Object... params) {

        if(query.trim().equals("*") && params.length == 0){
            return count(table);
        }
        if(query.trim().equals("*") && params.length != 0){
            throw new IllegalArgumentException("cannot use '*' and parameters");
        }

        String sql = "SELECT COUNT(*) FROM " + table + " WHERE " + query;
        return Converter.toLong(firstCell(sql, params));
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
    public Object firstCell(String query, Object... params) {

        List<Map> list = findAll(query, params);
        if(list.size() == 0) return null;

        Map map = list.get(0);
        if(map.size() > 1)
            throw new IllegalArgumentException("query: " + query + " selects more than one column");

        return map.get(map.keySet().toArray()[0]);
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
    public List<Map> findAll(String query, Object ... params) {

        long start = System.currentTimeMillis();
        final List<Map> results = new ArrayList<Map>();
        find(query, params).with(new RowListenerAdapter() {
            public void onNext(Map<String, Object> row) {
                results.add(row);
            }
        });
        LogFilter.logQuery(logger, query, params, start);
        return results;
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
    public List firstColumn(String query, Object ... params) {

        final List results = new ArrayList();
        long start = System.currentTimeMillis();
        find(query, params).with(new RowListenerAdapter() {
            public void onNext(Map<String, Object> row) {
                if(row.size() > 1) throw new IllegalArgumentException("Query selects more than one column");

                results.add(row.get(row.keySet().toArray()[0]));
            }
        });

        LogFilter.logQuery(logger, query, params, start);
        return results;
    }

    /**
     * This method returns entire resultset as one list. Do not use it for large result sets.
     *
     * @param query raw SQL query. This query is not parametrized.
     * @return entire result set corresponding to the query.
     */
    public List<Map> findAll(String query) {

        final ArrayList<Map> results = new ArrayList<Map>();
        long start = System.currentTimeMillis();
        find(query).with(new RowListenerAdapter() {
            public void onNext(Map<String, Object> row) {
                results.add(row);
            }
        });

        LogFilter.logQuery(logger, query, null, start);
        return results;
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
    public RowProcessor find(String query, Object ... params) {

        //TODO: count ? signs and number of params, throw exception if do not match

        if(query.indexOf('?') != -1 && params.length == 0) throw new IllegalArgumentException("you have placeholders (?) in the query, but no arguments are passed");
        if(query.indexOf('?') == -1 && params.length != 0) throw new IllegalArgumentException("you passed arguments, but the query does not have placeholders: (?)");
        if(!query.toLowerCase().contains("select"))throw new IllegalArgumentException("query must be 'select' query");

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection().prepareStatement(query);
            for (int index = 0; index < params.length; index++) {
                Object param = params[index];
                ps.setObject(index + 1, param);
            }

            rs = ps.executeQuery();
            return new RowProcessor(rs, ps);

        } catch (Exception e) {throw new DBException(query, params, e);}

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
            s = connection().createStatement();
            rs = s.executeQuery(sql);
            RowProcessor p = new RowProcessor(rs, s);
            p.with(listener);
        }
        catch (Exception e) {
            throw new DBException(sql, null, e);
        }
        finally{try{rs.close();}catch(Exception e){/*ignore*/} try{s.close();}catch(Exception e){/*ignore*/}}
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
            LogFilter.logQuery(logger, query, null, start);
            return count;
        } catch (SQLException e) {
            logger.error("Query failed: " + query, e);
            throw new DBException(query, null, e);
        }
        finally{try{s.close();}catch(Exception e){/*ignore*/}}
    }



    public  int exec(String query, Object ... params){

        if(query.trim().toLowerCase().startsWith("select")) throw new IllegalArgumentException("expected DML, but got select...");

        if(query.indexOf('?') == -1) throw new IllegalArgumentException("query must be parametrized");

        long start = System.currentTimeMillis();
        PreparedStatement ps = null;
        try {
            ps = connection().prepareStatement(query);
            for (int index = 0; index < params.length; index++) {
                Object param = params[index];
                ps.setObject(index + 1, param);
            }
            int count =  ps.executeUpdate();
            LogFilter.logQuery(logger, query, params, start);
            return count;
        } catch (Exception e) {
            throw new DBException(query, params, e);
        }
        finally{try{ps.close();}catch(Exception e){/*ignore*/}}

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
    long execInsert(String query, String autoIncrementColumnName, Object... params) {

        if (!query.toLowerCase().contains("insert"))
            throw new IllegalArgumentException("this method is only for inserts");

        long start = System.currentTimeMillis();
        PreparedStatement ps = null;
        try {
            Connection connection = connection();
            ps = StatementCache.instance().getPreparedStatement(connection, query);
            if(ps == null){                
                ps = connection.prepareStatement(query, new String[]{autoIncrementColumnName});
                StatementCache.instance().cache(connection, query, ps);
            }
            for (int index = 0; index < params.length; index++) {
                Object param = params[index];
                ps.setObject(index + 1, param);
            }
            ps.executeUpdate();

            ResultSet rs = null;
            try{
                rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    long id = rs.getLong(1);
                    LogFilter.logQuery(logger, query, params, start);
                    return id;
                } else {
                    return -1;
                }
            }
            catch(Exception e){
                logException("Failed to find out the auto-incremented value, returning -1, query: " + query, e);
                return -1;
            }finally{try{rs.close();}catch(Exception e){/*ignore*/}}
        } catch (Exception e) {
            throw new DBException(query, params, e);
        }
    }

    private void logException(String message, Exception e){
        logger.error(message, e);
    }

    public  void openTransaction() {
        try {
            ConnectionsAccess.getConnection(dbName).setAutoCommit(false);
            log(logger, "Transaction opened");
        } catch (SQLException ex) {
            throw new DBException(ex.getMessage(), ex);
        }
    }


    public void commitTransaction() {
        try {
            ConnectionsAccess.getConnection(dbName).commit();
            log(logger, "Transaction committed");
        } catch (SQLException ex) {
            throw new DBException(ex.getMessage(), ex);
        }
    }

    public void rollbackTransaction() {
        try {
            ConnectionsAccess.getConnection(dbName).rollback();
            log(logger, "Transaction rolled back");
        } catch (SQLException ex) {
            throw new DBException(ex.getMessage(), ex);
        }
    }

    public Connection connection() {
        return ConnectionsAccess.getConnection(dbName);
    }

    public Connection getConnection(){        
        return connection();
    }
}
