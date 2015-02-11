package org.javalite.templator;

import org.javalite.common.Util;
import org.junit.Ignore;
import org.junit.Test;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static junit.framework.TestCase.fail;
import static org.javalite.common.Collections.list;
import static org.javalite.common.Collections.map;
import static org.javalite.test.jspec.JSpec.a;

/**
 * @author Igor Polevoy on 1/10/15.
 */
public class TemplateSpec {

    @Test
    public void shouldProcessTextTemplate() {
        String source = "this is just plain text, no strings attached";
        Template template = new Template(source);

        StringWriter sw = new StringWriter();
        template.process(map(), sw);

        a(template.templateTokens().size()).shouldBeEqual(1);
        a(sw.toString()).shouldBeEqual("this is just plain text, no strings attached");
    }


    @Test
    public void shouldTokenizeTemplate() {
        String source = "<html>Hello, ${first_name} - your last name is ${last_name}</html>";
        Template template = new Template(source);
        List<TemplateToken> tokens = template.templateTokens();
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

    class Message {
        private final String message;

        private Message(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    @Test
    public void shouldMergeSimpleJson() {
        String source = "{\"message\":\"${message.message}\"}";
        Template template = new Template(source);

        StringWriter w = new StringWriter();
        template.process(map("message", new Message("Hello, world")), w);
        a(w.toString()).shouldBeEqual("{\"message\":\"Hello, world\"}");
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


    public static class BlahTag extends AbstractTag{
        public BlahTag() {
            super();
        }

        @Override
        public List<String> getEnds() {
            return list("/>");
        }

        @Override
        public void process(Map values, Writer writer) {
            try{writer.write("blah");}catch(Exception e){throw new TemplateException(e);}
        }
    }

    @Test
    public void shouldParseSimpleTag() {

        TemplatorConfig.instance().registerTag(BlahTag.class);

        String source = "<html><@blah arguments for blah /></html>";
        Template template = new Template(source);
        a(template.templateTokens().size()).shouldBeEqual(3);
        AbstractTag t = (AbstractTag) template.templateTokens().get(1);
        a(t.getArgumentLine()).shouldBeEqual(" arguments for blah ");
        a(t.getBody()).shouldBeNull();
    }

    @Test
    public void shouldParseMultipleTagsWithBody() {
        String source = "<html><#list people as person > body </#list> <#list people as person > body1 </#list> </html>";
        Template template = new Template(source);
        a(template.templateTokens().size()).shouldBeEqual(5);
        AbstractTag t = (AbstractTag) template.templateTokens().get(1);
        a(t.getBody()).shouldBeEqual(" body ");
        t = (AbstractTag) template.templateTokens().get(3);
        a(t.getBody()).shouldBeEqual(" body1 ");

    }

    @Test
    public void shouldIterateWithList() {
        String source = "<html><#list people as person > name: ${person.firstName} ${person.lastName}, has more: ${person_has_next}, index: ${person_index} </#list></html>";
        Template template = new Template(source);
        Person2 p1 = new Person2("John", "Doe");
        Person2 p2 = new Person2("Jane", "Doe");

        StringWriter w = new StringWriter();
        template.process(map("people", list(p1, p2)), w);
        a(w.toString()).shouldBeEqual("<html> name: John Doe, has more: true, index: 0  name: Jane Doe, has more: false, index: 1 </html>");
    }

    @Test
    public void shouldIterateWithListAndSimpleCondition() {

        String source = "<html><#list people as person>name: ${person.firstName} ${person.lastName}<#if person_has_next> <br> </#if></#list></html>";
        Template template = new Template(source);
        Person2 p1 = new Person2("John", "Doe");
        Person2 p2 = new Person2("Jane", "Kirkland");

        StringWriter w = new StringWriter();
        template.process(map("people", list(p1, p2)), w);
        a(w.toString()).shouldBeEqual("<html>name: John Doe <br> name: Jane Kirkland</html>");
    }


    public static class Person4 extends Person3{
        private List<String> habits = new ArrayList<String>();
        public Person4(String firstName, String lastName) {
            super(firstName, lastName);
        }

        public List<String> getHabits() {
            return habits;
        }

        public void addHabits(String ... habitsAr) {
            habits.addAll(list(habitsAr));
        }
    }

    @Test
    public void shouldrenderNestedTags() {
        String source = Util.readResource("/nested.html");

        Person4 jimmy = new Person4("Jimmy", "Henrdix");
        Person4 janice = new Person4("Janice", "Joplin");

        jimmy.addHabits("guitar playing", "prescription drugs");
        janice.addHabits("rock an roll", "cocaine");

        Template template = new Template(source);

        WhiteSpaceWriter w  = new WhiteSpaceWriter(new StringWriter());
        template.process(map("people", list(jimmy, janice)), w);

        a(w.toString()).shouldBeEqual(format("<html>%n" +
                "name: Jimmy Henrdix%n" +
                "Habit: guitar playing%n" +
                "Habit: prescription drugs%n" +
                "<br>%n" +
                "name: Janice Joplin%n" +
                "Habit: rock an roll%n" +
                "Habit: cocaine%n" +
                "</html>"));
    }

    @Test
    public void shouldUseBuiltIn() {

        String source = "<html>${article.content esc}</html>";
        Template template = new Template(source);
        StringWriter w = new StringWriter();
        template.process(map("article", map("content", "this & that")), w);

        a(w.toString()).shouldBeEqual("<html>this &amp; that</html>");

    }

    @Test
    public void shouldRejectWrongNumberOfArguments() {
        try{
            new Template("<html><#if one two three four> tada </#if></html>");
        }catch(Exception e){
            a(e.getMessage()).shouldBeEqual("Incorrect number of arguments for <#if> tag. Either provide a single value or expression with two operands.");
            return;
        }
        fail();
    }


    @Test
    public void shouldRejectWrongIncorrectOperator() {
        try{
            new Template("<html><#if one bad_operator four> tada </#if></html>");
        }catch(Exception e){
            a(e.getMessage()).shouldBeEqual("Cannot parse operator for <#if> tag: bad_operator. The following operators supported: [lt, gt, gt=, lt=, ||, &&, ==, !=]");
            return;
        }
        fail();
    }



    @Test
    public void shouldCompareNumbersWithLT() {

        String source = "<html><#if left lt right> tada </#if></html>";
        Template template = new Template(source);
        StringWriter w = new StringWriter();
        template.process(map("left", 2, "right", 3), w);
        a(w.toString()).shouldBeEqual("<html> tada </html>");

        source = "<html><#if left lt right> tada </#if></html>";
        template = new Template(source);
        w = new StringWriter();
        template.process(map("left", 3, "right", 2), w);
        a(w.toString()).shouldBeEqual("<html></html>");
    }

    @Test
    public void shouldCompareNumbersWithLTE() {

        String source = "<html><#if left lt= right> tada </#if></html>";
        Template template = new Template(source);
        StringWriter w = new StringWriter();
        template.process(map("left", 3, "right", 3), w);
        a(w.toString()).shouldBeEqual("<html> tada </html>");
    }

    @Test
    public void shouldCompareNumbersWithGTE() {

        String source = "<html><#if left gt= right> tada </#if></html>";
        Template template = new Template(source);
        StringWriter w = new StringWriter();
        template.process(map("left", 3, "right", 3), w);
        a(w.toString()).shouldBeEqual("<html> tada </html>");


        w = new StringWriter();
        template.process(map("left", 1, "right", 3), w);
        a(w.toString()).shouldBeEqual("<html></html>");
    }

    @Test
    public void shouldCompareNumbersWithGT() {

        String source = "<html><#if left gt right> tada </#if></html>";
        Template template = new Template(source);
        StringWriter w = new StringWriter();
        template.process(map("left", 3, "right", 2), w);
        a(w.toString()).shouldBeEqual("<html> tada </html>");

        source = "<html><#if left gt right> tada </#if></html>";
        template = new Template(source);
        w = new StringWriter();
        template.process(map("left", 2, "right", 3), w);
        a(w.toString()).shouldBeEqual("<html></html>");
    }


    @Test
    public void shouldCompareObjectsWithEQ() {

        String source = "<html><#if left == right> tada </#if></html>";
        Template template = new Template(source);
        StringWriter w = new StringWriter();
        template.process(map("left", "help", "right", "help"), w);
        a(w.toString()).shouldBeEqual("<html> tada </html>");

        w = new StringWriter();
        template.process(map("left", "help!", "right", "help?"), w);
        a(w.toString()).shouldBeEqual("<html></html>");
    }

    @Test
    public void shouldCompareObjectsWithNEQ() {

        String source = "<html><#if left != right> tada </#if></html>";
        Template template = new Template(source);
        StringWriter w = new StringWriter();
        template.process(map("left", "help!", "right", "help?"), w);
        a(w.toString()).shouldBeEqual("<html> tada </html>");

        w = new StringWriter();
        template.process(map("left", "help", "right", "help"), w);
        a(w.toString()).shouldBeEqual("<html></html>");
    }


    @Test
    public void shouldCompareBooleansWithOR() {

        String source = "<html><#if left || left> tada </#if></html>";
        Template template = new Template(source);
        StringWriter w = new StringWriter();
        template.process(map("left", true, "left", true), w);
        a(w.toString()).shouldBeEqual("<html> tada </html>");

        w = new StringWriter();
        template.process(map("left", true, "right", false), w);
        a(w.toString()).shouldBeEqual("<html> tada </html>");
    }

    @Test
    public void shouldCompareBooleansWithAND() {

        String source = "<html><#if left && right> tada </#if></html>";
        Template template = new Template(source);
        StringWriter w = new StringWriter();
        template.process(map("left", true, "right", true), w);
        a(w.toString()).shouldBeEqual("<html> tada </html>");

        w = new StringWriter();
        template.process(map("left", true, "right", false), w);
        a(w.toString()).shouldBeEqual("<html></html>");
    }



    @Test @Ignore //TODO
    public void implement1() {
        /*
        implement else:

         <#if condition >

         <else>

         <#if>

         */

    }

}
