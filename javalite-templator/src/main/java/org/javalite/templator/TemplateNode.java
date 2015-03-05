package org.javalite.templator;

import java.util.Map;

/**
 * @author Igor Polevoy
 * @author Eric Nielsen
 */
abstract class TemplateNode {
    abstract void process(Map values, Appendable appendable) throws Exception;
}
