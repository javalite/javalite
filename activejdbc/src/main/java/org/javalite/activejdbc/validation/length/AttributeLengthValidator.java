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
    private boolean allowBlank;

    private AttributeLengthValidator(String attribute) {
        this.attribute = attribute;
    }

    public static AttributeLengthValidator on(String attribute) {
        return new AttributeLengthValidator(attribute);
    }

    public void validate(Model m) {
        Object value = m.get(this.attribute);

        if(allowBlank && (null == value || "".equals(value))) {
            return;
        }

        if(null == value) {
            m.addValidator(this, this.attribute);
            return;
        }

        if(!(value instanceof String)) {
            throw new IllegalArgumentException("Attribute must be a String");
        } else {
            if(!this.lengthOption.validate((String)((String)m.get(this.attribute)))) {
                //somewhat confusingly this adds an error for a validator.
                m.addValidator(this, this.attribute);
            }

        }
    }

    public AttributeLengthValidator with(LengthOption lengthOption) {
        this.lengthOption = lengthOption;
        this.setMessage(lengthOption.getParametrizedMessage());
        return this;
    }

    public AttributeLengthValidator allowBlank(boolean allowBlank) {
        this.allowBlank = allowBlank;
        return this;
    }

    public String formatMessage(Locale locale, Object... params) {
        return super.formatMessage(locale, this.lengthOption.getMessageParameters());
    }
}
