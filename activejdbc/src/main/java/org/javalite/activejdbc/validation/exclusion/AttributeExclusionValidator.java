package org.javalite.activejdbc.validation.exclusion;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.validation.ValidatorAdapter;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Validates that the attribute is not included in a given list.
 */
public class AttributeExclusionValidator extends ValidatorAdapter {

    private final String attribute;
    private List list;

    private AttributeExclusionValidator(String attribute) {
        this.attribute = attribute;
        setMessage("{0} is reserved.");
    }

    public static AttributeExclusionValidator on(String attribute) {
        return new AttributeExclusionValidator(attribute);
    }

    @Override
    public void validate(Model m) {
        Object o = m.get(attribute);
        if (list.contains(o)) {
            m.addValidator(this, attribute);
        }
    }

    public AttributeExclusionValidator with(List list) {
        this.list = list;
        return this;
    }

    public AttributeExclusionValidator with(Object... items) {
        this.list = Arrays.asList(items);
        return this;
    }

    @Override
    public String formatMessage(Locale locale, Object... params) {
        return super.formatMessage(locale, attribute);
    }
}
