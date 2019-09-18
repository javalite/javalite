/*
Copyright 2009-2016 Igor Polevoy

Licensed under the Apache License, Version 2.0 (the "License"); 
you may not use this file except in compliance with the License. 
You may obtain a copy of the License at 

http://www.apache.org/licenses/LICENSE-2.0 

Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS, 
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
See the License for the specific language governing permissions and 
limitations under the License. 
*/
package org.javalite.activeweb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Igor Polevoy
 */
public class Cookie {

    private static Logger LOGGER = LoggerFactory.getLogger(Cookie.class);

    private java.lang.String name;
    private java.lang.String value;

    private int maxAge = -1;
    private java.lang.String domain;
    private java.lang.String path = "/";
    private boolean secure;
    private boolean httpOnly;
    private int version;

    public Cookie(java.lang.String name, String value) {
        this.name = name;
        this.value = value;
    }

    public Cookie(java.lang.String name, String value, boolean httpOnly) {
        this.name = name;
        this.value = value;
        this.httpOnly = httpOnly;
    }

    public void setMaxAge(int maxAge) {this.maxAge = maxAge;}

    public int getMaxAge() {return maxAge;}

    public void setPath(String path) {this.path = path;}

    public java.lang.String getPath() {return path;}

    public void setDomain(String domain) {this.domain = domain;}

    public java.lang.String getDomain() {return domain;}

    public void setSecure(boolean secure) {this.secure = secure;}

    public boolean isSecure() {return secure;}

    public String getName() { return name;}

    public void setValue(String value) {this.value = value;}

    public String getValue() { return value; }

    public int getVersion() { return version; }

    public void setVersion(int version) { this.version = version;}

    /**
     * Sets this cookie to be HTTP only.
     *
     * This will only work with Servlet 3
     */
    public void setHttpOnly(){httpOnly = true;}

    /**
     * Tells if a cookie HTTP only or not.
     *
     * This will only work with Servlet 3
     */
    public boolean isHttpOnly(){return httpOnly;}

    /**
     * Sets this cookie to be Http only or not
     */
    public void setHttpOnly(boolean httpOnly){
        this.httpOnly = httpOnly;
    }

    @Override
    public String toString() {
        return "Cookie{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                ", maxAge=" + maxAge +
                ", domain='" + domain + '\'' +
                ", path='" + path + '\'' +
                ", secure=" + secure +
                ", version=" + version +
                '}';
    }

    static Cookie fromServletCookie(javax.servlet.http.Cookie servletCookie){
        Cookie cookie = new Cookie(servletCookie.getName(), servletCookie.getValue());
        cookie.setMaxAge(servletCookie.getMaxAge());
        cookie.setDomain(servletCookie.getDomain());
        cookie.setPath(servletCookie.getPath());
        cookie.setSecure(servletCookie.getSecure());
        cookie.setVersion(servletCookie.getVersion());
        cookie.setHttpOnly(isHttpOnlyReflect(servletCookie));
        return cookie;
    }

    static javax.servlet.http.Cookie toServletCookie(Cookie cookie){
        javax.servlet.http.Cookie servletCookie = new javax.servlet.http.Cookie(cookie.getName(), cookie.getValue());
        servletCookie.setMaxAge(cookie.getMaxAge());
        if (cookie.getDomain() != null)
                servletCookie.setDomain(cookie.getDomain());
        servletCookie.setPath(cookie.getPath());
        servletCookie.setSecure(cookie.isSecure());
        servletCookie.setVersion(cookie.getVersion());
        setHttpOnlyReflect(cookie, servletCookie);
        return servletCookie;
    }

    //Need to call this by reflection for backwards compatibility with Servlet 2.5
    private static void setHttpOnlyReflect(org.javalite.activeweb.Cookie awCookie, javax.servlet.http.Cookie servletCookie){
        try {
            servletCookie.getClass().getMethod("setHttpOnly", boolean.class).invoke(servletCookie, awCookie.isHttpOnly());
        } catch (Exception e) {
            LOGGER.warn("You are trying to set HttpOnly on a cookie, but it appears you are running on Servlet version before 3.0.");
        }
    }

    //Need to call this by reflection for backwards compatibility with Servlet 2.5
    private static boolean isHttpOnlyReflect(javax.servlet.http.Cookie servletCookie){
        try {
            return (Boolean)servletCookie.getClass().getMethod("isHttpOnly").invoke(servletCookie);
        } catch (Exception e) {
            LOGGER.warn("You are trying to get HttpOnly from a cookie, but it appears you are running on Servlet version before 3.0. Returning false.. which can be false!");
            return false; //return default. Should we be throwing exception here?
        }
    }
}
