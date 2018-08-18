package org.javalite.activejdbc;


import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.Person;
import org.javalite.activejdbc.test_models.Student;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.javalite.activejdbc.test.JdbcProperties.driver;
import static org.javalite.common.Collections.map;

/**
 * @author igor on 12/9/17.
 */
public class FreemarkerSpec extends ActiveJDBCTest {

    private freemarker.template.Configuration config = new freemarker.template.Configuration();

    @Before
    public void  beforeTest(){
        config.setObjectWrapper(new DefaultObjectWrapper());
        config.setAPIBuiltinEnabled(true);
    }


    @Test
    public void shouldRenderSingleInstance() throws IOException, TemplateException {
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
        String processedTemplate = writer.toString().trim();
        if(System.getProperty("os.name").contains("indows")) {
            processedTemplate = processedTemplate.replaceAll("\r\n", "\n");
        }
        the(processedTemplate).shouldBeEqual("Person: John  Smith, graduation date: \n" +
                "Person: Leylah  Jonston, graduation date: Apr 3, 1974\n" +
                "Person: Muhammad  Ali, graduation date: Jan 4, 1963\n" +
                "Person: Joe  Pesci, graduation date: Feb 23, 1964");
    }

    @Test
    public void shouldRenderRowProcessor() throws IOException, TemplateException {
        deleteAndPopulateTable("students");

        Student cary = Student.findFirst("last_name = ?", "Cary");
        cary.set("enrollment_date", null).saveIt();

        List<Map> students = new ArrayList<>();
        Base.find("select * from students order by id").with(new RowListenerAdapter() {
            @Override
            public void onNext(Map<String, Object> row) {
                students.add(row);
            }
        });

        Template template = config.getTemplate("src/test/resources/list_row.ftl");

        StringWriter writer = new StringWriter();
        template.process(map("students", students), writer);
        String processedTemplate = writer.toString().trim();
        if(System.getProperty("os.name").contains("indows")) {
            processedTemplate = processedTemplate.replaceAll("\r\n", "\n");
        }

        if(driver().equals("org.sqlite.JDBC")){
            the(processedTemplate).shouldBeEqual("Student: Jim  Cary, enrollment date:\n" +
                    "Student: John  Carpenter, enrollment date: 1987-01-29 13:00:00");
        }else {
            the(processedTemplate).shouldBeEqual("Student: Jim  Cary, enrollment date:\n" +
                    "Student: John  Carpenter, enrollment date: Jan 29, 1987 1:00:00 PM");
        }
    }
}
