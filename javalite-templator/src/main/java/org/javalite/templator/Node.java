package org.javalite.templator;

import java.io.IOException;
import java.util.Map;

/**
 * @author Igor Polevoy
 * @author Eric Nielsen
 */
abstract class Node {
    abstract void process(Map values, Appendable appendable) throws IOException;
}
