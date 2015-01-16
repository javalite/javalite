package org.javalite.common;

import org.junit.Test;

import static org.javalite.test.jspec.JSpec.the;

/**
 * @author Igor Polevoy on 1/15/15.
 * @author ericbn
 */
public class EscapeTest {

    @Test
    public void shouldEscapeBasicHTML(){
        the(Escape.html("<script>alert(\"This & that, it's the problem.\");</script>")).shouldBeEqual(
                "&lt;script&gt;alert(&quot;This &amp; that, it&apos;s the problem.&quot;);&lt;/script&gt;");
    }

    @Test
    public void shouldNotEscapeHighUnicode() {
        String unicode = "\u30D5\u30EC\u30FC\u30E0\u30EF\u30FC\u30AF\u306E\u30D9\u30F3\u30C1\u30DE\u30FC\u30AF";
        the(Escape.html(unicode)).shouldBeEqual(unicode);
    }
}
