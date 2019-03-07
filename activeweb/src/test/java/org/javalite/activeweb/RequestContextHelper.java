package org.javalite.activeweb;

import javax.servlet.http.HttpSession;

public class RequestContextHelper {

    public static void createSession() {
        RequestContext.getHttpRequest().getSession(true);
    }

    public static HttpSession getSession() {
        return RequestContext.getHttpRequest().getSession(false);
    }

}
