package org.javalite.templator;

import java.io.StringWriter;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import static org.javalite.common.Collections.*;
import static org.javalite.test.jspec.JSpec.*;

/**
 * @author Eric Nielsen
 */
public class TemplateParserSpec {

    @Test
    public void shouldParseTextTemplate() throws Exception {
        String source = "this is just plain text, no strings attached";
        List<TemplateNode> tokens = new TemplateParser(source).parse();
        the(tokens.size()).shouldBeEqual(1);
        StringWriter sw = new StringWriter();
        tokens.get(0).process(map(), sw);
        the(sw.toString()).shouldBeEqual(source);
    }

    private String processAll(List<TemplateNode> nodes, Map values) throws Exception {
        StringWriter sw = new StringWriter();
        for (TemplateNode node : nodes) {
            node.process(values, sw);
        }
        return sw.toString();
    }

    @Test
    public void shouldTokenizeTemplate() throws Exception {
        List<TemplateNode> tokens = new TemplateParser(
                "Hello %{first_name}, your code is <b>%{%{foo.size()}%{invalid()}}</b> %{one.two.three.four}")
                .parse();
        the(tokens.size()).shouldBeEqual(6);
        the(processAll(tokens, map(
                "first_name", "John",
                "foo", map(),
                "one", map("two", map("three", map("four", "five")))
        ))).shouldBeEqual("Hello John, your code is <b>%{0%{invalid()}}</b> five");
    }

    @Test
    public void shouldParseBuiltIn() throws Exception {
        List<TemplateNode> tokens = new TemplateParser("<b>%{article.content esc}</b>").parse();
        the(tokens.size()).shouldBeEqual(3);
        the(processAll(tokens, map("article", map("content", "R&B")))).shouldBeEqual("<b>R&amp;B</b>");
    }
}
