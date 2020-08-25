package org.javalite.cassandra.jdbc;

import com.datastax.oss.driver.api.core.cql.Row;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.ResultSet;
import java.util.Iterator;
import java.util.NoSuchElementException;


public class CassandraJDBCResultSet implements InvocationHandler {

    private Iterator<Row> rows;


    static ResultSet createProxy(com.datastax.oss.driver.api.core.cql.ResultSet cqlResultSet){
        return (ResultSet) Proxy.newProxyInstance(
                CassandraJDBCResultSet.class.getClassLoader(),
                new Class[] { ResultSet.class },
                new CassandraJDBCResultSet(cqlResultSet));
    }

    private CassandraJDBCResultSet(com.datastax.oss.driver.api.core.cql.ResultSet cqlResultSet) {
        rows = cqlResultSet.iterator();
    }

    private Row row;
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        switch (method.getName()) {
            case "next":
                try {
                    row = rows.next();
                    return true;
                } catch (NoSuchElementException e) {
                    return false;
                }
            case "getObject":
                return row.getObject((Integer) args[0] - 1);
            case "close":
                return null;
            default:
                throw new UnsupportedOperationException("Unsupported method: " + method + " with count of arguments: " + method.getParameterCount());
        }
    }
}
