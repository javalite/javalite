package org.javalite.templator;

import org.javalite.common.Inflector;

import java.io.Writer;
import java.util.List;
import java.util.Map;

import static org.javalite.common.Collections.list;


/**
 * This class represents a custom tag written in Java and linked into template manager, such as:
 * <p/>
 * <code>
 * <#list people as person > name: ${person.name} </#list>
 * </code>
 *
 * @author Igor Polevoy on 1/10/15.
 */
public abstract class AbstractTag extends TemplateToken {


    private String argumentLine, body, tagName, matchingEnd = null;
    private int tagStartIndex = -1, tagEndIndex = -1, argumentsEndIndex = -1;

    public AbstractTag() {
        String underscore = Inflector.underscore(getClass().getSimpleName());
        tagName = underscore.contains("_") ? underscore.substring(0, underscore.indexOf("_")) : underscore;

    }

    public String getMatchingEnd() {
        return matchingEnd;
    }

    public int getArgumentsEndIndex() {
        return argumentsEndIndex;
    }

    public int getTagStartIndex() {
        return tagStartIndex;
    }

    public void setTagStartIndex(int tagStartIndex) {
        this.tagStartIndex = tagStartIndex;
    }

    public int getTagEndIndex() {
        return tagEndIndex;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    protected String getArgumentLine() {
        return argumentLine;
    }

    public void setArguments(String argumentLine) {
        this.argumentLine = argumentLine;
    }


    public String getTagName() {
        return tagName;
    }

    @Override
    public abstract void process(Map values, Writer writer);

    @Override
    String originalValue() {
        return null;
    }


    /**
     * Returns entire start tag  as in <code>&lt;@list</code>.
     *
     * @return start tag text.
     */
    public String getTagStart() {
        return "<@" + tagName;
    }


    /**
     * This method has a side effect!
     * <p/>
     * Finds out if  characters preceding the index match at least one  tag end.
     * <p/>
     * For template:
     * <hr>
     * <pre>
     *  This list of books: &lt;#list readers as reader /&gt; is your reading assignment.
     * </pre>
     * <hr>
     * If the tag has one of the ends as "/&gt;", and the <code>index</code> is pointing to the first space character
     * in phrase " is your...", than the <code>matchingEnd</code>  and <code>tagEndIndex</code> values are set and
     * this method returns <code>true</code>.
     *
     * @param template template text.
     * @param index    current index.
     * @return true if there is a match of at least one end.
     */
    public boolean matchEndTag(String template, int index) {
        List<String> ends = getEnds();
        for (String end : ends) {
            boolean haveEnoughSpaceForEndTag = end.length() < index;
            boolean endTagMatchesLeftOfIndex = template.substring(index - end.length(), index).equals(end);
            boolean indexOnEnd = index == template.length() - 1;
            boolean endTagMatchesEndOfTemplate = template.substring(index - end.length() + 1).equals(end);

            if (haveEnoughSpaceForEndTag && endTagMatchesLeftOfIndex) {
                matchingEnd = end;
                tagEndIndex = index - end.length();
                return true;
            }

            if (indexOnEnd && endTagMatchesEndOfTemplate) {
                matchingEnd = end;
                tagEndIndex = index - end.length() + 1;
            }

            if (matchingEnd != null) {
                return true;
            }
        }
        return false;
    }

    public boolean matchStartAtIndex(String template, int index) {
        return index >= getTagStart().length()
                && template.substring(index - getTagStart().length(), index).equals(getTagStart());
    }


    public List<String> getEnds() {
        return list("</@" + tagName + ">", "/>");
    }

    public String getArgumentEnd() {
        return null;

    }

    public String getMiddle() {
        return null;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "argumentLine=" + argumentLine +
                ", body='" + body + '\'' +
                '}';
    }

    public boolean marchArgumentEnd(String template, int index) {
        String argumentEnd = getArgumentEnd();
        if (argumentEnd == null) {
            return false;
        } else if ((index + 1) >= argumentEnd.length()
                && template.substring(index - argumentEnd.length(), index).equals(argumentEnd)) {
            argumentsEndIndex = index - argumentEnd.length();
            return true;
        }
        return false;
    }
    public boolean matchMiddle(String template, int templateIndex) {
        return false;
    }
}
