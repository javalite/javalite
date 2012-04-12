package org.javalite.activeweb.freemarker;

/**
 * This class is used to pass data to the &lt;@select/&gt; tag.
 *
 * @author Igor Polevoy: 4/12/12 1:53 PM
 */
public class SelectOption {

    private String value, label;
    private boolean enabled = true, selected = false;

    /**
     * Value is used for both: value and label. Enabled, not selected by default
     *
     * @param value value and label to display (will be converted to string
     */
    public SelectOption(String value) {
        this.value = String.valueOf(value);
        this.label = value;
    }

    /**
     * Enabled, not selected by default
     *
     * @param value option value (will be converted to string
     * @param label option label
     */
    public SelectOption(Object value, String label) {
        this.value = String.valueOf(value);
        this.label = label;
    }

    /**
     * Creates new instance with all properties. Enabled by default.
     *
     * @param value option value (will be converted to string)
     * @param label option label
     * @param selected true to select, false otherwise
     */
    public SelectOption(Object value, String label, boolean selected) {
        this.value = String.valueOf(value);
        this.label = label;
        this.selected = selected;
    }

    public String getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
