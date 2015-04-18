package org.javalite.templator;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

/**
 * @author Igor Polevoy on 1/10/15.
 */
abstract class TemplateToken extends MethodExecutor {
    abstract void process(Map values, Writer writer) throws Exception;
    abstract String originalValue();
}
