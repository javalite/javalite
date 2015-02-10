package org.javalite.templator;

import java.io.IOException;
import java.io.Writer;

/**
 * Condenses consecutive whitespace characters into just one character. Multiple spaces become just one space.
 * This is useful for web services, where you need to strip all but the most necessary characters.
 *
 * @author Igor Polevoy on 2/9/15.
 */
public class WhiteSpaceWriter extends Writer {

    private Writer delegate;
    private char last;

    public WhiteSpaceWriter(Writer delegate) {
        this.delegate = delegate;
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {

        for (int i = off; i < (off + len); i++) {
            if (Character.isWhitespace(last) && Character.isWhitespace(cbuf[i])) {
                if (last == '\n' && cbuf[i] == '\n') {
                    continue;
                }

                if (last == Character.SPACE_SEPARATOR && cbuf[i] == Character.SPACE_SEPARATOR) {
                    continue;
                }

                if (last == Character.LINE_SEPARATOR && cbuf[i] == Character.LINE_SEPARATOR) {
                    continue;
                }

                if (last == Character.LINE_SEPARATOR && cbuf[i] == Character.SPACE_SEPARATOR) {
                    continue;
                }

                if (last == Character.SPACE_SEPARATOR && cbuf[i] == Character.LINE_SEPARATOR) {
                    delegate.write(cbuf[i]);
                    last = cbuf[i];
                }
            } else {
                delegate.write(cbuf[i]);
                last = cbuf[i];
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
