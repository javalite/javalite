package org.javalite.common;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.javalite.common.Util.read;

/**
 * Utility class to shell out system commands. Use for quick execution of  external processes that will not generate a lot of output.
 *
 * @author igor on 1/20/17.
 */
public class RuntimeUtil {

    private static int MAX_BUFFER_SIZE = 2048;


    /**
     * Executes an external command and provides results of execution at the current location.
     * Will accumulate limited output from the external process.
     *
     * @param command array containing the command to call and its arguments.
     * @param maxBuffer max size of buffers <code>out, err</code>. An external process may produce a
     *                  lot of output, be careful setting to a large value. The buffer will not be allocated to this
     *                  size at the start, but will grow until it reaches it. The program will continue toi execute, and the buffer
     *                  will be 'tailing' the output of the external process.
     *
     *
     * @return instance of {@link Response} with result of execution.
     */
    public static Response execute(int maxBuffer, String ... command) {
        return execute(maxBuffer, null, command);
    }

    /**
     * Executes an external command and provides results of execution.
     * Will accumulate limited output from the external process.
     *
     * @param command array containing the command to call and its arguments.
     * @param maxBuffer max size of buffers <code>out, err</code>. An external process may produce a
     *                  lot of output, be careful setting to a large value. The buffer will not be allocated to this
     *                  this size at the start, but will grow until it reaches it. The program will continue toi execute, and the buffer
     *                  will be 'tailing' the output of the external process.
     * @param dir - location of process execution. Pass <code>null</code> to execute at current location of the calling process.
     * @param envVars a list  of environment variables  to pass to the process. The format for each string in a list: "name=value".
     *
     *
     *
     * @return instance of {@link Response} with result of execution.
     */
    public static Response execute(int maxBuffer, File dir, List<String> envVars, String ... command) {
        if(dir != null && dir.isFile()){
            throw new IllegalArgumentException("Location must be a directory, not a file ");
        }

        if(command.length == 0){
            throw new IllegalArgumentException("Command must be provided.");
        }

        String[]  commandAndArgs = command.length == 1 && command[0].contains(" ") ? Util.split(command[0], " ") : command;

        try {


            if (envVars == null) {
                Map<String, String> systemEnvMap = System.getenv();
                envVars = new ArrayList<>();
                for(Map.Entry e : systemEnvMap.entrySet()) {
                    envVars.add(e.getKey() + "=" + e.getValue());
                }
            }

            String[] localEnv = envVars.toArray(new String[0]);

            Process process = Runtime.getRuntime().exec(commandAndArgs, localEnv, dir);

            OutputReader stdOutReader = new OutputReader(process.getInputStream(), maxBuffer);
            OutputReader stdErrReader = new OutputReader(process.getErrorStream(), maxBuffer);

            Thread t1 = new Thread(stdOutReader);
            t1.start();
            Thread t2 = new Thread(stdErrReader);
            t2.start();
            int code = process.waitFor();
            t1.join();
            t2.join();

            String out = stdOutReader.getOutput();
            String err = stdErrReader.getOutput();

            return new Response(out, err, code);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw  new RuntimeException("Interrupted");
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    /**
     * Executes an external command and provides results of execution.
     * Will accumulate limited output from the external process.
     *
     * @param command array containing the command to call and its arguments.
     * @param maxBuffer max size of buffers <code>out, err</code>. An external process may produce a
     *                  lot of output, be careful setting to a large value. The buffer will not be allocated to this
     *                  this size at the start, but will grow until it reaches it. The program will continue to execute, and the buffer
     *                  will be 'tailing' the output of the external process.
     * @param dir - location of process execution. Pass <code>null</code> to execute at current location of the calling process.
     *
     * @return instance of {@link Response} with result of execution.
     */
    public static Response execute(int maxBuffer, File dir, String ... command) {
        return execute(maxBuffer, dir, null, command);
    }


    /**
     * Executes an external command and provides results of execution.
     * Will accumulate limited output from the external process.
     *
     * Defaults to max 2048 characters in each buffer: out, err and only shows the tail of each buffer.
     *
     * @param command array containing the command to call and its arguments.
     *
     * @return instance of {@link Response} with result of execution.
     */
    public static Response execute(String ... command) {
        return execute(MAX_BUFFER_SIZE, command);
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

    /**
     * Will accumulate tail of STDIO or STDERR from the process in a separate thread
     */
    static class OutputReader implements Runnable {

        private int maxBuffer;
        private InputStream is;
        private  LinkedList<Character> buffer = new LinkedList<>();

        OutputReader(InputStream is, int maxBuffer) {
            this.is = is;
            this.maxBuffer = maxBuffer;
        }
        public void run() {
            try {
                InputStreamReader reader = new InputStreamReader(is);
                int s;
                while ((s = reader.read()) != -1) {
                    if(buffer.size() == maxBuffer){
                        buffer.remove(0);
                    }
                    buffer.add((char)s); // boxing
                }
                is.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        public String getOutput() {

            StringBuilder stringBuilder = new StringBuilder(buffer.size());
            for (Character character : buffer) {
                stringBuilder.append(character);
            }
            return stringBuilder.toString();
        }
    }
}
