package org.javalite.activejdbc.validation.length;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.validation.ValidatorAdapter;

import java.util.Locale;

/**
 * Attribute length validator.
 */
public class AttributeLengthValidator extends ValidatorAdapter {

    private final String attribute;
    private LengthOption lengthOption;

    private AttributeLengthValidator(String attribute) {
        this.attribute = attribute;
    }

    public static AttributeLengthValidator on(String attribute) {
        return new AttributeLengthValidator(attribute);
    }

    @Override
    public void validate(Model m) {
        Object value = m.get(attribute);
        if (!(value instanceof String)) {
            throw new IllegalArgumentException("Attribute must be a String");
        }

        if (!lengthOption.validate((String) (m.get(attribute)))) {
            m.addValidator(this, attribute);
        }
    }

    public AttributeLengthValidator with(LengthOption lengthOption) {
        this.lengthOption = lengthOption;
        setMessage(lengthOption.getParametrizedMessage());
        return this;
    }

    @Override
    public String formatMessage(Locale locale, Object... params) {
        return super.formatMessage(locale, lengthOption.getMessageParameters());
    }
}
