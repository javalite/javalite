package org.javalite.templator;

import java.io.IOException;
import java.io.Writer;

/**
 * Condenses consecutive whitespace characters into just one character. Multiple white space chars become just one space.
 * This is useful for web services, where you need to strip all but the most necessary characters.
 *
 * @author Igor Polevoy on 2/9/15.
 */
public class WhiteSpaceWriter extends Writer {

    private Writer delegate;
    private byte last;

    public WhiteSpaceWriter(Writer delegate) {
        this.delegate = delegate;
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        String temp = new String(cbuf, off, len).replaceAll("\\r\\n|\\r|\\n", " "); // should take care of Windows, *nix and Mac
        byte[] bytes = temp.getBytes();
        for (int i = 0; i < bytes.length; i++) {
            if (!(Character.isWhitespace(last) && Character.isWhitespace(bytes[i]))) {
                delegate.write(bytes[i]);
                last = bytes[i];
            }
        }
    }

    @Override
    public void flush() throws IOException {
        delegate.flush();
    }

    @Override
    public void close() throws IOException {

        delegate.close();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}
