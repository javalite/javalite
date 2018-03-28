package org.javalite.common;

import java.io.IOException;

import static org.javalite.common.Util.read;

/**
 * Utility class to shell out system commands
 *
 * @author igor on 1/20/17.
 */
public class RuntimeUtil {
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

            return new Response(out, err, process.waitFor());
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw  new RuntimeException("Interrupted");
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    public static class Response{
        public final String out, err;
        public final int exitValue;
        Response(String out, String err, int exitValue) {
            this.out = out;
            this.err = err;
            this.exitValue = exitValue;
        }
    }

    /**
     * Convenience method, does the same as {@link #execute(String...)}, but  will
     * automatically convert a full command string to tokens for convenience.
     * Here is how to call:
     *
     * <pre>
     * System.out.println(execute("ls -ls").out);
     * </pre>
     *
     * @param command - a single string representing a command and its arguments.
     * @return instance of {@link Response} with result of execution.
     */
    public static Response execute(String command) {
        return execute(Util.split(command, " "));
    }

    public static void main(String[] args) {
        System.out.println(execute("ls -ls").out);
    }
}
