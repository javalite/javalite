package org.javalite.activeweb.proxy;

import java.io.PrintWriter;
import java.util.Locale;

/**
 * This class exists to catch a case when a client disconnected from the server
 * during a long download or streaming.
 */
public class PrintWriterProxy extends PrintWriter {

    private PrintWriter target;

    public PrintWriterProxy(PrintWriter target) {
        super(target);
        this.target = target;
    }

    @Override
    public void flush() {
        target.flush();
        if(target.checkError()){
            throw new ProxyWriterException();
        }
    }

    @Override
    public void close() {
        target.close();
    }

    @Override
    public boolean checkError() {
        return target.checkError();
    }


    @Override
    public void write(int c) {
        target.write(c);
    }

    @Override
    public void write(char[] buf, int off, int len) {
        target.write(buf, off, len);
    }

    @Override
    public void write(char[] buf) {
        target.write(buf);
    }

    @Override
    public void write(String s, int off, int len) {
        target.write(s, off, len);
    }

    @Override
    public void write(String s) {
        target.write(s);
    }

    @Override
    public void print(boolean b) {
        target.print(b);
    }

    @Override
    public void print(char c) {
        target.print(c);
    }

    @Override
    public void print(int i) {
        target.print(i);
    }

    @Override
    public void print(long l) {
        target.print(l);
    }

    @Override
    public void print(float f) {
        target.print(f);
    }

    @Override
    public void print(double d) {
        target.print(d);
    }

    @Override
    public void print(char[] s) {
        target.print(s);
    }

    @Override
    public void print(String s) {
        target.print(s);
    }

    @Override
    public void print(Object obj) {
        target.print(obj);
    }

    @Override
    public void println() {
        target.println();
    }

    @Override
    public void println(boolean x) {
        target.println(x);
    }

    @Override
    public void println(char x) {
        target.println(x);
    }

    @Override
    public void println(int x) {
        
            target.println(x);
        
    }

    @Override
    public void println(long x) {
            target.println(x);
    }

    @Override
    public void println(float x) {
            target.println(x);
    }

    @Override
    public void println(double x) {
            target.println(x);
    }

    @Override
    public void println(char[] x) {
            target.println(x);
    }

    @Override
    public void println(String x) {
            target.println(x);
    }

    @Override
    public void println(Object x) {
            target.println(x);
    }

    @Override
    public PrintWriter printf(String format, Object... args) {
            return target.printf(format, args);
    }

    @Override
    public PrintWriter printf(Locale l, String format, Object... args) {
        return target.printf(l, format, args);
    }

    @Override
    public PrintWriter format(String format, Object... args) {
            return target.format(format, args);
    }

    @Override
    public PrintWriter format(Locale l, String format, Object... args) {
            return target.format(l, format, args);
    }

    @Override
    public PrintWriter append(CharSequence csq) {
            return target.append(csq);
    }

    @Override
    public PrintWriter append(CharSequence csq, int start, int end) {
            return target.append(csq, start, end);
    }

    @Override
    public PrintWriter append(char c) {
            return target.append(c);
    }

    @Override
    public int hashCode() {
        return target.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return target.equals(obj);
    }

    @Override
    public String toString() {
        return target.toString();
    }
}