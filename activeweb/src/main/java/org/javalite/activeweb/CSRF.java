package org.javalite.activeweb;

import org.javalite.common.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpSession;
import java.security.*;

/**
 * Responsible for generating tokens for CSRF protection support.
 */
public class CSRF {

    interface TokenProvider {
        String nextToken();
    }

    private static class SecureRandomTokenProvider implements TokenProvider {

        private SecureRandom secureRandom;

        private SecureRandomTokenProvider() {
            try {
                secureRandom = SecureRandom.getInstance("SHA1PRNG");
            } catch(NoSuchAlgorithmException e) {
                logger.error(e.getMessage());
                throw new RuntimeException(e.getMessage(), e);
            }
        }

        @Override
        public String nextToken() {
            return Util.toBase64(secureRandom.generateSeed(32));
        }
    }

    private static Logger logger = LoggerFactory.getLogger(CSRF.class);

    private static boolean enabled = false;

    private static final String HMAC_ALG = "HMAC_MD5";

    public static final String PARAMETER_NAME = "_csrfToken";
    public static final String HTTP_HEADER_NAME = "X-CSRF-Token";

    private static TokenProvider tokenProvider;

    private static void setTokenProvider(TokenProvider provider) {
        tokenProvider = provider;
    }

    public static boolean verificationEnabled() {
        setTokenProvider(new SecureRandomTokenProvider());
        System.out.println("-- tokenProvider: " + tokenProvider);
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
                    System.out.println("tokenProvider: " + tokenProvider);
                    token = tokenProvider.nextToken();
                    session.setAttribute(PARAMETER_NAME, token);
                }
            }
        }
        return token;
    }

}
