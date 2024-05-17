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
        try{
            target.flush();
        }catch(Exception e){
            throw new HttpProxyException(e);
        }
    }

    @Override
    public void close() {
        try{
            target.close();
        }catch(Exception e){
            throw new HttpProxyException(e);
        }
    }

    @Override
    public boolean checkError() {
        try{
            return target.checkError();
        }catch(Exception e){
            throw new HttpProxyException(e);
        }
    }


    @Override
    public void write(int c) {
        try{
            target.write(c);
        }catch(Exception e){
            throw new HttpProxyException(e);
        }
    }

    @Override
    public void write(char[] buf, int off, int len) {
        try{
            target.write(buf, off, len);
        }catch(Exception e){
            throw new HttpProxyException(e);
        }
    }

    @Override
    public void write(char[] buf) {

        try{
            target.write(buf);
        }catch(Exception e){
            throw new HttpProxyException(e);
        }
    }

    @Override
    public void write(String s, int off, int len) {
        try{
            target.write(s, off, len);
        }catch(Exception e){
            throw new HttpProxyException(e);
        }
    }

    @Override
    public void write(String s) {
        try{
            target.write(s);
        }catch(Exception e){
            throw new HttpProxyException(e);
        }
    }

    @Override
    public void print(boolean b) {
        try{
            target.print(b);
        }catch(Exception e){
            throw new HttpProxyException(e);
        }
    }

    @Override
    public void print(char c) {
        try{
            target.print(c);
        }catch(Exception e){
            throw new HttpProxyException(e);
        }
    }

    @Override
    public void print(int i) {
        try{
            target.print(i);
        }catch(Exception e){
            throw new HttpProxyException(e);
        }
    }

    @Override
    public void print(long l) {
        try{
            target.print(l);
        }catch(Exception e){
            throw new HttpProxyException(e);
        }
    }

    @Override
    public void print(float f) {
        try{
            target.print(f);
        }catch(Exception e){
            throw new HttpProxyException(e);
        }
    }

    @Override
    public void print(double d) {
        try{
            target.print(d);
        }catch(Exception e){
            throw new HttpProxyException(e);
        }
    }

    @Override
    public void print(char[] s) {
        try{
            target.print(s);
        }catch(Exception e){
            throw new HttpProxyException(e);
        }
    }

    @Override
    public void print(String s) {
        try{
            target.print(s);
        }catch(Exception e){
            throw new HttpProxyException(e);
        }
    }

    @Override
    public void print(Object obj) {
        try{
            target.print(obj);
        }catch(Exception e){
            throw new HttpProxyException(e);
        }
    }

    @Override
    public void println() {
        try{
            target.println();
        }catch(Exception e){
            throw new HttpProxyException(e);
        }
    }

    @Override
    public void println(boolean x) {
        try{
            target.println(x);
        }catch(Exception e){
            throw new HttpProxyException(e);
        }
    }

    @Override
    public void println(char x) {
        try{
            target.println(x);
        }catch(Exception e){
            throw new HttpProxyException(e);
        }
    }

    @Override
    public void println(int x) {
        try{
            target.println(x);
        }catch(Exception e){
            throw new HttpProxyException(e);
        }
    }

    @Override
    public void println(long x) {
        try{
            target.println(x);
        }catch(Exception e){
            throw new HttpProxyException(e);
        }
    }

    @Override
    public void println(float x) {
        try{
            target.println(x);
        }catch(Exception e){
            throw new HttpProxyException(e);
        }
    }

    @Override
    public void println(double x) {
        try{
            target.println(x);
        }catch(Exception e){
            throw new HttpProxyException(e);
        }
    }

    @Override
    public void println(char[] x) {
        try{
            target.println(x);
        }catch(Exception e){
            throw new HttpProxyException(e);
        }
    }

    @Override
    public void println(String x) {
        try{
            target.println(x);
        }catch(Exception e){
            throw new HttpProxyException(e);
        }
    }

    @Override
    public void println(Object x) {
        try{
            target.println(x);
        }catch(Exception e){
            throw new HttpProxyException(e);
        }
    }

    @Override
    public PrintWriter printf(String format, Object... args) {
        try{
            return target.printf(format, args);
        }catch(Exception e){
            throw new HttpProxyException(e);
        }
    }

    @Override
    public PrintWriter printf(Locale l, String format, Object... args) {
        try{
            return target.printf(l, format, args);
        }catch(Exception e){
            throw new HttpProxyException(e);
        }
    }

    @Override
    public PrintWriter format(String format, Object... args) {
        try{
            return target.format(format, args);
        }catch(Exception e){
            throw new HttpProxyException(e);
        }
    }

    @Override
    public PrintWriter format(Locale l, String format, Object... args) {
        try{
            return target.format(l, format, args);
        }catch(Exception e){
            throw new HttpProxyException(e);
        }
    }

    @Override
    public PrintWriter append(CharSequence csq) {
        try{
            return target.append(csq);
        }catch(Exception e){
            throw new HttpProxyException(e);
        }
    }

    @Override
    public PrintWriter append(CharSequence csq, int start, int end) {
        try{
            return target.append(csq, start, end);
        }catch(Exception e){
            throw new HttpProxyException(e);
        }
    }

    @Override
    public PrintWriter append(char c) {
        try{
            return target.append(c);
        }catch(Exception e){
            throw new HttpProxyException(e);
        }


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