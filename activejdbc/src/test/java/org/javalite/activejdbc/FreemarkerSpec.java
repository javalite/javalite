package org.javalite.activejdbc;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.Person;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import static org.javalite.common.Collections.map;

/**
 * @author igor on 12/9/17.
 */
public class FreemarkerSpec extends ActiveJDBCTest {

    freemarker.template.Configuration config = new freemarker.template.Configuration();

    @Test
    public void shouldRenderSingleIntance() throws IOException, TemplateException {
        deleteAndPopulateTable("people");

        Person smith = Person.findFirst("last_name = ?", "Smith");
        smith.set("graduation_date", null).saveIt();

        freemarker.template.Configuration config = new freemarker.template.Configuration();
        Template template = config.getTemplate("src/test/resources/single.ftl");

        smith = Person.findFirst("last_name = ?", "Smith");

        StringWriter writer = new StringWriter();
        template.process(map("person", smith), writer);

        the(writer.toString().trim()).shouldBeEqual("Person: John  Smith, graduation date:");
    }

    @Test
    public void shouldRenderList() throws IOException, TemplateException {
        deleteAndPopulateTable("people");

        Person smith = Person.findFirst("last_name = ?", "Smith");
        smith.set("graduation_date", null).saveIt();

        List<Person> people = Person.findAll().orderBy("id");

        Template template = config.getTemplate("src/test/resources/list.ftl");
        StringWriter writer = new StringWriter();

        template.process(map("people", people), writer);
        the(writer.toString().trim()).shouldBeEqual("Person: John  Smith, graduation date: \n" +
                "Person: Leylah  Jonston, graduation date: Apr 3, 1974\n" +
                "Person: Muhammad  Ali, graduation date: Jan 4, 1963\n" +
                "Person: Joe  Pesci, graduation date: Feb 23, 1964");

    }
}
