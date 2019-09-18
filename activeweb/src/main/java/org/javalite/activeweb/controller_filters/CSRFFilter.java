package org.javalite.activeweb.controller_filters;

import org.javalite.activeweb.*;

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
            if (token == null && RequestUtils.isMultipartContent()) {
                int i = 0;
                for(FormItem fi : multipartFormItems()) {
                    if (fi.isFormField() && sessionName.equals(fi.getFieldName())) {
                        token = new String(fi.getBytes());
                        multipartFormItems().remove(i);
                        break;
                    }
                    i++;
                }
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
