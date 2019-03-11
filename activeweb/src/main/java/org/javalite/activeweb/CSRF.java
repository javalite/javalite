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

    public static final String PARAMETER_NAME = "_csrfToken";
    public static final String HTTP_HEADER_NAME = "X-CSRF-Token";

    interface TokenProvider {
        String nextToken();
    }

    private static class SecureRandomTokenProvider implements TokenProvider {

        private ThreadLocal<SecureRandom> secureRandom = ThreadLocal.withInitial(() -> {
            try {
                return SecureRandom.getInstance("SHA1PRNG");
            } catch(NoSuchAlgorithmException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        });

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

    public static String token() {
        HttpServletRequest request = RequestContext.getHttpRequest();
        if (request == null) {
            throw new RuntimeException("Request not found!");
        }
        HttpSession session = request.getSession(false);
        String token = (String) session.getAttribute(PARAMETER_NAME);
        if (token == null) {
            token = tokenProvider.get().nextToken();
            session.setAttribute(PARAMETER_NAME, token);
        }
        return token;
    }

}
