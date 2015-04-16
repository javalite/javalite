package org.javalite.templator.template_parser;

import java.io.IOException;
import java.util.Map;

/**
 * @author Eric Nielsen
 */
public interface TemplateNode {
    void process(Map values, Appendable appendable) throws IOException;
}
