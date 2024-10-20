package org.javalite.activeweb.proxy;


import org.javalite.activeweb.WebException;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Locale;

public class HttpServletResponseProxy implements HttpServletResponse {

    private HttpServletResponse servletResponse;
    private PrintWriterProxy printWriterProxy;
    private ServletOutputStreamProxy outputStreamProxy;
    public enum OutputType{
        WRITER, OUTPUT_STREAM, NONE
    }

    public HttpServletResponseProxy(HttpServletResponse target) {
        this.servletResponse = target;
    }

    public OutputType getOutputType(){
        if(outputStreamProxy != null){
            return OutputType.OUTPUT_STREAM;
        } else if (printWriterProxy != null) {
            return OutputType.WRITER;
        }else {
            return OutputType.NONE;
        }
    }
    public HttpServletResponse getTarget() {
        return servletResponse;
    }

    @Override
    public void addCookie(Cookie cookie) {
        servletResponse.addCookie(cookie);
    }

    @Override
    public boolean containsHeader(String name) {
        return servletResponse.containsHeader(name);
    }

    @Override
    public String encodeRedirectURL(String url) {
        return servletResponse.encodeRedirectURL(url);
    }

    @Override
    public String encodeURL(String url) {
        return servletResponse.encodeURL(url);
    }


    public String encodeRedirectUrl(String url) {
        return servletResponse.encodeRedirectURL(url);
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        servletResponse.sendError(sc, msg);
    }

    @Override
    public void sendError(int sc) throws IOException {
        servletResponse.sendError(sc);
    }

    @Override
    public void sendRedirect(String location) throws IOException {
        servletResponse.sendRedirect(location);
    }

    @Override
    public void sendRedirect(String s, int i, boolean b) throws IOException {

    }

    @Override
    public void setDateHeader(String name, long date) {
        servletResponse.setDateHeader(name, date);
    }

    @Override
    public void addDateHeader(String name, long date) {
        servletResponse.addDateHeader(name, date);
    }

    @Override
    public void setHeader(String name, String value) {
        servletResponse.setHeader(name, value);
    }

    @Override
    public void addHeader(String name, String value) {
        servletResponse.addHeader(name, value);
    }

    @Override
    public void setIntHeader(String name, int value) {
        servletResponse.setIntHeader(name, value);
    }

    @Override
    public void addIntHeader(String name, int value) {
        servletResponse.addIntHeader(name, value);
    }

    @Override
    public void setStatus(int sc) {
        servletResponse.setStatus(sc);
    }

    @Override
    public int getStatus() {
        return servletResponse.getStatus();
    }

    @Override
    public String getHeader(String name) {
        return servletResponse.getHeader(name);
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return servletResponse.getHeaders(name);
    }

    @Override
    public Collection<String> getHeaderNames() {
        return servletResponse.getHeaderNames();
    }

    @Override
    public String getCharacterEncoding() {
        return servletResponse.getCharacterEncoding();
    }

    @Override
    public String getContentType() {
        return servletResponse.getContentType();
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if(printWriterProxy != null){
            throw new WebException("Cannot return OutputStream because Writer was already used.");
        }
        if(outputStreamProxy == null){
            outputStreamProxy= new ServletOutputStreamProxy(this.servletResponse.getOutputStream());
        }
        return outputStreamProxy;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if(outputStreamProxy != null){
            throw new WebException("Cannot return Writer because OutputStream was already used.");
        }
        if(printWriterProxy == null){
            printWriterProxy = new PrintWriterProxy(this.servletResponse.getWriter());
        }
        return  printWriterProxy;
    }

    @Override
    public void setCharacterEncoding(String charset) {
        servletResponse.setCharacterEncoding(charset);
    }

    @Override
    public void setCharacterEncoding(Charset encoding) {
        HttpServletResponse.super.setCharacterEncoding(encoding);
    }

    @Override
    public void setContentLength(int len) {
        servletResponse.setContentLength(len);
    }

    @Override
    public void setContentLengthLong(long len) {
        servletResponse.setContentLengthLong(len);
    }

    @Override
    public void setContentType(String type) {
        servletResponse.setContentType(type);
    }

    @Override
    public void setBufferSize(int size) {
        servletResponse.setBufferSize(size);
    }

    @Override
    public int getBufferSize() {
        return servletResponse.getBufferSize();
    }

    @Override
    public void flushBuffer() throws IOException {
        servletResponse.flushBuffer();
    }

    @Override
    public void resetBuffer() {
        servletResponse.resetBuffer();
    }

    @Override
    public boolean isCommitted() {
        return servletResponse.isCommitted();
    }

    @Override
    public void reset() {
        servletResponse.reset();
    }

    @Override
    public void setLocale(Locale loc) {
        servletResponse.setLocale(loc);
    }

    @Override
    public Locale getLocale() {
        return servletResponse.getLocale();
    }
}
