package org.javalite.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;

import static org.javalite.common.Util.read;

/**
 * Utility class to shell out system commands
 *
 * @author igor on 1/20/17.
 */
public class RuntimeUtil {
    /**
     * Executes an external command and provides results of execution. Will accummulate limited output from the external process.
     *
     * @param command array containing the command to call and its arguments.
     *
     * @return instance of {@link Response} with result of execution.
     */
    public static Response execute(String ... command) {
        try {
            Process process = Runtime.getRuntime().exec(command);

            OutputReader stdOutReader = new OutputReader(process.getInputStream());
            OutputReader stdErrReader = new OutputReader(process.getErrorStream());

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

        private static final int MAX_SIZE = 2048;
        private InputStream is;
        private  LinkedList<Character> buffer = new LinkedList<>();

        OutputReader(InputStream is) {
            this.is = is;
        }
        public void run() {
            try {
                InputStreamReader reader = new InputStreamReader(is);
                int s;
                while ((s = reader.read()) != -1) {
                    if(buffer.size() == MAX_SIZE){
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

    public static void main(String[] args) {
        System.out.println(execute("ls -ls").out);

        System.out.println(execute("ls", "-ls").out);
    }
}
