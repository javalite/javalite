package org.javalite.activejdbc.test;

import org.javalite.activejdbc.DB;
import org.javalite.activejdbc.InitException;
import org.javalite.activejdbc.connection_config.ConnectionSpecWrapper;
import org.javalite.activejdbc.connection_config.DbConfiguration;
import org.javalite.test.jspec.JSpecSupport;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * @author igor on 12/2/16.
 */
public class DBSpec extends DbConfiguration implements JSpecSupport {

    private static Logger LOGGER = LoggerFactory.getLogger(DBSpec.class.getSimpleName());
    private boolean rollback = true;


    /**
     * Current state of 'rollback' flag.
     *
     * @return Current state of 'rollback' flag.
     */
    public boolean rollback() {
        return rollback;
    }

    /**
     * Set to true in order  to rollback a transaction at the end of the test.
     * <p>
     *     <em>
     *     WARNING: if you set this value to false inside your test, the framework will not
     *     clean any remaining data you insert into your test database. Basically, this is a
     *     "manual mode" where you are responsible for cleaning after yourself.
     *     </em>
     * </p>
     *
     * @param rollback true to rollback transactions at the end of the test, false to not rollback.
     */
    public void setRollback(boolean rollback) {
        this.rollback = rollback;

        Map<String, Connection> connections = DB.connections();

        for(String name: connections.keySet()){
            try {
                boolean autocommit = !rollback;
                connections.get(name).setAutoCommit(autocommit);
            } catch (SQLException e) {
                throw new InitException(e);
            }
        }
    }

    @Before
    public final void openTestConnections() {

        loadConfiguration("/database.properties");

        List<ConnectionSpecWrapper> connectionWrappers = getTestConnectionWrappers();
        if (connectionWrappers.isEmpty()) {
            LOGGER.warn("no DB connections are configured, none opened");
            return;
        }

        for (ConnectionSpecWrapper connectionWrapper : connectionWrappers) {
            DB db = new DB(connectionWrapper.getDbName());
            db.open(connectionWrapper.getConnectionSpec());
            if (rollback()){
                db.openTransaction();
            }
        }
    }

    @After
    public final void closeTestConnections() {
        List<ConnectionSpecWrapper> connectionWrappers = getTestConnectionWrappers();
        for (ConnectionSpecWrapper connectionWrapper : connectionWrappers) {
            String dbName = connectionWrapper.getDbName();
            DB db = new DB(dbName);
            if (rollback()) {
                db.rollbackTransaction();
            }
            db.close();
        }
        clearConnectionWrappers();
    }
}
