package org.javalite.activeweb.controller_filters;

import org.javalite.activeweb.CSRF;
import org.javalite.activeweb.HttpMethod;

public class CSRFFilter extends HttpSupportFilter {


    public CSRFFilter() {
        CSRF.enableVerification();
    }

    @Override
    public void before() {
        HttpMethod method = getRoute().getMethod();
        if (method == HttpMethod.POST || method == HttpMethod.DELETE || method == HttpMethod.PUT) {
            verify();
        }
    }

    private void verify() {
        String sessionToken = sessionString(CSRF.PARAMETER_NAME);
        if (sessionToken != null) {
            String token = param(CSRF.PARAMETER_NAME);
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
