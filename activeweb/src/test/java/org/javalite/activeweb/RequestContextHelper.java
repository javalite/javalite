package org.javalite.activeweb;

import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class RequestContextHelper {

    public static HttpSession getSession() {
        HttpServletRequest httpServletRequest = RequestContext.getHttpRequest();
        if (httpServletRequest == null) {
            RequestContext.setHttpRequest(httpServletRequest = new MockHttpServletRequest());
        }
        return httpServletRequest.getSession(true);
    }

    public static void createSession() {
        getSession();
    }

}
