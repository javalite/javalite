package org.javalite.activejdbc.validation.length;


import org.javalite.activejdbc.validation.Validatable;
import org.javalite.activejdbc.validation.ValidatorAdapter;

import java.util.Locale;


/**
 * Attribute length validator.
 */
public class AttributeLengthValidator extends ValidatorAdapter {
    private final String attributeName;
    private LengthOption lengthOption;
    private boolean allowBlank;

    private AttributeLengthValidator(String attributeName) {
        this.attributeName = attributeName;
    }

    public static AttributeLengthValidator on(String attribute) {
        return new AttributeLengthValidator(attribute);
    }

    public void validate(Validatable m) {
        Object value = m.get(this.attributeName);

        if(allowBlank && (null == value || "".equals(value))) {
            return;
        }

        if(null == value) {
            m.addValidator(this, this.attributeName);
            return;
        }

        if(!(value instanceof String)) {
            throw new IllegalArgumentException("Attribute must be a String");
        } else {
            if(!this.lengthOption.validate((String)((String)m.get(this.attributeName)))) {
                //somewhat confusingly this adds an error for a validator.
                m.addValidator(this, this.attributeName);
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
