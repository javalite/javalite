package org.javalite.activejdbc;

import org.slf4j.Logger;

/**
 * Implement this interface if you want to completely replace logging behavior with your own.
 *
 * For more information, refer: <a hreh="http://javalite.io/logging">Logging</a>.
 *
 * @author igor on 5/20/17.
 */
public interface ActiveJDBCLogger {
    void log(Logger logger, String log);
    void log(Logger logger, String log, Object param);
    void log(Logger logger, String log, Object ... param);
    void log(Logger logger, String log, Object param1, Object param2);
}
