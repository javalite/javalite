package org.javalite.templator.template_parser;

import java.util.List;

/**
 * @author Eric Nielsen
 */
public interface TemplateTagNode extends TemplateNode {
    String name();
    List<TemplateNode> children();
}
