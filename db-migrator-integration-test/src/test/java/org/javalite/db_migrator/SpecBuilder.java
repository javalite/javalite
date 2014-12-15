package org.javalite.db_migrator;

/**
 * @author Igor Polevoy on 12/12/14.
 */
public class SpecBuilder {

    private String value;

    public SpecBuilder(String value) {
        this.value = value;
    }

    public static SpecBuilder the(String value) {
        return new SpecBuilder(value);
    }

    public void shouldContain(String substring) {
        if (!value.contains(substring)) {
            throw new RuntimeException("tested object does not contain this value");
        }
    }
}
