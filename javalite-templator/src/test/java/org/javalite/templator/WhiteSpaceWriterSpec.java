package org.javalite.templator;

import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;

import static java.lang.String.format;
import static org.javalite.common.Util.readResource;
import static org.javalite.test.jspec.JSpec.a;

/**
 * @author Igor Polevoy on 2/9/15.
 */
public class WhiteSpaceWriterSpec {

    @Test
    public void shouldCondenseWhiteSpace() throws IOException {
        WhiteSpaceWriter w = new WhiteSpaceWriter(new StringWriter());
        w.write(readResource("/whitespace.txt"));
        a(w.toString()).shouldBeEqual("tada hello data oops tada");
    }
}
