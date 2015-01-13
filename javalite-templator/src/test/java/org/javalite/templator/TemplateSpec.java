package org.javalite.templator;

import org.javalite.templator.tags.ListTag;
import org.junit.Test;

import java.io.StringWriter;
import java.util.List;

import static org.javalite.common.Collections.list;
import static org.javalite.common.Collections.map;
import static org.javalite.test.jspec.JSpec.a;

/**
 * @author Igor Polevoy on 1/10/15.
 */
public class TemplateSpec {

    @Test
    public void shouldTokenizeTemplate() {
        String source = "<html>Hello, ${first_name} - your last name is ${last_name}</html>";
        Template template = new Template(source);
        List<TemplateToken> tokens = template.builtInTokens();
        a(tokens.size()).shouldBeEqual(5);
        a(tokens.get(0).originalValue()).shouldBeEqual("<html>Hello, ");
        a(tokens.get(1).originalValue()).shouldBeEqual("first_name");
        a(tokens.get(2).originalValue()).shouldBeEqual(" - your last name is ");
        a(tokens.get(3).originalValue()).shouldBeEqual("last_name");
        a(tokens.get(4).originalValue()).shouldBeEqual("</html>");
    }

    @Test
    public void shouldMergeSimpleTemplate() {
        String source = "<html>Hello, ${first_name} - your last name is ${last_name}</html>";
        Template template = new Template(source);
        StringWriter w = new StringWriter();
        template.process(map("first_name", "John", "last_name", "Doe"), w);
        a(w.toString()).shouldBeEqual("<html>Hello, John - your last name is Doe</html>");
    }

    @Test(expected = ParseException.class)
    public void should_reject_merge_token_with_more_than_one_dot() {
        String source = "<html>Hello, ${person.first_name.last_name}";
        new Template(source);
    }


    public static class Person1 {
        public String firstName, lastName;
    }

    @Test
    public void shouldMergeTemplateWithPublicFields() {
        String source = "<html>Hello, ${person.firstName} - your last name is ${person.lastName}</html>";
        Template template = new Template(source);

        Person1 p = new Person1();
        p.firstName = "John";
        p.lastName = "Doe";
        StringWriter w = new StringWriter();
        template.process(map("person", p), w);
        a(w.toString()).shouldBeEqual("<html>Hello, John - your last name is Doe</html>");
    }


    public static class Person2 {
        private String firstName, lastName;
        public Person2(String firstName, String lastName) { this.firstName = firstName; this.lastName = lastName; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
    }

    @Test
    public void shouldMergeTemplateWithObjectProperties() {
        String source = "<html>Hello, ${person.firstName} - your last name is ${person.lastName}</html>";
        Template template = new Template(source);
        Person2 p = new Person2("John", "Doe");
        StringWriter w = new StringWriter();
        template.process(map("person", p), w);
        a(w.toString()).shouldBeEqual("<html>Hello, John - your last name is Doe</html>");
    }

    @Test
    public void shouldMergeTemplateWithMap() {
        String source = "<html>Hello, ${person.first_name} - your last name is ${person.last_name}</html>";
        Template template = new Template(source);
        StringWriter w = new StringWriter();
        template.process(map("person", map("first_name", "John", "last_name", "Doe")), w);
        a(w.toString()).shouldBeEqual("<html>Hello, John - your last name is Doe</html>");
    }


    public static class Person3 {
        private String firstName, lastName;
        public Person3(String firstName, String lastName) { this.firstName = firstName; this.lastName = lastName; }
        public String get(String attribute) {
            if(attribute.equals("first_name"))
            return firstName;
            else if(attribute.equals("last_name")){
                return lastName;
            }else{
                return null;
            }
        }
    }

    @Test // this is for ActiveJDBC models.
    public void shouldMergeTemplateWithGenericGetters() {
        String source = "<html>Hello, ${person.first_name} - your last name is ${person.last_name}</html>";
        Template template = new Template(source);
        Person3 p = new Person3("John", "Doe");
        StringWriter w = new StringWriter();
        template.process(map("person", p), w);
        a(w.toString()).shouldBeEqual("<html>Hello, John - your last name is Doe</html>");
    }


    @Test
    public void shouldParseSimpleTag() {
        TemplatorConfig.instance().registerTag("list", new ListTag());
        String source = "<html><#list people as person /></html>";
        Template template = new Template(source);
        a(template.builtInTokens().size()).shouldBeEqual(3);
        AbstractTag t = (AbstractTag) template.builtInTokens().get(1);
        a(t.getBody()).shouldBeNull();
    }

    @Test
    public void shouldParseTagWithBody() {
        TemplatorConfig.instance().registerTag("list", new ListTag());
        String source = "<html><#list people as person > body </#list></html>";
        Template template = new Template(source);
        a(template.builtInTokens().size()).shouldBeEqual(3);
        AbstractTag t = (AbstractTag) template.builtInTokens().get(1);
        a(t.getBody()).shouldBeEqual(" body ");
    }

    @Test
    public void shouldIterateWithList() {
        TemplatorConfig.instance().registerTag("list", new ListTag());
        String source = "<html><#list people as person > name: ${person.firstName} ${person.lastName}, has more: ${person_has_next}, index: ${person_index} </#list></html>";
        Template template = new Template(source);
        Person2 p1 = new Person2("John", "Doe");
        Person2 p2 = new Person2("Jane", "Doe");

        StringWriter w = new StringWriter();
        template.process(map("people", list(p1, p2)), w);
        a(w.toString()).shouldBeEqual("<html> name: John Doe, has more: true, index: 0 name: Jane Doe, has more: false, index: 1</html>");
    }
}
