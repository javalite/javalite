package org.javalite.common;

import org.junit.Test;

import static org.javalite.test.jspec.JSpec.the;

/**
 * @author Igor Polevoy on 1/15/15.
 */
public class EscapeTest {

    @Test
    public void shouldEscapeBasicHTML(){
        String html = "<html><script>alert(\"This & that, it's the problem.\");</script></html>";
        the(Escape.html(html)).shouldBeEqual("&lt;html&gt;&lt;script&gt;alert(&quot;This &amp; that, it&apos;s the problem.&quot;);&lt;/script&gt;&lt;/html&gt;");
    }
}
