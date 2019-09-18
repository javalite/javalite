package org.javalite.activeweb;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;

public class HttpServletResponseProxy implements HttpServletResponse {

    private HttpServletResponse target;
    private OutputType outputType;

    enum OutputType{
        WRITER, OUTPUT_STREAM
    }


    HttpServletResponseProxy(HttpServletResponse target) {
        this.target = target;
    }

    HttpServletResponse getTarget() {
        return target;
    }

    @Override
    public void addCookie(Cookie cookie) {
        target.addCookie(cookie);
    }

    @Override
    public boolean containsHeader(String name) {
        return target.containsHeader(name);
    }

    @Override
    public String encodeURL(String url) {
        return encodeURL(url);
    }

    @Override
    public String encodeRedirectURL(String url) {
        return target.encodeRedirectURL(url);
    }

    @Override
    public String encodeUrl(String url) {
        return target.encodeUrl(url);
    }

    @Override
    public String encodeRedirectUrl(String url) {
        return target.encodeRedirectURL(url);
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        target.sendError(sc, msg);
    }

    @Override
    public void sendError(int sc) throws IOException {
        target.sendError(sc);
    }

    @Override
    public void sendRedirect(String location) throws IOException {
        target.sendRedirect(location);
    }

    @Override
    public void setDateHeader(String name, long date) {
        target.setDateHeader(name, date);
    }

    @Override
    public void addDateHeader(String name, long date) {
        target.addDateHeader(name, date);
    }

    @Override
    public void setHeader(String name, String value) {
        target.setHeader(name, value);
    }

    @Override
    public void addHeader(String name, String value) {
        target.addHeader(name, value);
    }

    @Override
    public void setIntHeader(String name, int value) {
        target.setIntHeader(name, value);
    }

    @Override
    public void addIntHeader(String name, int value) {
        target.addIntHeader(name, value);
    }

    @Override
    public void setStatus(int sc) {
        target.setStatus(sc);
    }

    @Override
    public void setStatus(int sc, String sm) {
        target.setStatus(sc, sm);
    }

    @Override
    public int getStatus() {
        return target.getStatus();
    }

    @Override
    public String getHeader(String name) {
        return target.getHeader(name);
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return target.getHeaders(name);
    }

    @Override
    public Collection<String> getHeaderNames() {
        return target.getHeaderNames();
    }

    @Override
    public String getCharacterEncoding() {
        return target.getCharacterEncoding();
    }

    @Override
    public String getContentType() {
        return target.getContentType();
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if(OutputType.WRITER == outputType){
            throw new WebException("Cannot return OutputStream because Writer was already served.");
        }else if(outputType == null) {
            outputType = OutputType.OUTPUT_STREAM;
        }
        return target.getOutputStream();
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if(OutputType.OUTPUT_STREAM == outputType){
            throw new WebException("Cannot return Writer because OutputStream was already served.");
        }else if(outputType == null){
            outputType = OutputType.WRITER;
        }
        return target.getWriter();
    }

    OutputType getOutputType(){
        return outputType;
    }


    @Override
    public void setCharacterEncoding(String charset) {
        target.setCharacterEncoding(charset);
    }

    @Override
    public void setContentLength(int len) {
        target.setContentLength(len);
    }

    @Override
    public void setContentLengthLong(long len) {
        target.setContentLengthLong(len);
    }

    @Override
    public void setContentType(String type) {
        target.setContentType(type);
    }

    @Override
    public void setBufferSize(int size) {
        target.setBufferSize(size);
    }

    @Override
    public int getBufferSize() {
        return target.getBufferSize();
    }

    @Override
    public void flushBuffer() throws IOException {
        target.flushBuffer();
    }

    @Override
    public void resetBuffer() {
        target.resetBuffer();
    }

    @Override
    public boolean isCommitted() {
        return target.isCommitted();
    }

    @Override
    public void reset() {
        target.reset();
    }

    @Override
    public void setLocale(Locale loc) {
        target.setLocale(loc);
    }

    @Override
    public Locale getLocale() {
        return target.getLocale();
    }
}
