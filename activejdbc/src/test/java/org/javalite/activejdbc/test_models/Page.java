package org.javalite.activejdbc.test_models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.validation.Converter;
import org.javalite.common.Convert;

import java.util.Locale;

/**
 * @author Igor Polevoy
 */
public class Page extends Model {
    static {
        validateNumericalityOf("word_count").greaterThan(10).onlyInteger().message("'word_count' must be a number greater than 10");
        convertWith(new IntegerConverter("word_count"));
    }
}

class IntegerConverter extends Converter {

    String name;

    IntegerConverter(String name) {
        this.name = name;
    }

    @Override
    public void setMessage(String message) {}

    @Override
    public String formatMessage(Locale locale, Object... params) {
        return "Failed to format " + name + " to Integer";
    }

    @Override
    public void convert(Model m) {
        try {
            Integer val = Convert.toInteger(m.get(name));
            m.set(name, val);
        } catch (Exception e) {
            m.addValidator(this, name);
        }
    }
}
