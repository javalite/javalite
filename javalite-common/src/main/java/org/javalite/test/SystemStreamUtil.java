package org.javalite.test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * This class can be used to catch standard and error outputs in tests so as not to
 * pollute test printout with "good" exceptions stack traces.
 *
 * @author Igor Polevoy: 3/5/12 12:25 PM
 */
public class SystemStreamUtil {
    private static PrintStream out;
    private static PrintStream err;
    private static ByteArrayOutputStream outStream;
    private static ByteArrayOutputStream errorStream;

    private SystemStreamUtil() {
        
    }
    
    /**
     * Replaces <code>System.out</code> with internal buffer. All calls such as <code>System.out.print...</code>
     * will go to this buffer and not to STDIO
     */
    public static void replaceOut() {
        out = System.out;
        outStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outStream));
    }

    /**
     * Returns buffer accumulated with data as string.
     *
     * @return buffer accumulated with data as string.
     */
    public static String getSystemOut() {
        return new String(outStream.toByteArray());
    }

    /**
     * Restores <code>System.out</code> to former glory.
     */
    public static void restoreSystemOut() {
        if (out == null) throw new NullPointerException("out cannot be null");
        System.setOut(out);
    }


    /**
     * Replaces <code>System.err</code> with internal buffer. All calls such as <code>System.err.print...</code>
     * will go to this buffer and not to STDERR
     */
    public static void replaceError() {
        err = System.err;
        errorStream = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errorStream));
    }

    /**
     * Returns buffer accumulated with data as string.
     *
     * @return buffer accumulated with data as string.
     */
    public static String getSystemErr() {
        return new String(errorStream.toByteArray());
    }

    /**
     * Restores <code>System.err</code> to former glory.
     */
    public static void restoreSystemErr() {
        if (err == null) throw new NullPointerException("err cannot be null");
        System.setErr(err);
    }
}
