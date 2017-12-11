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

    @Test
    public void shouldRenderTemplate() throws IOException, TemplateException {
        deleteAndPopulateTable("people");
        List<Person> people = Person.findAll().orderBy("id").limit(2);
        freemarker.template.Configuration config = new freemarker.template.Configuration();
        Template template = config.getTemplate("src/test/resources/template.ftl");
        for (int i = 0; i < people.size(); i++) {
            Person person = people.get(i);
            StringWriter writer = new StringWriter();
            template.process(map("person", person), writer);
            if(i == 0){
                the(writer.toString().trim()).shouldBeEqual("Person: John, Smith");
            }
            if(i == 1){
                the(writer.toString().trim()).shouldBeEqual("Person: Leylah, Jonston");
            }
        }
    }
}
