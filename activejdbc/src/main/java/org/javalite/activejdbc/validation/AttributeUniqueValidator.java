/**
 * @author Kadvin, Date: 11-8-6 下午2:30
 */
package org.javalite.activejdbc.validation;

import org.javalite.activejdbc.Messages;
import org.javalite.activejdbc.Model;

import java.util.Locale;

/**
 * Validate the attribute is unique or not
 */
public class AttributeUniqueValidator extends ValidatorAdapter {
    private String attribute;

    public AttributeUniqueValidator(String attribute) {
        this.attribute = attribute;
        setMessage("value is not unique");
    }

    public void validate(Model m) {
        Model exist;
        Object uniqueValue = m.get(attribute);
        //TODO I don't support nullable unique value now!
        if( uniqueValue == null ){
            m.addError(attribute, "unique value can't be null!");
            return;
        }
        if (m.isNew()) {
            exist = Model.findFirst(String.format("%s = ?", attribute), uniqueValue);
        }else{
            //Avoid change self key as other's key while modification
            exist = Model.findFirst(String.format("(%s = ?) and (id != ?)", attribute), uniqueValue, m.getId());
        }
        if( exist != null ){
            m.addValidator(this, attribute);
        }
    }

    @Override
    public int hashCode() {
        return attribute.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if(!this.getClass().equals(other.getClass())) return false;

        return this.attribute.equals(((AttributeUniqueValidator)other).attribute);
    }

    public String formatMessage(Locale locale) {
        return locale != null ? Messages.message(getMessage(), locale) : Messages.message(getMessage());
    }
}
