package org.javalite.activeweb;

import org.javalite.common.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static java.util.Arrays.asList;
import static org.apache.commons.fileupload.FileUploadBase.MULTIPART;
import static org.javalite.common.Collections.list;

/**
 * Provides access to request values.
 *
 * @author igor, on 6/16/14.
 */
public interface RequestAccess {

    Logger LOGGER = LoggerFactory.getLogger(RequestAccess.class);


    default boolean isMultipartContent() {
        String contentType = RequestContext.getHttpRequest().getContentType();
        return contentType != null && contentType.toLowerCase(Locale.ENGLISH).startsWith(MULTIPART);
    }

    /**
     * Returns value of routing user segment, or route wild card value, or request parameter.
     * If this name represents multiple values, this  call will result in {@link IllegalArgumentException}.
     *
     * @param name name of parameter.
     * @return value of routing user segment, or route wild card value, or request parameter.
     */
    default String param(String name){
        if(name.equals("id")){
            return getId();
        }else if(RequestContext.getRequestVo().getUserSegments().get(name) != null){
            return RequestContext.getRequestVo().getUserSegments().get(name);
        }else if(RequestContext.getRequestVo().getWildCardName() != null
                && name.equals(RequestContext.getRequestVo().getWildCardName())){
            return RequestContext.getRequestVo().getWildCardValue();
        }else{
            return RequestContext.getHttpRequest().getParameter(name);
        }
    }


    /**
     * Convenience method to get a parameter value in case <code>multipart/form-data</code> request was used.
     *
     * Returns a value of one named parameter from request. Will only return form fields, and not files.
     *
     * @param name name of parameter.
     * @param formItems form items retrieved from <code>multipart/form-data</code> request.
     * @return value of request parameter  from <code>multipart/form-data</code> request or null if not found.
     */
    default  String param(String name, List<FormItem> formItems) {
        for (FormItem formItem : formItems) {
            if(formItem.isFormField() && formItem.getFieldName().equals(name)){
                return formItem.getStreamAsString();
            }
        }
        return null;
    }

