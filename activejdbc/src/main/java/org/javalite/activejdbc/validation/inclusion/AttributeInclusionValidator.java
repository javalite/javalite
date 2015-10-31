package org.javalite.activejdbc.validation.inclusion;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.validation.ValidatorAdapter;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Validates that the attribute is included in a given list.
 */
public class AttributeInclusionValidator extends ValidatorAdapter {

    private final String attribute;
    private List list;

    private AttributeInclusionValidator(String attribute) {
        this.attribute = attribute;
        setMessage("{0} is not included in the list.");
    }

    public static AttributeInclusionValidator on(String attribute) {
        return new AttributeInclusionValidator(attribute);
    }

    @Override
    public void validate(Model m) {
        Object o = m.get(attribute);
        if (!list.contains(o)) {
            m.addValidator(this, attribute);
        }
    }

    public AttributeInclusionValidator with(List list) {
        this.list = list;
        return this;
    }

    public AttributeInclusionValidator with(Object... items) {
        this.list = Arrays.asList(items);
        return this;
    }

    @Override
    public String formatMessage(Locale locale, Object... params) {
        return super.formatMessage(locale, attribute);
    }
}
