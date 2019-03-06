package org.javalite.activeweb;

import javax.servlet.http.HttpSession;

public class CSRF {

    private static boolean enabled = false;

    public static final String PARAMETER_NAME = "_csrfToken";
    public static final String HTTP_HEADER_NAME = "X-CSRF-Token";

    public static boolean verificationEnabled() {
        return enabled;
    }

    public static void enableVerification() {
        enabled = true;
    }

    public static String token() {
        HttpSession session = RequestContext.getHttpRequest().getSession(false);
        String token = (String) session.getAttribute(PARAMETER_NAME);
        if (token == null) {
            synchronized (session) {
                token = (String) session.getAttribute(PARAMETER_NAME);
                if (token == null) {
                    token = generateToken();
                    session.setAttribute(PARAMETER_NAME, token);
                }
            }
        }
        return token;
    }

    private static String generateToken() {

        //TODO implement HMAC
        return "TEST";
    }

}
