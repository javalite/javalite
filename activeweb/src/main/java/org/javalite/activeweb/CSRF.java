package org.javalite.activeweb;

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
        //HttpSession session = RequestContext.getHttpRequest().getSession(false);
        return "TEST";
    }

}
