package org.javalite.activeweb;

import org.javalite.common.Convert;
import org.javalite.common.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static java.util.Arrays.asList;
import static org.javalite.common.Collections.list;

/**
 * 
 * TODO: this needs to become a default interface, once we move the project to java 8
 * 
 * @author igor, on 6/16/14.
 */
public class RequestUtils {

    private static Logger logger = LoggerFactory.getLogger(RequestUtils.class);

    /**
     * Returns value of one named parameter from request. If this name represents multiple values, this
     * call will result in {@link IllegalArgumentException}.
     *
     * @param name name of parameter.
     * @return value of request parameter.
     */
    public static String param(String name){
        if(name.equals("id")){
            return getId();
        }else if(Context.getRequestContext().getUserSegments().get(name) != null){
            return Context.getRequestContext().getUserSegments().get(name);
        }else if(Context.getRequestContext().getWildCardName() != null
                && name.equals(Context.getRequestContext().getWildCardName())){
            return Context.getRequestContext().getWildCardValue();
        }else{
            return Context.getHttpRequest().getParameter(name);
        }
    }


    /**
     * Returns value of ID if one is present on a URL. Id is usually a part of a URI, such as: <code>/controller/action/id</code>.
     * This depends on a type of a URI, and whether controller is RESTful or not.
     *
     * @return ID value from URI is one exists, null if not.
     */
    public static String getId(){
        String paramId = Context.getHttpRequest().getParameter("id");
        if(paramId != null && Context.getHttpRequest().getAttribute("id") != null){
            logger.warn("WARNING: probably you have 'id' supplied both as a HTTP parameter, as well as in the URI. Choosing parameter over URI value.");
        }

        String theId;
        if(paramId != null){
            theId =  paramId;
        }else{
            Object id = Context.getHttpRequest().getAttribute("id");
            theId =  id != null ? id.toString() : null;
        }
        return Util.blank(theId) ? null : theId;
    }


    /**
     * Returns a format part of the URI, or null if URI does not have a format part.
     * A format part is defined as part of URI that is trailing after a last dot, as in:
     *
     * <code>/books.xml</code>, here "xml" is a format.
     *
     * @return format part of the URI, or nul if URI does not have it.
     */
    public static String format(){
        return Context.getFormat();
    }


    /**
     * Returns instance of {@link AppContext}.
     *
     * @return instance of {@link AppContext}.
     */
    public static AppContext appContext(){
        return Context.getAppContext();
    }


    /**
     * Returns true if this request is Ajax.
     *
     * @return true if this request is Ajax.
     */
    public static boolean isXhr(){
        return header("X-Requested-With") != null || header("x-requested-with") != null;
    }


    /**
     * Helper method, returns user-agent header of the request.
     *
     * @return user-agent header of the request.
     */
    public static String userAgent(){
        String camel = header("User-Agent");
        return camel != null ? camel : header("user-agent");
    }

    /**
     * Synonym for {@link #isXhr()}.
     */
    public static boolean xhr(){
        return isXhr();
    }


    /**
     * Returns instance of {@link Route} to be used for potential conditional logic inside controller filters.
     *
     * @return instance of {@link Route}
     */
    public static Route getRoute(){
        return Context.getRoute();
    }


    /**
     * Tests if a request parameter exists. Disregards the value completely - this
     * can be empty string, but as long as parameter does exist, this method returns true.
     *
     * @param name name of request parameter to test.
     * @return true if parameter exists, false if not.
     */
    public static boolean exists(String name){
        return param(name) != null;
    }

    /**
     * Synonym of {@link #exists(String)}.
     *
     * @param name name of request parameter to test.
     * @return true if parameter exists, false if not.
     */
    public static boolean requestHas(String name){
        return param(name) != null;
    }


    /**
     * Returns local host name on which request was received.
     *
     * @return local host name on which request was received.
     */
    public static String host() {
        return Context.getHttpRequest().getLocalName();
    }


    /**
     * Returns local IP address on which request was received.
     *
     * @return local IP address on which request was received.
     */
    public static  String ipAddress() {
        return Context.getHttpRequest().getLocalAddr();
    }





    /**
     * This method returns a protocol of a request to web server if this container is fronted by one, such that
     * it sets a header <code>X-Forwarded-Proto</code> on the request and forwards it to the Java container.
     * If such header is not present, than the {@link #protocol()} method is used.
     *
     * @return protocol of web server request if <code>X-Forwarded-Proto</code> header is found, otherwise current
     * protocol.
     */
    public static String getRequestProtocol(){
        String protocol = header("X-Forwarded-Proto");
        return Util.blank(protocol)? protocol(): protocol;
    }

