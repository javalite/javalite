package org.javalite.activeweb.controller_filters;

import org.javalite.activeweb.CSRF;
import org.javalite.activeweb.HttpMethod;

public class CSRFFilter extends HttpSupportFilter {


    public CSRFFilter() {
        CSRF.enableVerification();
    }

    @Override
    public void before() {
        if (CSRF.verificationEnabled()) {
            HttpMethod method = getRoute().getMethod();
            if (method == HttpMethod.POST || method == HttpMethod.DELETE || method == HttpMethod.PUT) {
                verify();
            }
        }
    }

    private void verify() {
        String sessionName = sessionString(CSRF.CSRF_TOKEN_NAME);
        String sessionToken = sessionString(CSRF.CSRF_TOKEN_VALUE);
        if (sessionToken != null && sessionName != null) {
            String token = param(sessionName);
            if (token == null) {
                token = header(CSRF.HTTP_HEADER_NAME);
            }
            if (token == null) {
                throw new SecurityException("CSRF attack detected! Token not found!");
            }
            if (!sessionToken.equals(token)) {
                throw new SecurityException("CSRF attack detected! Request token is not valid!");
            }
        } else {
            throw new SecurityException("CSRF attack detected! Session token missing!");
        }
    }
}
