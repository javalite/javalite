package org.javalite.async;

import org.javalite.activejdbc.Base;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Standard command listener to process commands that require a database access.
 * ActiveJDBC class {@link Base} will be used to open a connection.
 * <p>
 * This class will open a new connection, start a new transaction and will execute the command.
 * After that, the transaction will be committed. In case execution of a command fails,
 * the transaction will be rolled back and command and exception wil be passed to {@link #onException(Command, Exception)}
 * method, where a subclass can process them further. The connection will be closed regardless of outcome.
 * </p>
 *
 *
 * @author Igor Polevoy on 2/14/16.
 */
public class DBCommandListener extends CommandListener {

    private Logger logger = LoggerFactory.getLogger(DBCommandListener.class);

    private String jndiConnection;

    /**
     * JNDI string to open a connection from a pool. Example: "java:comp/env/jdbc/yourdb".
     *
     * @param jndiConnection JDBC connection string.
     */
    public DBCommandListener(String jndiConnection) {
        this.jndiConnection = jndiConnection;
    }

    /**
     * Use this constructor to open a connection using a set of properties from "database.properties"
     * file according to current ACTIVE_ENV environment (development, production, etc.).
     */
    public DBCommandListener() {}

    /**
     * @param command command to execute.
     */
    @Override
    public <T extends Command> void onCommand(T command) {
        try {

            if(jndiConnection != null){
                Base.open(jndiConnection);
            }else{
                Base.open();
            }
            Base.openTransaction();
            command.execute();
            Base.commitTransaction();
        } catch (Exception e) {
            try {
                if (Base.hasConnection()) {
                    Base.rollbackTransaction();
                }
            } catch (Exception ignore) {}
            onException(command, e);
        } finally {
            try {
                Base.close();
            } catch (Exception ignore) {}
        }
    }

    /**
     * Override in subclasses to handle exceptions. This implementation does nothing.
     *
     * @param command   command that was unsuccessfully executed
     * @param exception exception caught during command execution
     */
    protected void onException(Command command, Exception exception) {
        logger.error(command.dehydrate() , exception);
    }
}