    /**
     * This method returns a port of a web server if this Java container is fronted by one, such that
     * it sets a header <code>X-Forwarded-Port</code> on the request and forwards it to the Java container.
     * If such header is not present, than the {@link #port()} method is used.
     *
     * @return port of web server request if <code>X-Forwarded-Port</code> header is found, otherwise port of the Java container.
     */
    public static int getRequestPort(){
        String port = header("X-Forwarded-Port");
        return Util.blank(port)? port(): Integer.parseInt(port);
    }



    /**
     * Returns port on which the of the server received current request.
     *
     * @return port on which the of the server received current request.
     */
    public static int port(){
        return Context.getHttpRequest().getLocalPort();
    }


    /**
     * Returns protocol of request, for example: HTTP/1.1.
     *
     * @return protocol of request
     */
    public static String protocol(){
        return Context.getHttpRequest().getProtocol();
    }

    //TODO: provide methods for: X-Forwarded-Proto and X-Forwarded-Port
    /**
     * This method returns a host name of a web server if this container is fronted by one, such that
     * it sets a header <code>X-Forwarded-Host</code> on the request and forwards it to the Java container.
     * If such header is not present, than the {@link #host()} method is used.
     *
     * @return host name of web server if <code>X-Forwarded-Host</code> header is found, otherwise local host name.
     */
    public static String getRequestHost() {
        String forwarded = header("X-Forwarded-Host");
        if (Util.blank(forwarded)) {
            return host();
        }
        String[] forwards = forwarded.split(",");
        return forwards[0].trim();
    }

    /**
     * Returns IP address that the web server forwarded request for.
     *
     * @return IP address that the web server forwarded request for.
     */
    public static String ipForwardedFor() {
        String h = header("X-Forwarded-For");
        return !Util.blank(h) ? h : remoteAddress();
    }


    /**
     * Returns multiple request values for a name.
     *
     * @param name name of multiple values from request.
     * @return multiple request values for a name.
     */
    public static List<String> params(String name){
        if (name.equals("id")) {
            String id = getId();
            return id != null ? asList(id) : Collections.<String>emptyList();
        } else {
            String[] values = Context.getHttpRequest().getParameterValues(name);
            List<String>valuesList = values == null? new ArrayList<String>() : list(values);
            String userSegment = Context.getRequestContext().getUserSegments().get(name);
            if(userSegment != null){
                valuesList.add(userSegment);
            }
            return valuesList;
        }
    }


    /**
     * Returns a map where keys are names of all parameters, while values are first value for each parameter, even
     * if such parameter has more than one value submitted.
     *
     * @return a map where keys are names of all parameters, while values are first value for each parameter, even
     * if such parameter has more than one value submitted.
     */
    public static Map<String, String> params1st(){
        //TODO: candidate for performance optimization
        Map<String, String> params = new HashMap<String, String>();
        Enumeration names = Context.getHttpRequest().getParameterNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement().toString();
            params.put(name, Context.getHttpRequest().getParameter(name));
        }
        if(getId() != null)
            params.put("id", getId());

