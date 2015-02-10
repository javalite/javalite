package org.javalite.templator;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * @author Igor Polevoy on 1/10/15.
 */
public class Template {

    private final List<TemplateToken> templateTokens = new ArrayList<TemplateToken>();

    public Template(String template) {
        try {
            parse(template);
        } catch (ParseException e) {
            throw e;
        } catch (Exception e) {
            throw new TemplateException(e);
        }

    }

    //for testing only
    List<TemplateToken> templateTokens() {
        return templateTokens;
    }

    Stack<AbstractTag> stack = new Stack<AbstractTag>();

    private void parse(String template) throws IllegalAccessException, InstantiationException {
        Map<String, Class> tags = TemplatorConfig.instance().getTags();

        List<AbstractTag> tagsList = new ArrayList<AbstractTag>();

        //TODO: separate iterations through all tags, and focus on stack
        for (int templateIndex = 0; templateIndex < template.length(); templateIndex++) {
            for (String tagName : tags.keySet()) {
                Class tagClass = tags.get(tagName);

                AbstractTag tag; // ony used to test things
                tag = (AbstractTag) tagClass.newInstance();

                //match tag start, such as "<#list"
                if (tag.matchStartAtIndex(template, templateIndex)) {
                    tag.setTagStartIndex(templateIndex - tag.getTagStart().length());
                    stack.push(tag);
                    continue;
                }

                //match tag argument end, such as:    <#list people as person > body </#list>
                //                                                         ---^
                if (!stack.isEmpty()
                        && stack.peek().getArgumentsEndIndex() == -1
                        && stack.peek().marchArgumentEnd(template, templateIndex)) {
                    AbstractTag currentTag = stack.peek();
                    int argumentsStartIndex = currentTag.getTagStartIndex() + currentTag.getTagStart().length();
                    int argumentsEndIndex = currentTag.getArgumentsEndIndex();

                    String arguments = template.substring(argumentsStartIndex, argumentsEndIndex);
                    currentTag.setArguments(arguments);
                }

                //match tag end, such as:    <#list people as person > body </#list>
                //                                                       ---^
                if (!stack.isEmpty()
                        && stack.peek().matchEndTag(template, templateIndex)) {

                    AbstractTag currentTag = stack.pop();
                    if (stack.size() == 0) {
                        tagsList.add(currentTag);
                        String arguments;
                        if (currentTag.getArgumentsEndIndex() != -1) { // we have body. case like this: <#list people as person > body </#list>
                            arguments = template.substring(currentTag.getTagStartIndex() + currentTag.getTagStart().length(), currentTag.getArgumentsEndIndex());
                            String body = template.substring(currentTag.getArgumentsEndIndex() + 1, currentTag.getTagEndIndex());
                            currentTag.setBody(body);
                        } else {
                            // this is what is between the start and end tag in case there is no body:
                            ///<@blah arguments for blah />
                            arguments = template.substring(currentTag.getTagStartIndex() + currentTag.getTagStart().length(), currentTag.getTagEndIndex());
                        }
                        currentTag.setArguments(arguments);
                    } else {
                        break;
                    }
                }
            }
        }

        if (tagsList.size() == 0) { // assuming that this is just text, no funny business
            templateTokens.add(new StringToken(template));
        } else {
            //now we need to collect string chunks in between
            for (int i = 0; i < tagsList.size(); i++) {
                AbstractTag currentTag = tagsList.get(i);

                // if this is a first tag and not at beginning of template
                if (i == 0 && currentTag.getTagStartIndex() != 0) {
                    templateTokens.add(new StringToken(template.substring(0, currentTag.getTagStartIndex())));
                }

                // if there is a gap between current and previous
                if (tagsList.size() > 1 && i != 0) {
                    int startIndex = tagsList.get(i - 1).getTagEndIndex() + tagsList.get(i - 1).getMatchingEnd().length();
                    int endIndex = currentTag.getTagStartIndex();
                    StringToken st = new StringToken(template.substring(startIndex, endIndex));
                    templateTokens.add(st);
                }

                templateTokens.add(currentTag);

                // if this is the last tag and there is some text after
                if (i == (tagsList.size() - 1) && template.length() > currentTag.getTagEndIndex()) {
                    StringToken st = new StringToken(template.substring(currentTag.getTagEndIndex() + currentTag.getMatchingEnd().length()));
                    templateTokens.add(st);
                }
            }
        }

        if (!stack.isEmpty()) {
            throw new ParseException("At least one tag is not closed: " + stack.peek().getTagName());
        }
    }

    public void process(Map values, Writer writer) {
        try {
            for (TemplateToken token : templateTokens) {
                token.process(values, writer);
            }
        } catch (TemplateException e) {
            throw e;
        } catch (Exception e) {
            throw new TemplateException(e);
        }
    }
}
