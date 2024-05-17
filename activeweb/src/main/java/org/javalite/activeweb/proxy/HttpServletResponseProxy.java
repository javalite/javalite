package org.javalite.activeweb.proxy;


import org.javalite.activeweb.WebException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;

public class HttpServletResponseProxy implements HttpServletResponse {

    private HttpServletResponse servletResponse;
    private OutputType outputType;

    public enum OutputType{
        WRITER, OUTPUT_STREAM
    }

    public HttpServletResponseProxy(HttpServletResponse target) {
        this.servletResponse = target;
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
    public String encodeURL(String url) {
        return encodeURL(url);
    }

    @Override
    public String encodeRedirectURL(String url) {
        return servletResponse.encodeRedirectURL(url);
    }

    @Override
    public String encodeUrl(String url) {
        return servletResponse.encodeUrl(url);
    }

    @Override
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
    public void setStatus(int sc, String sm) {
        servletResponse.setStatus(sc, sm);
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
        if(OutputType.WRITER == outputType){
            throw new WebException("Cannot return OutputStream because Writer was already used.");
        }else if(outputType == null) {
            outputType = OutputType.OUTPUT_STREAM;
        }
        return new ServletOutputStreamProxy(this.servletResponse.getOutputStream());
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if(OutputType.OUTPUT_STREAM == outputType){
            throw new WebException("Cannot return Writer because OutputStream was already used.");
        }else if(outputType == null){
            outputType = OutputType.WRITER;
        }
        return  new PrintWriterProxy(this.servletResponse.getWriter());
    }

    public OutputType getOutputType(){
        return outputType;
    }


    @Override
    public void setCharacterEncoding(String charset) {
        servletResponse.setCharacterEncoding(charset);
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
