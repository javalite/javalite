package org.javalite.templator;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import static org.javalite.common.Collections.map;

/**
 * This is not a real spec. This is a performance test to see which templating engine is faster,
 * hand-written or generated from BNF. Looks like hand-written is 3 - 5 times faster.
 *
 * @author Igor Polevoy on 4/15/15.
 */
public class CompareSpec {

    public static class Person {
        private String firstName, lastName;

        public Person(String firstName, String lastName) {
            this.firstName = firstName;
            this.lastName = lastName;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }
    }

    private List<Person> people;
    private long millis;


    @Before
    public void before() {
        Runtime.getRuntime().gc();
        people = new ArrayList<Person>();
        for (int i = 0; i < 10000; i++) {
            people.add(new Person("first-" + i, "last-" + i));
        }
    }


    class NopeStringWriter extends StringWriter{
        @Override
        public void write(int c) {}
        @Override
        public void write(char[] cbuf, int off, int len) {}
        @Override
        public void write(String str) {}
        @Override
        public void write(String str, int off, int len) {}
        @Override
        public String toString() {return "";}
        @Override
        public StringWriter append(CharSequence csq) {return this;}
        @Override
        public StringWriter append(CharSequence csq, int start, int end) {return this;}
        @Override
        public StringWriter append(char c) {return this;}
    }

    @Test
    public void shouldIterate1() {
        String source = "<html><#list people as person > name: ${person.firstName} ${person.lastName} </#list></html>";
        Template template = new Template(source);
        StringWriter w = new NopeStringWriter();
        millis = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            template.process(map("people", people), w);
        }
        System.out.println("Completed in " + (System.currentTimeMillis() - millis) + " milliseconds");
    }


    @Test
    public void shouldIterate2() throws IOException {
        TemplateTagNode node = (TemplateTagNode) new TemplateParser("<html><#for person:people> name: %{person.firstName} %{person.lastName}  </#for></html>").parse();
        StringWriter sw = new NopeStringWriter();
        millis = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            node.process(map("people", people), sw);
        }
        System.out.println("Completed in " + (System.currentTimeMillis() - millis) + " milliseconds");
    }
}
