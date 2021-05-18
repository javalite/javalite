package org.javalite.activeweb;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Map;

import static org.javalite.common.Collections.map;
import static org.javalite.test.jspec.JSpec.a;

/**
 * @author Andrey Yanchevsky
 */
public class SessionFacadeSpec {

    private SessionFacade session;

    @Before
    public void before() {
        RequestContext.setHttpRequest(new MockHttpServletRequest());
        session = new SessionFacade();
    }

    @Test
    public void shouldNotExistsByDefault() {
        a(session.exists()).shouldBeFalse();
    }

    @Test
    public void shouldExistsIfUsed() {
        session.put("test", "text");
        a(session.exists()).shouldBeTrue();
    }

    @Test
    public void shouldSessionIdExists() {
        a(session.getId()).shouldBeNull();
        a(session.id()).shouldBeNull();
        session.put("test", "text");
        a(session.getId()).shouldNotBeNull();
        a(session.id()).shouldNotBeNull();
        a(session.getId()).shouldBeEqual(session.id());
    }

    @Test
    public void shouldGetPutRemoveValue() {
        a(session.get("test")).shouldBeNull();
        a(session.getOrDefault("test", 999)).shouldBeEqual(999);
        session.put("test", 100);
        a(session.get("test")).shouldEqual(100);
        a(session.get("test", Integer.class).getClass()).shouldBeEqual(Integer.class);
        session.putAll(map("1", 1, "2", 2, "3", 3));
        a(session.get("1")).shouldEqual(1);
        a(session.get("2")).shouldEqual(2);
        a(session.get("3")).shouldEqual(3);
        session.remove("1");
        a(session.get("1")).shouldBeNull();
        session.remove("2", 3);
        a(session.get("2")).shouldEqual(2);
        session.remove("2", 2);
        a(session.get("2")).shouldBeNull();
    }

    @Test
    public void shouldCreationTimeExists() {
        a(session.getCreationTime()).shouldEqual(-1);
        session.put("test", 100);
        a(session.getCreationTime() > 0).shouldBeTrue();
    }

    @Test
    public void shouldInvalidateSession() {
        session.put("test", 100);
        a(session.get("test")).shouldEqual(100);
        session.invalidate();
        a(session.exists()).shouldBeFalse();
    }

    @Test
    public void shouldDestroySession() {
        session.put("test", 100);
        a(session.get("test")).shouldEqual(100);
        session.destroy();
        a(session.exists()).shouldBeFalse();
    }

    @Test
    public void shouldBeNames() {
        session.putAll(map("1", 1, "2", 2, "3", 3));
        a(session.names().length).shouldEqual(3);
        session.remove("2");
        a(session.names().length).shouldEqual(2);
    }

    @Test
    public void shouldBeEmpty() {
        session.putAll(map("1", 1, "2", 2, "3", 3));
        a(session.isEmpty()).shouldBeFalse();
        session.clear();
        a(session.isEmpty()).shouldBeTrue();
    }

    @Test
    public void shouldContainsKeyOrValue() {
        session.putAll(map("1", 1, "2", 2, "3", 3));
        a(session.containsValue(2)).shouldBeTrue();
        a(session.containsValue(4)).shouldBeFalse();
        a(session.containsKey("1")).shouldBeTrue();
        a(session.containsKey("4")).shouldBeFalse();
        session.clear();
        a(session.containsKey("1")).shouldBeFalse();
        a(session.containsKey("4")).shouldBeFalse();
    }

    @Test
    public void shouldBeKeySet() {
        session.putAll(map("1", 1, "2", 2, "3", 3));
        a(session.keySet().size()).shouldBeEqual(3);
        a(session.keySet().contains("2")).shouldBeTrue();
    }

    @Test
    public void shouldBeEntrySet() {
        session.putAll(map("1", 1, "2", 2, "3", 3));
        a(session.entrySet().size()).shouldBeEqual(3);
        for(Map.Entry e : session.entrySet()) {
            a(e.getValue().toString()).shouldBeEqual(e.getKey());
        }
    }



}
