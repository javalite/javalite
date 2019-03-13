package org.javalite.activeweb;

import org.javalite.common.Util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Responsible for generating tokens for CSRF protection support.
 */
public class CSRF {

    public static final String CSRF_TOKEN_NAME = "CSRF_TOKEN_NAME";
    public static final String CSRF_TOKEN_VALUE = "CSRF_TOKEN_VALUE";
    public static final String HTTP_HEADER_NAME = "X-CSRF-Token";

    interface TokenProvider {
        String nextName();
        String nextToken();
    }

    private static class SecureRandomTokenProvider implements TokenProvider {

        private static final String name = "_csrfToken";
        private String base = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";

        private ThreadLocal<SecureRandom> secureRandom = ThreadLocal.withInitial(() -> {
            try {
                return SecureRandom.getInstance("SHA1PRNG");
            } catch(NoSuchAlgorithmException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        });

        @Override
        public String nextName() {
            StringBuilder name = new StringBuilder();
            SecureRandom sr = secureRandom.get();
            while(name.length() < 8) {
                name.append(base.charAt((int)(sr.nextFloat() * base.length())));
            }
            return name.toString();
        }

        @Override
        public String nextToken() {
            return Util.toBase64(secureRandom.get().generateSeed(32));
        }
    }

    private static AtomicBoolean enabled = new AtomicBoolean(false);

    private static AtomicReference<TokenProvider> tokenProvider = new AtomicReference<>(new SecureRandomTokenProvider());

    private static void setTokenProvider(TokenProvider provider) {
        tokenProvider.set(provider);
    }

    public static boolean verificationEnabled() {
        return enabled.get();
    }

    public static void enableVerification() {
        enabled.set(true);
    }

    public static void disableVerification() {
        enabled.set(false);
    }

    private static HttpSession getSession() {
        HttpServletRequest request = RequestContext.getHttpRequest();
        if (request == null) {
            throw new RuntimeException("Request not found!");
        }
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new RuntimeException("Session not initialized!");
        }
        return session;
    }

    public static String token() {
        HttpSession session = getSession();
        String token = (String) session.getAttribute(CSRF_TOKEN_VALUE);
        if (token == null) {
            token = tokenProvider.get().nextToken();
            session.setAttribute(CSRF_TOKEN_VALUE, token);
        }
        return token;
    }

    public static String name() {
        HttpSession session = getSession();
        String name = (String) session.getAttribute(CSRF_TOKEN_NAME);
        if (name == null) {
            name = tokenProvider.get().nextName();
            session.setAttribute(CSRF_TOKEN_NAME, name);
        }
        return name;
    }

}
