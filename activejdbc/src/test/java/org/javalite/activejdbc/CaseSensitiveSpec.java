package org.javalite.activejdbc;

import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.WildAnimal;
import org.junit.Test;

import static org.javalite.activejdbc.test.JdbcProperties.url;

/**
 * @author Igor Polevoy on 1/1/16.
 */
public class CaseSensitiveSpec extends ActiveJDBCTest {

    @Test// see definition of table name for WildAnimal
    public void should_pass_with_quotes_spaces_and_CamelCase(){
        if(url().contains("postgresql") || url().contains("h2")){ // ATTENTION, this is testing only H2 and PostgreSQL
            WildAnimal wildAnimal = new WildAnimal();
            wildAnimal.set("\"Name\"", "Cheetah").saveIt();
            a(WildAnimal.count()).shouldBeEqual(1);
        }
    }
}
