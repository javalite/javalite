package org.javalite.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.javalite.common.Util.read;

/**
 * Utility class to shell out system commands
 *
 * @author igor on 1/20/17.
 */
public class RuntimeUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(RuntimeUtil.class);

    /**
     * Executes an external command and provides results of execution.
     *
     * @param command array containing the command to call and its arguments.
     *
     * @return instance of {@link Response} with result of execution.
     */
    public static Response execute(String ... command) {
        try {
            Process process = Runtime.getRuntime().exec(command);
            String out = read(process.getInputStream());
            String err = read(process.getErrorStream());
            LOGGER.info("Command \"{}\" finished with the a response '{}' and error '{}'.", Util.join(command, " "), out, err);
            if (process.waitFor() != 0) {
                throw new RuntimeException("Failed to execute command: " + Util.join(command, " ") + "\n" + err);
            }
            return new Response(out, err);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw  new RuntimeException("Interrupted");
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    public static class Response{
        public final String out, err;
        public Response(String out, String err) {
            this.out = out;
            this.err = err;
        }
    }

}
