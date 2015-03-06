package org.javalite.templator;

import java.io.StringWriter;
import org.junit.Test;

import java.util.Map;
import static org.javalite.common.Collections.*;
import static org.javalite.test.jspec.JSpec.*;

/**
 * @author Eric Nielsen
 */
public class TemplateParserSpec {

    private String process(Node node, Map values) throws Exception {
        StringWriter sw = new StringWriter();
        node.process(values, sw);
        return sw.toString();
    }

    @Test
    public void shouldParseTextTemplate() throws Exception {
        String source = "this is just plain text, no strings attached";
        ParentNode node = (ParentNode) new TemplateParser(source).parse();
        the(node.children.size()).shouldBeEqual(1);
        the(process(node, map())).shouldBeEqual(source);
    }

    @Test
    public void shouldTokenizeTemplate() throws Exception {
        ParentNode node = (ParentNode) new TemplateParser(
                "Hello %{first_name}, your code is <b>%{%{foo.size()}%{invalid()}}</b> %{one.two.three.four}")
                .parse();
        the(node.children.size()).shouldBeEqual(6);
        the(process(node, map(
                "first_name", "John",
                "foo", map(),
                "one", map("two", map("three", map("four", "five")))
        ))).shouldBeEqual("Hello John, your code is <b>%{0%{invalid()}}</b> five");
    }

    @Test
    public void shouldParseBuiltIn() throws Exception {
        ParentNode node = (ParentNode) new TemplateParser("<b>%{article.content esc}</b>").parse();
        the(node.children.size()).shouldBeEqual(3);
        the(process(node, map("article", map("content", "R&B")))).shouldBeEqual("<b>R&amp;B</b>");
    }

    @Test
    public void shouldParseExpressionWithEq() throws Exception {
        ParentNode node = (ParentNode) new TemplateParser("<#if(left==right)>").parse();
        the(node.children.size()).shouldBeEqual(1);
        the(process(node, map("left", "help!", "right", "help!"))).shouldBeEqual("true");
        the(process(node, map("left", "help!", "right", "help?"))).shouldBeEqual("false");
    }

    @Test
    public void shouldParseExpressionWithNotNeq() throws Exception {
        ParentNode node = (ParentNode) new TemplateParser("<#if(!left!=right)>").parse();
        the(node.children.size()).shouldBeEqual(1);
        the(process(node, map("left", 0, "right", 0))).shouldBeEqual("true");
        the(process(node, map("left", 0, "right", 1))).shouldBeEqual("false");
    }

    @Test
    public void shouldParseExpressionWithAndParensOr() throws Exception {
        ParentNode node = (ParentNode) new TemplateParser("<#if (first > second && (first > third || third <= second))>").parse();
        the(node.children.size()).shouldBeEqual(1);
        the(process(node, map("first", "foo", "second", "bar", "third", "baa"))).shouldBeEqual("true");
        the(process(node, map("first", 1, "second", 0, "third", 0))).shouldBeEqual("true");
        the(process(node, map("first", 1, "second", 0, "third", 1))).shouldBeEqual("false");
    }
}
