package org.javalite.activeweb;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class CSRFSpec extends RequestSpec {

    @Test
    public void shouldCreateUniqueTokens() throws InterruptedException {
        CSRF.verificationEnabled();
        int count = 10;
        Set store = Collections.synchronizedSet(new HashSet<>());
        CountDownLatch latch = new CountDownLatch(count);
        for(int i = 0; i < count; i++) {
            new Thread(() -> {
                try {
                    RequestContext.setTLs(new MockHttpServletRequest(), null, null, null, null, null);
                    RequestContext.getHttpRequest().getSession(true);
                    String token = CSRF.token();
                    store.add(token);
                } catch(Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            }).start();
        }
        latch.await();
        a(store.size()).shouldEqual(count);
    }

    @Test
    public void shouldCreateOneTokenForSession() {
        RequestContext.getHttpRequest().getSession(true);
        CSRF.verificationEnabled();
        int count = 10;
        Set store = new HashSet<>();
        for(int i = 0; i < count; i++) {
            store.add(CSRF.token());
        }
        a(store.size()).shouldEqual(1);
    }

}