        Map<String, String> userSegments = Context.getRequestContext().getUserSegments();
        params.putAll(userSegments);
        return params;
    }


    /**
     * Returns an instance of <code>java.util.Map</code> containing parameter names as keys and parameter values as map values.
     * The keys in the parameter map are of type String. The values in the parameter map are of type String array.
     *
     * @return an instance <code>java.util.Map</code> containing parameter names as keys and parameter values as map values.
     * The keys in the parameter map are of type String. The values in the parameter map are of type String array.
     */
    public static Map<String, String[]> params(){
        SimpleHash params = new SimpleHash(Context.getHttpRequest().getParameterMap());
        if(getId() != null)
            params.put("id", new String[]{getId()});

        Map<String, String> userSegments = Context.getRequestContext().getUserSegments();

        for(String name:userSegments.keySet()){
            params.put(name, new String[]{userSegments.get(name)});
        }

        return params;
    }



    /**
     * Returns locale of request.
     *
     * @return locale of request.
     */
    public static Locale locale(){
        return Context.getHttpRequest().getLocale();
    }

    /**
     * Same as {@link #locale()}.
     *
     * @return locale of request.
     */
    public static Locale getLocale(){
        return Context.getHttpRequest().getLocale();
    }

    /**
     * Returns collection of all cookies browser sent.
     *
     * @return collection of all cookies browser sent.
     */
    public static List<Cookie> cookies(){
        javax.servlet.http.Cookie[] servletCookies = Context.getHttpRequest().getCookies();
        if(servletCookies == null)
            return new ArrayList<Cookie>();

        List<Cookie> cookies = new ArrayList<Cookie>();
        for (javax.servlet.http.Cookie servletCookie: servletCookies) {
            Cookie cookie = Cookie.fromServletCookie(servletCookie);
            cookies.add(cookie);
        }
        return cookies;
    }

    /**
     * Returns a cookie by name, null if not found.
     *
     * @param name name of a cookie.
     * @return a cookie by name, null if not found.
     */
    public static Cookie cookie(String name){
        javax.servlet.http.Cookie[] servletCookies = Context.getHttpRequest().getCookies();
        if (servletCookies != null) {
            for (javax.servlet.http.Cookie servletCookie : servletCookies) {
                if (servletCookie.getName().equals(name)) {
                    return Cookie.fromServletCookie(servletCookie);
                }
            }
        }
        return null;
    }


    /**
     * Convenience method, returns cookie value.
     *
     * @param name name of cookie.
     * @return cookie value.
     */
    public static String cookieValue(String name){
        return cookie(name).getValue();
    }


    /**
     * Returns a path of the request. It does not include protocol, host, port or context. Just a path.
     * Example: <code>/controller/action/id</code>
     *
     * @return a path of the request.
     */
    public static String path(){
        return Context.getHttpRequest().getServletPath();
    }

    /**
     * Returns a full URL of the request, all except a query string.
     *
     * @return a full URL of the request, all except a query string.
     */
    public  static String url(){
        return Context.getHttpRequest().getRequestURL().toString();
    }

    /**
     * Returns query string of the request.
     *
     * @return query string of the request.
     */
    public  static String queryString(){
        return Context.getHttpRequest().getQueryString();
    }

    /**
     * Returns an HTTP method from the request.
     *
     * @return an HTTP method from the request.
     */
    public static String method(){
        return Context.getHttpRequest().getMethod();
    }

    /**
     * True if this request uses HTTP GET method, false otherwise.
     *
     * @return True if this request uses HTTP GET method, false otherwise.
     */
    public static boolean isGet() {
        return isMethod("get");
    }


    /**
     * True if this request uses HTTP POST method, false otherwise.
     *
     * @return True if this request uses HTTP POST method, false otherwise.
     */
    public static boolean isPost() {
        return isMethod("post");
    }


    /**
     * True if this request uses HTTP PUT method, false otherwise.
     *
     * @return True if this request uses HTTP PUT method, false otherwise.
     */
    public static boolean isPut() {
        return isMethod("put");
    }


    /**
     * True if this request uses HTTP DELETE method, false otherwise.
     *
     * @return True if this request uses HTTP DELETE method, false otherwise.
     */
    public static boolean isDelete() {
        return isMethod("delete");
    }


    public static boolean isMethod(String method){
        return HttpMethod.getMethod(Context.getHttpRequest()).name().equalsIgnoreCase(method);
    }


    /**
     * True if this request uses HTTP HEAD method, false otherwise.
     *
     * @return True if this request uses HTTP HEAD method, false otherwise.
     */
    public static boolean isHead() {
        return isMethod("head");
    }

    /**
     * Provides a context of the request - usually an app name (as seen on URL of request). Example:
     * <code>/mywebapp</code>
     *
     * @return a context of the request - usually an app name (as seen on URL of request).
     */
    public static String context(){
        return Context.getHttpRequest().getContextPath();
    }

    /**
     * Returns URI, or a full path of request. This does not include protocol, host or port. Just context and path.
     * Examlpe: <code>/mywebapp/controller/action/id</code>
     * @return  URI, or a full path of request.
     */
    public static String uri(){
        return Context.getHttpRequest().getRequestURI();
    }

    /**
     * Host name of the requesting client.
     *
     * @return host name of the requesting client.
     */
    public static String remoteHost(){
        return Context.getHttpRequest().getRemoteHost();
    }

    /**
     * IP address of the requesting client.
     *
     * @return IP address of the requesting client.
     */
    public static String remoteAddress(){
        return Context.getHttpRequest().getRemoteAddr();
    }



    /**
     * Returns a request header by name.
     *
     * @param name name of header
     * @return header value.
     */
    public static String header(String name){
        return Context.getHttpRequest().getHeader(name);
    }

    /**
     * Returns all headers from a request keyed by header name.
     *
     * @return all headers from a request keyed by header name.
     */
    public static Map<String, String> headers(){
        Map<String, String> headers = new HashMap<String, String>();
        Enumeration<String> names = Context.getHttpRequest().getHeaderNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            headers.put(name, Context.getHttpRequest().getHeader(name));
        }
        return headers;
    }
}
