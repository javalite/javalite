package org.javalite.activeweb;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * @author Igor Polevoy: 1/4/12 2:39 PM
 */
public class SystemStreamUtil {
    private static PrintStream out;
    private static PrintStream err;
    private static ByteArrayOutputStream outStream;
    private static ByteArrayOutputStream errorStream;


    public static void replaceOut() {
        out = System.out;
        outStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outStream));
    }

    public static String getSystemOut() {
        return new String(outStream.toByteArray());
    }

    public static void restoreSystemOut() {
        if (out == null) throw new NullPointerException("err cannot be null");
        System.setOut(out);
    }

    public static void replaceError() {
        err = System.err;
        errorStream = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errorStream));
    }

    public static String getSystemErr() {
        return new String(errorStream.toByteArray());
    }

    public static void restoreSystemErr() {
        if (err == null) throw new NullPointerException("err cannot be null");
        System.setErr(err);
    }
}
