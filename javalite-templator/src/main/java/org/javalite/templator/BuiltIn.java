package org.javalite.templator;

/**
 * This interface is implemented by classes that need to do additional processing of strings
 * during a template merge. For instance:
 *
 * <code>
 *     ${article.text esc}
 * </code>
 *
 * where <code>esc</code> is a name of a built-in registered with a name "esc".
 *
 * <p></p>
 *
 * Think of a built-in as a function that will do the last minute formatting of a value before merging it into
 * a template.
 *
 * <p></p>
 *
 * <strong>Subclasses must be stateless.</strong>
 *
 * @author Igor Polevoy on 1/15/15.
 */
public interface BuiltIn {


    /**
     * Will manipulate input. The returned value is the one ultimately displayed in a template.
     * For instance here:
     *
     * <code>
     *     ${article.text esc}
     * </code>
     *
     * the value <code>article.text</code>, <code>esc</code> is a name of a built-in and a returned value
     * from a built-in is merged into template.
     *
     *
     * @param input value passed to template for merging
     * @return value passed to template for merging after processing by this built-in
     */
    String process(String input);
}
