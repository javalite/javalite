package org.javalite.templator;

import java.io.StringWriter;

import org.javalite.templator.template_parser.TemplateNode;
import org.javalite.templator.template_parser.TemplateParser;
import org.javalite.templator.template_parser.TemplateTagNode;
import org.junit.Test;

import java.util.Map;
import static org.javalite.common.Collections.*;
import static org.javalite.test.jspec.JSpec.*;

/**
 * @author Eric Nielsen
 */
public class TemplateParserSpec {

    private String process(TemplateNode node, Map values) throws Exception {
        StringWriter sw = new StringWriter();
        node.process(values, sw);
        return sw.toString();
    }

    @Test
    public void shouldParseTextTemplate() throws Exception {
        String source = "this is just plain text, no strings attached";
        TemplateTagNode node = (TemplateTagNode) new TemplateParser(source).parse();
        the(node.children().size()).shouldBeEqual(1);
        the(process(node, map())).shouldBeEqual(source);
    }

    @Test
    public void shouldTokenizeTemplate() throws Exception {
        TemplateTagNode node = (TemplateTagNode) new TemplateParser(
                "Hello %{first_name}, your code is <b>%{%{foo.size()}%{invalid()}}</b> %{one.two.three.four}")
                .parse();
        the(node.children().size()).shouldBeEqual(6);
        the(process(node, map(
                "first_name", "John",
                "foo", map(),
                "one", map("two", map("three", map("four", "five")))
        ))).shouldBeEqual("Hello John, your code is <b>%{0%{invalid()}}</b> five");
    }

    @Test
    public void shouldParseBuiltIn() throws Exception {
        TemplateTagNode node = (TemplateTagNode) new TemplateParser("<b>%{article.content esc}</b>").parse();
        the(node.children().size()).shouldBeEqual(3);
        the(process(node, map("article", map("content", "R&B")))).shouldBeEqual("<b>R&amp;B</b>");
    }

    @Test
    public void shouldParseIfTagWithEq() throws Exception {
        TemplateTagNode node = (TemplateTagNode) new TemplateParser("<b><<#if(left==right)>tada</#if>></b>").parse();
        the(node.children().size()).shouldBeEqual(3);
        the(process(node, map("left", "help!", "right", "help!"))).shouldBeEqual("<b><tada></b>");
        the(process(node, map("left", "help!", "right", "help?"))).shouldBeEqual("<b><></b>");
    }

    @Test
    public void shouldParseIfTagWithNotNeq() throws Exception {
        TemplateTagNode node = (TemplateTagNode) new TemplateParser("<#if(!left!=right)>tada</#if>").parse();
        the(node.children().size()).shouldBeEqual(1);
        the(process(node, map("left", 0, "right", 0))).shouldBeEqual("tada");
        the(process(node, map("left", 0, "right", 1))).shouldBeEqual("");
    }

    @Test
    public void shouldParseIfTagWithAndParensOr() throws Exception {
        TemplateTagNode node = (TemplateTagNode) new TemplateParser(
                "<#if (first > second && (first > third || third <= second))><b>%{first}</b></#if>").parse();
        the(node.children().size()).shouldBeEqual(1);
        the(process(node, map("first", "foo", "second", "bar", "third", "baa"))).shouldBeEqual("<b>foo</b>");
        the(process(node, map("first", 1, "second", 0, "third", 0))).shouldBeEqual("<b>1</b>");
        the(process(node, map("first", 1, "second", 0, "third", 1))).shouldBeEqual("");
    }

    @Test
    public void shouldParseIfTagWithBoolean() throws Exception {
        TemplateTagNode node = (TemplateTagNode) new TemplateParser("<#if (foo)>bar</#if>").parse();
        the(node.children().size()).shouldBeEqual(1);
        the(process(node, map("foo", true))).shouldBeEqual("bar");
        the(process(node, map("foo", false))).shouldBeEqual("");
    }

    @Test
    public void shouldNotParseInvalidIfTagWithBoolean() throws Exception {
        String source = "<#if (foo=)>bar</#if>";
        TemplateTagNode node = (TemplateTagNode) new TemplateParser(source).parse();
        the(node.children().size()).shouldBeEqual(1);
        the(process(node, map())).shouldBeEqual(source);
    }

    @Test
    public void shouldParseForTag() throws Exception {
        TemplateTagNode node = (TemplateTagNode) new TemplateParser("<#for item:list> %{item} </#for>").parse();
        the(node.children().size()).shouldBeEqual(1);
        the(process(node, map("list", list(1, 2, 3)))).shouldBeEqual(" 1  2  3 ");
    }

    @Test
    public void shouldParseIfAndForTagsWithNestedValues() throws Exception {
        TemplateTagNode node = (TemplateTagNode) new TemplateParser(
                "<#if (duck.name == name)>%{duck.name}'s nephews: <#for nephew : duck.nephews>"
                        + "%{nephew_index}. %{nephew.name}<#if (nephew_has_next)>, </#if>"
                        + "</#for></#if>").parse();
        the(node.children().size()).shouldBeEqual(1);
        the(process(node, map("name", "Donald", "duck", map("name", "Donald", "nephews",
                list(map("name", "Huey"), map("name", "Dewey"), map("name", "Louie"))))))
                .shouldBeEqual("Donald's nephews: 0. Huey, 1. Dewey, 2. Louie");
    }
}
