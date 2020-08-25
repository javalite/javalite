package org.javalite.cassandra.jdbc;

import com.datastax.oss.driver.api.core.CqlSession;

import java.sql.*;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

//TODO: convert this to InvocationHandler
public class CassandraJDBCConnection implements Connection {

    private CqlSession session;

    public CassandraJDBCConnection(CqlSession session) {
        this.session = session;
    }

    @Override
    public Statement createStatement() throws SQLException {
        return new CassandraJDBCStatement(session);
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
          return new CassandraJDBCPreparedStatement( session, sql);
    }

    @Override
    public CallableStatement prepareCall(String sql) throws SQLException {
        throw  new UnsupportedOperationException();
    }

    @Override
    public String nativeSQL(String sql) throws SQLException {
        throw  new UnsupportedOperationException();
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        //NOPE operation
    }

    @Override
    public boolean getAutoCommit() throws SQLException {
        throw  new UnsupportedOperationException();
    }

    @Override
    public void commit() throws SQLException {
        //do nothing, Cassandra does not seem support transactions.
    }

    @Override
    public void rollback() throws SQLException {
        //DO NOTHING
    }

    @Override
    public void close() throws SQLException {
        session.close();
    }

    @Override
    public boolean isClosed() throws SQLException {
        return session.isClosed();
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        throw  new UnsupportedOperationException();
    }

    @Override
    public void setReadOnly(boolean readOnly) throws SQLException {
        throw  new UnsupportedOperationException();
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        throw  new UnsupportedOperationException();
    }

    @Override
    public void setCatalog(String catalog) throws SQLException {
        throw  new UnsupportedOperationException();
    }

    @Override
    public String getCatalog() throws SQLException {
        throw  new UnsupportedOperationException();
    }

    @Override
    public void setTransactionIsolation(int level) throws SQLException {

        throw  new UnsupportedOperationException();
    }

    @Override
    public int getTransactionIsolation() throws SQLException {
        throw  new UnsupportedOperationException();
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        throw  new UnsupportedOperationException();
    }

    @Override
    public void clearWarnings() throws SQLException {

    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        return new CassandraJDBCStatement(this.session);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return prepareStatement(sql);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        throw  new UnsupportedOperationException();
    }

    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        throw  new UnsupportedOperationException();
    }

    @Override
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        throw  new UnsupportedOperationException();
    }

    @Override
    public void setHoldability(int holdability) throws SQLException {
        throw  new UnsupportedOperationException();
    }

    @Override
    public int getHoldability() throws SQLException {
        throw  new UnsupportedOperationException();
    }

    @Override
    public Savepoint setSavepoint() throws SQLException {
        throw  new UnsupportedOperationException();
    }

    @Override
    public Savepoint setSavepoint(String name) throws SQLException {
        throw  new UnsupportedOperationException();
    }

    @Override
    public void rollback(Savepoint savepoint) throws SQLException {
        throw  new UnsupportedOperationException();
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        throw  new UnsupportedOperationException();
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        throw  new UnsupportedOperationException();
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        throw  new UnsupportedOperationException();
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        throw  new UnsupportedOperationException();
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        throw  new UnsupportedOperationException();
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        throw  new UnsupportedOperationException();
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        throw  new UnsupportedOperationException();
    }

    @Override
    public Clob createClob() throws SQLException {
        throw  new UnsupportedOperationException();
    }

    @Override
    public Blob createBlob() throws SQLException {
        throw  new UnsupportedOperationException();
    }

    @Override
    public NClob createNClob() throws SQLException {
        throw  new UnsupportedOperationException();
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
        throw  new UnsupportedOperationException();
    }

    @Override
    public boolean isValid(int timeout) throws SQLException {
        throw  new UnsupportedOperationException();
    }

    @Override
    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        throw  new UnsupportedOperationException();
    }

    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        throw  new UnsupportedOperationException();
    }

    @Override
    public String getClientInfo(String name) throws SQLException {
        throw  new UnsupportedOperationException();
    }

    @Override
    public Properties getClientInfo() throws SQLException {
        throw  new UnsupportedOperationException();
    }

    @Override
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        throw  new UnsupportedOperationException();
    }

    @Override
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        throw  new UnsupportedOperationException();
    }

    @Override
    public void setSchema(String schema) throws SQLException {
        throw  new UnsupportedOperationException();
    }

    @Override
    public String getSchema() throws SQLException {
        throw  new UnsupportedOperationException();
    }

    @Override
    public void abort(Executor executor) throws SQLException {
        throw  new UnsupportedOperationException();
    }

    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
        throw  new UnsupportedOperationException();
    }

    @Override
    public int getNetworkTimeout() throws SQLException {
        throw  new UnsupportedOperationException();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw  new UnsupportedOperationException();
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        throw  new UnsupportedOperationException();
    }
}
