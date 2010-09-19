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
               m.addError("custom_message", "message.for.custom.validator");
            }
        };

        Person.addValidator(mock);

        Person p = new Person();
        p.validate();
        a(p.errors().size()).shouldBeEqual(3);

        a(p.errors().get("custom_message")).shouldBeEqual("this is a test message!");

        //this is so that other tests succeed
        Person.removeValidator(mock);
    }
}
