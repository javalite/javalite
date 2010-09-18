package activejdbc;

import activejdbc.test.ActiveJDBCTest;
import activejdbc.test_models.Person;
import activejdbc.validation.Validator;
import activejdbc.validation.ValidatorAdapter;
import org.junit.Test;

/**
 * @author Igor Polevoy
 */
public class CustomValidatorTest extends ActiveJDBCTest {

    @Test
    public void shouldRegisterCustomValidator() {

        Validator mock = new ValidatorAdapter() {
            @Override
            public void validate(Model m) {
               m.addError("custom_message", "this is a test message!");
            }
        };

        Person.addValidator(mock);

        Person p = new Person();
        p.validate();
        a(p.errors().size()).shouldBeEqual(3);

        //this is so that other tests succeed
        Person.removeValidator(mock);
    }
}
