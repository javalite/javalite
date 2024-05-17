package org.javalite.activeweb.proxy;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import java.io.IOException;

/**
 * This class exists to catch a case when a client disconnected from the server
 * during a long download or streaming.
 */
public class ServletOutputStreamProxy extends ServletOutputStream {

    private ServletOutputStream target;

    public ServletOutputStreamProxy(ServletOutputStream target) {
        this.target = target;
    }

    @Override
    public boolean isReady() {
        return target.isReady();
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {
        target.setWriteListener(writeListener);
    }

    @Override
    public void write(int b) throws IOException {

        try{
            target.write(b);
        }catch(Exception e){
            throw new HttpProxyException(e);
        }


    }

    @Override
    public void print(String s) throws IOException {
        try{
            target.print(s);
        }catch(Exception e){
            throw new HttpProxyException(e);
        }
    }

    @Override
    public void print(boolean b) throws IOException {
        try{
            target.print(b);
        }catch(Exception e){
            throw new HttpProxyException(e);
        }
    }

    @Override
    public void print(char c) throws IOException {
        try{
            target.print(c);
        }catch(Exception e){
            throw new HttpProxyException(e);
        }


    }

    @Override
    public void print(int i) throws IOException {
        try{
            target.print(i);
        }catch(Exception e){
            throw new HttpProxyException(e);
        }
    }

    @Override
    public void print(long l) throws IOException {
        try{
            target.print(l);
        }catch(Exception e){
            throw new HttpProxyException(e);
        }
    }

    @Override
    public void print(float f) throws IOException {
        try{
            target.print(f);
        }catch(Exception e){
            throw new HttpProxyException(e);
        }
    }

    @Override
    public void print(double d) throws IOException {
        try{
            target.print(d);
        }catch(Exception e){
            throw new HttpProxyException(e);
        }
    }

    @Override
    public void println() throws IOException {
        target.println();
    }

    @Override
    public void println(String s) throws IOException {
        try{
            target.print(s);
        }catch(Exception e){
            throw new HttpProxyException(e);
        }
    }

    @Override
    public void println(boolean b) throws IOException {
        try{
            target.print(b);
        }catch(Exception e){
            throw new HttpProxyException(e);
        }
    }

    @Override
    public void println(char c) throws IOException {
        try{
            target.print(c);
        }catch(Exception e){
            throw new HttpProxyException(e);
        }
    }

    @Override
    public void println(int i) throws IOException {
        try{
            target.print(i);
        }catch(Exception e){
            throw new HttpProxyException(e);
        }
    }

    @Override
    public void println(long l) throws IOException {
        try{
            target.print(l);
        }catch(Exception e){
            throw new HttpProxyException(e);
        }
    }

    @Override
    public void println(float f) throws IOException {

        try{
            target.print(f);
        }catch(Exception e){
            throw new HttpProxyException(e);
        }
    }

    @Override
    public void println(double d) throws IOException {
        try{
            target.print(d);
        }catch(Exception e){
            throw new HttpProxyException(e);
        }
    }

    @Override
    public void write(byte[] b) throws IOException {

        try{
            target.write(b);
        }catch(Exception e){
            throw new HttpProxyException(e);
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {

        try{
            target.write(b, off, len);
        }catch(Exception e){
            throw new HttpProxyException(e);
        }
    }

    @Override
    public void flush(){

        try{
            target.flush();
        }catch(Exception e){
            throw new HttpProxyException(e);
        }

    }

    @Override
    public void close() throws IOException {
        target.close();
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
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return target.toString();
    }
}
