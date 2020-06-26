package org.javalite.activejdbc.validation;

public interface Validatable {
    Object get(String attribute);
//    Object set(String attribute, Object val); // for backwards compatibility
    void addValidator(Validator validator, String attribute);

}