    /**
     * Returns value of ID if one is present on a URL. Id is usually a part of a URI, such as: <code>/controller/action/id</code>.
     * This depends on a type of a URI, and whether controller is RESTful or not.
     *
     * @return ID value from URI is one exists, null if not.
     */
    default String getId(){
        String paramId = RequestContext.getHttpRequest().getParameter("id");
        if(paramId != null && RequestContext.getHttpRequest().getAttribute("id") != null){
            LOGGER.warn("WARNING: probably you have 'id' supplied both as a HTTP parameter, as well as in the URI. Choosing parameter over URI value.");
        }

        String theId;
        if(paramId != null){
            theId =  paramId;
        }else{
            Object id = RequestContext.getHttpRequest().getAttribute("id");
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
    default String format(){
        return RequestContext.getFormat();
    }


    /**
     * Returns instance of {@link AppContext}.
     *
     * @return instance of {@link AppContext}.
     */
    default AppContext appContext(){
        return RequestContext.getAppContext();
    }


    /**
     * Returns true if this request is Ajax.
     *
     * @see <a href="http://en.wikipedia.org/wiki/List_of_HTTP_header_fields">List_of_HTTP_header_fields</a>
     * @return true if this request is Ajax.
     */
    default boolean isXhr(){
        String xhr = header("X-Requested-With");
        if(xhr == null) {
            xhr = header("x-requested-with");
        }
        return xhr != null && xhr.toLowerCase().equals("xmlhttprequest");
    }


    /**
     * Helper method, returns user-agent header of the request.
     *
     * @return user-agent header of the request.
     */
    default String userAgent(){
        String camel = header("User-Agent");
        return camel != null ? camel : header("user-agent");
    }

    /**
     * Synonym for {@link #isXhr()}.
     */
    default boolean xhr(){
        return isXhr();
    }


    /**
     * Returns instance of {@link Route} to be used for potential conditional logic inside controller filters.
     *
     * @return instance of {@link Route}
     */
    default Route getRoute(){
        return RequestContext.getRoute();
    }


    /**
     * Tests if a request parameter exists. Disregards the value completely - this
     * can be empty string, but as long as parameter does exist, this method returns true.
     *
     * @param name name of request parameter to test.
     * @return true if parameter exists, false if not.
     */
    default boolean exists(String name){
        return param(name) != null;
    }

    /**
     * Synonym of {@link #exists(String)}.
     *
     * @param name name of request parameter to test.
     * @return true if parameter exists, false if not.
     */
    default boolean requestHas(String name){
        return param(name) != null;
    }


    /**
     * Returns local host name on which request was received.
     *
     * @return local host name on which request was received.
     */
    default String host() {
        return RequestContext.getHttpRequest().getLocalName();
    }


    /**
     * Returns local IP address on which request was received.
     *
     * @return local IP address on which request was received.
     */
    default String ipAddress() {
        return RequestContext.getHttpRequest().getLocalAddr();
    }


    /**
     * This method returns a protocol of a request to web server if this container is fronted by one, such that
     * it sets a header <code>X-Forwarded-Proto</code> on the request and forwards it to the Java container.
     * If such header is not present, than the {@link #protocol()} method is used.
     *
     * @return protocol of web server request if <code>X-Forwarded-Proto</code> header is found, otherwise current
     * protocol.
     */
    default String getRequestProtocol(){
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
    default int getRequestPort(){
        String port = header("X-Forwarded-Port");
        return Util.blank(port)? port(): Integer.parseInt(port);
    }



    /**
     * Returns port on which the of the server received current request.
     *
     * @return port on which the of the server received current request.
     */
    default int port(){
        return RequestContext.getHttpRequest().getLocalPort();
    }


    /**
     * Returns protocol of request, for example: HTTP/1.1.
     *
     * @return protocol of request
     */
    default String protocol(){
        return RequestContext.getHttpRequest().getProtocol();
    }

    /**
     * This method returns a host name of a web server if this container is fronted by one, such that
     * it sets a header <code>X-Forwarded-Host</code> on the request and forwards it to the Java container.
     * If such header is not present, than the {@link #host()} method is used.
     *
     * @return host name of web server if <code>X-Forwarded-Host</code> header is found, otherwise local host name.
     */
    default String getRequestHost() {
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
    default String ipForwardedFor() {
        String h = header("X-Forwarded-For");
        return !Util.blank(h) ? h : remoteAddress();
    }


    /**
     * Returns multiple request values for a name.
     *
     * @param name name of multiple values from request.
     * @return multiple request values for a name.
     */
    default List<String> params(String name){
        String[] values = RequestContext.getHttpRequest().getParameterValues(name);
        List<String>valuesList = null;
        if (name.equals("id")) {
            if(values.length == 1){
                valuesList = Collections.singletonList(values[0]);
            }else if(values.length > 1){
                valuesList = asList(values);
            }
        } else {
            valuesList = values == null? new ArrayList<>() : list(values);
            String userSegment = RequestContext.getRequestVo().getUserSegments().get(name);
            if(userSegment != null){
                valuesList.add(userSegment);
            }
        }
        return valuesList;
    }


    /**
     * Convenience method to get parameter values in case <code>multipart/form-data</code> request was used.
     * Returns multiple request values for a name.
     *
     * @param name name of multiple values from request.
     * @param formItems form items retrieved from <code>multipart/form-data</code> request.
     * @return multiple request values for a name. Will ignore files, and only return form fields.
     */
    default List<String> params(String name, List<FormItem> formItems) {
        List<String> vals = new ArrayList<>();
        for (FormItem formItem : formItems) {
            if(formItem.isFormField() && formItem.getFieldName().equals(name)){
                vals.add(formItem.getStreamAsString());
            }
        }
        return vals;
    }

    /**
     * Returns a map where keys are names of all parameters, while values are the first value for each parameter, even
     * if such parameter has more than one value submitted.
     *
     * @return a map where keys are names of all parameters, while values are first value for each parameter, even
     * if such parameter has more than one value submitted.
     */
    default Map<String, String> params1st(){
        Map<String, String> params;
        if(RequestContext.getParams1st() == null){
            params = new HashMap<>();
            Enumeration names = RequestContext.getHttpRequest().getParameterNames();
            while (names.hasMoreElements()) {
                String name = names.nextElement().toString();
                params.put(name, RequestContext.getHttpRequest().getParameter(name));
            }

            if(getId() != null){
                params.put("id", getId());
            }

            Map<String, String> userSegments = RequestContext.getRequestVo().getUserSegments();
            params.putAll(userSegments);
            RequestContext.setParams1st(params);
            return params;
        }else{
            return RequestContext.getParams1st();
        }
    }

    /**
     * Convenience method to get parameters in case <code>multipart/form-data</code> request was used.
     *
     * Returns a map where keys are names of all parameters, while values are the first value for each parameter, even
     * if such parameter has more than one value submitted.
     *
     * @param formItems form items retrieved from <code>multipart/form-data</code> request.
     * @return a map where keys are names of all parameters, while values are first value for each parameter, even
     * if such parameter has more than one value submitted.
     */
    default Map<String, String> params1st(List<FormItem> formItems) {
        Map<String, String> vals = new HashMap<>();
        for (FormItem formItem : formItems) {
            if(formItem.isFormField() && !vals.containsKey(formItem.getFieldName())){
                vals.put(formItem.getFieldName(), formItem.getStreamAsString());
            }
        }
        return vals;
    }


    /**
     * Returns an instance of <code>java.util.Map</code> containing parameter names as keys and parameter values as map values.
     * The keys in the parameter map are of type String. The values in the parameter map are of type String array.
     *
     * @return an instance <code>java.util.Map</code> containing parameter names as keys and parameter values as map values.
     * The keys in the parameter map are of type String. The values in the parameter map are of type String array.
     */
    default Map<String, String[]> params(){
        SimpleHash params = new SimpleHash(RequestContext.getHttpRequest().getParameterMap());
        if(getId() != null)
            params.put("id", new String[]{getId()});

        Map<String, String> userSegments = RequestContext.getRequestVo().getUserSegments();

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
    default Locale locale(){
        return RequestContext.getHttpRequest().getLocale();
    }

    /**
     * Same as {@link #locale()}.
     *
     * @return locale of request.
     */
    default Locale getLocale(){
        return RequestContext.getHttpRequest().getLocale();
    }

    /**
     * Returns collection of all cookies browser sent.
     *
     * @return collection of all cookies browser sent.
     */
    default List<Cookie> cookies(){
        javax.servlet.http.Cookie[] servletCookies = RequestContext.getHttpRequest().getCookies();
        if(servletCookies == null)
            return new ArrayList<>();

        List<Cookie> cookies = new ArrayList<>();
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
    default Cookie cookie(String name){
        javax.servlet.http.Cookie[] servletCookies = RequestContext.getHttpRequest().getCookies();
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
    default String cookieValue(String name){
        return cookie(name).getValue();
    }


    /**
     * Returns a path of the request. It does not include protocol, host, port or context. Just a path.
     * Example: <code>/controller/action/id</code>
     *
     * @return a path of the request.
     */
    default String path(){
        return RequestContext.getHttpRequest().getServletPath();
    }

    /**
     * Returns a full URL of the request, all except a query string.
     *
     * @return a full URL of the request, all except a query string.
     */
    default String url(){
        return RequestContext.getHttpRequest().getRequestURL().toString();
    }

    /**
     * Returns query string of the request.
     *
     * @return query string of the request.
     */
    default String queryString(){
        return RequestContext.getHttpRequest().getQueryString();
    }

    /**
     * Returns an HTTP method from the request.
     *
     * @return an HTTP method from the request.
     */
    default String method(){
        return RequestContext.getHttpRequest().getMethod();
    }

    /**
     * True if this request uses HTTP GET method, false otherwise.
     *
     * @return True if this request uses HTTP GET method, false otherwise.
     */
    default boolean isGet() {
        return isMethod("get");
    }


    /**
     * True if this request uses HTTP POST method, false otherwise.
     *
     * @return True if this request uses HTTP POST method, false otherwise.
     */
    default boolean isPost() {
        return isMethod("post");
    }


    /**
     * True if this request uses HTTP PUT method, false otherwise.
     *
     * @return True if this request uses HTTP PUT method, false otherwise.
     */
    default boolean isPut() {
        return isMethod("put");
    }


    /**
     * True if this request uses HTTP DELETE method, false otherwise.
     *
     * @return True if this request uses HTTP DELETE method, false otherwise.
     */
    default boolean isDelete() {
        return isMethod("delete");
    }


    default boolean isMethod(String method){
        return HttpMethod.getMethod(RequestContext.getHttpRequest()).name().equalsIgnoreCase(method);
    }


    /**
     * True if this request uses HTTP HEAD method, false otherwise.
     *
     * @return True if this request uses HTTP HEAD method, false otherwise.
     */
    default boolean isHead() {
        return isMethod("head");
    }

    /**
     * Provides a context of the request - usually an app name (as seen on URL of request). Example:
     * <code>/mywebapp</code>
     *
     * @return a context of the request - usually an app name (as seen on URL of request).
     */
    default String context(){
        return RequestContext.getHttpRequest().getContextPath();
    }

    /**
     * Returns URI, or a full path of request. This does not include protocol, host or port. Just context and path.
     * Examlpe: <code>/mywebapp/controller/action/id</code>
     * @return  URI, or a full path of request.
     */
    default String uri(){
        return RequestContext.getHttpRequest().getRequestURI();
    }

    /**
     * Host name of the requesting client.
     *
     * @return host name of the requesting client.
     */
    default String remoteHost(){
        return RequestContext.getHttpRequest().getRemoteHost();
    }

    /**
     * IP address of the requesting client.
     *
     * @return IP address of the requesting client.
     */
    default String remoteAddress(){
        return RequestContext.getHttpRequest().getRemoteAddr();
    }



    /**
     * Returns a request header by name.
     *
     * @param name name of header
     * @return header value.
     */
    default String header(String name){
        return RequestContext.getHttpRequest().getHeader(name);
    }

    /**
     * Returns all headers from a request keyed by header name.
     *
     * @return all headers from a request keyed by header name.
     */
    default Map<String, String> headers(){
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> names = RequestContext.getHttpRequest().getHeaderNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            headers.put(name, RequestContext.getHttpRequest().getHeader(name));
        }
        return headers;
    }

    /**
     * @param name name of an object  in session
     *
     * @return object in session, or null.
     */
    default Object session(String name){
        return RequestContext.getHttpRequest().getSession().getAttribute(name);
    }

    /**
     * Sets an object on a current session.
     *
     * @param name name of object
     * @param value value of object
     */
    default void session(String name, Object value){
        RequestContext.getHttpRequest().getSession().setAttribute(name, value);
    }

    default String getRequestProperties(){
        StringBuilder sb = new StringBuilder();
        HttpServletRequest request = RequestContext.getHttpRequest();
        sb.append("Request URL: ").append(request.getRequestURL()).append("\n");
        sb.append("ContextPath: ").append(request.getContextPath()).append("\n");
        sb.append("Query String: ").append(request.getQueryString()).append("\n");
        sb.append("URI Full Path: ").append(request.getRequestURI()).append("\n");
        sb.append("URI Path: ").append(request.getServletPath()).append("\n");
        sb.append("Method: ").append(request.getMethod()).append("\n");
        return sb.toString();
    }

    default String servletPath() {
        return RequestContext.getHttpRequest().getServletPath();
    }
}
