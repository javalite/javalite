package org.javalite.app_config;

/**
 * Identifies a configuration property. These properties are defined in environment - specific files, such as:
 * <code>jdbc.prod.properties</code>.
 *
 * @author Igor Polevoy
 */
public class Property {
    private String name;
    private String value;
    private String propertyFile;

    /**
     * @param name name of property.
     * @param value value of property.
     * @param propertyFile - path to property file this value is defined in.
     */
    public Property(String name, String value, String propertyFile) {
        this.name = name;
        this.value = value;
        this.propertyFile = propertyFile;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    void setValue(String value) {
        this.value = value;
    }

    public String getPropertyFile() {
        return propertyFile;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Property)){
            return false;
        }
        Property other = (Property) obj;
        return this.value.equals(other.value)
                && this.propertyFile.equals(other.propertyFile)
                && this.name.equals(other.name);
    }
}
