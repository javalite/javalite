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

/**
 * @author Igor Polevoy: 12/31/13 1:02 PM
 */

package org.javalite.db_migrator.mock;

import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

public class MockConnection implements Connection {
    MockStatement statement = new MockStatement();

    public Statement createStatement() throws SQLException {
        return statement;
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
        throw new UnsupportedOperationException("unsupported");  
    }

    public CallableStatement prepareCall(String sql) throws SQLException {
        throw new UnsupportedOperationException("unsupported");  
    }

    public String nativeSQL(String sql) throws SQLException {
        throw new UnsupportedOperationException("unsupported");  
    }

    public void setAutoCommit(boolean autoCommit) throws SQLException {
        throw new UnsupportedOperationException("unsupported");
    }

    public boolean getAutoCommit() throws SQLException {
        throw new UnsupportedOperationException("unsupported");
    }

    public void commit() throws SQLException {
        throw new UnsupportedOperationException("unsupported");
    }

    public void rollback() throws SQLException {
        throw new UnsupportedOperationException("unsupported");
    }

    public void close() throws SQLException {

    }

    public boolean isClosed() throws SQLException {
        throw new UnsupportedOperationException("unsupported");
    }

    public DatabaseMetaData getMetaData() throws SQLException {
        throw new UnsupportedOperationException("unsupported");  
    }

    public void setReadOnly(boolean readOnly) throws SQLException {
        throw new UnsupportedOperationException("unsupported");
    }

    public boolean isReadOnly() throws SQLException {
        throw new UnsupportedOperationException("unsupported");
    }

    public void setCatalog(String catalog) throws SQLException {
        throw new UnsupportedOperationException("unsupported");
    }

    public String getCatalog() throws SQLException {
        throw new UnsupportedOperationException("unsupported");  
    }

    public void setTransactionIsolation(int level) throws SQLException {
        throw new UnsupportedOperationException("unsupported");
    }

    public int getTransactionIsolation() throws SQLException {
        throw new UnsupportedOperationException("unsupported");
    }

    public SQLWarning getWarnings() throws SQLException {
        throw new UnsupportedOperationException("unsupported");  
    }

    public void clearWarnings() throws SQLException {
        throw new UnsupportedOperationException("unsupported");
    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        throw new UnsupportedOperationException("unsupported");  
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        throw new UnsupportedOperationException("unsupported");  
    }

    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        throw new UnsupportedOperationException("unsupported");  
    }

    public Map<String, Class<?>> getTypeMap() throws SQLException {
        throw new UnsupportedOperationException("unsupported");  
    }

    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        throw new UnsupportedOperationException("unsupported");
    }

    public void setHoldability(int holdability) throws SQLException {
        throw new UnsupportedOperationException("unsupported");
    }

    public int getHoldability() throws SQLException {
        throw new UnsupportedOperationException("unsupported");
    }

    public Savepoint setSavepoint() throws SQLException {
        throw new UnsupportedOperationException("unsupported");  
    }

    public Savepoint setSavepoint(String name) throws SQLException {
        throw new UnsupportedOperationException("unsupported");  
    }

    public void rollback(Savepoint savepoint) throws SQLException {
        throw new UnsupportedOperationException("unsupported");
    }

    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        throw new UnsupportedOperationException("unsupported");
    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        throw new UnsupportedOperationException("unsupported");  
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        throw new UnsupportedOperationException("unsupported");  
    }

    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        throw new UnsupportedOperationException("unsupported");  
    }

    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        throw new UnsupportedOperationException("unsupported");  
    }

    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        throw new UnsupportedOperationException("unsupported");  
    }

    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        throw new UnsupportedOperationException("unsupported");  
    }

    public Clob createClob() throws SQLException {
        throw new UnsupportedOperationException("unsupported");  
    }

    public Blob createBlob() throws SQLException {
        throw new UnsupportedOperationException("unsupported");  
    }

    public NClob createNClob() throws SQLException {
        throw new UnsupportedOperationException("unsupported");  
    }

    public SQLXML createSQLXML() throws SQLException {
        throw new UnsupportedOperationException("unsupported");  
    }

    public boolean isValid(int timeout) throws SQLException {
        throw new UnsupportedOperationException("unsupported");
    }

    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        throw new UnsupportedOperationException("unsupported");
    }

    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        throw new UnsupportedOperationException("unsupported");
    }

    public String getClientInfo(String name) throws SQLException {
        throw new UnsupportedOperationException("unsupported");  
    }

    public Properties getClientInfo() throws SQLException {
        throw new UnsupportedOperationException("unsupported");  
    }

    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        throw new UnsupportedOperationException("unsupported");  
    }

    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        throw new UnsupportedOperationException("unsupported");  
    }

    public void setSchema(String schema) throws SQLException {
        throw new UnsupportedOperationException("unsupported");
    }

    public String getSchema() throws SQLException {
        throw new UnsupportedOperationException("unsupported");  
    }

    public void abort(Executor executor) throws SQLException {
        throw new UnsupportedOperationException("unsupported");
    }

    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
        throw new UnsupportedOperationException("unsupported");
    }

    public int getNetworkTimeout() throws SQLException {
        throw new UnsupportedOperationException("unsupported");
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new UnsupportedOperationException("unsupported");  
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        throw new UnsupportedOperationException("unsupported");
    }

    public List<String> getExecutedStatements() {
        return statement.getExecutedStatements();
    }
}
