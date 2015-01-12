package org.javalite.templator;

import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.javalite.common.Util.blank;
import static org.javalite.common.Util.split;

/**
 * @author Igor Polevoy on 1/10/15.
 */
public class Template {

    private final List<TemplateToken> builtIn = new ArrayList<TemplateToken>();

    public Template(String template) {
        parse(template);
    }

    //for testing only
    List<TemplateToken> builtInTokens() {
        return builtIn;
    }

    private void parse(String template) {

        int cursor = 0; // on the left are processed chars, on the right are not processed
        for (int i = 0; i < template.length(); i++) {
            if (i < (template.length() - 1)) {
                // parse merge fields like ${first_name} or ${person.firstName}
                if (template.charAt(i) == '$' && template.charAt(i + 1) == '{') {
                    String[] arr = {"}"};
                    Token t = findFirst(template, arr, i, false, null);
                    if (cursor == 0) {
                        builtIn.add(new StringToken(template.substring(cursor, i)));
                    } else {
                        builtIn.add(new StringToken(template.substring(cursor + 1, i)));
                    }
                    builtIn.add(new MergeToken(template.substring(i + 2, t.index)));
                    i = cursor = t.index;

                }

                if (template.charAt(i) == '<' && template.charAt(i + 1) == '#') {
                    int firstSpace = template.indexOf(' ', i);
                    String tagName = template.substring(i + 2, firstSpace);
                    String[] closers = {"/>", " >", "</#" + tagName + ">"};
                    Token next = findFirst(template, closers, i + 2, true, null);

                    //<#for collection=people partial=person />
                    if (next.token.equals("/>")) {
                        String argumentLine = template.substring(i + tagName.length() + 2, next.index);
                        if (cursor == 0) {
                            builtIn.add(new StringToken(template.substring(cursor, i)));
                        } else {
                            builtIn.add(new StringToken(template.substring(cursor + 1, i)));
                        }
                        AbstractTag tag = TemplatorConfig.instance().getTag(tagName);
                        tag.setArguments(argumentLine);
                        builtIn.add(tag);
                        i = cursor = next.index + next.token.length();
                    }

                    //<#list collection=people partial=person > body </#list>
                    if (next.token.equals(" >")) {
                        String argumentLine = template.substring(i + tagName.length() + 2, next.index);
                        if (cursor == 0) {
                            builtIn.add(new StringToken(template.substring(cursor, i)));
                        } else {
                            builtIn.add(new StringToken(template.substring(cursor + 1, i)));
                        }
                        //need to get a body here.

                        String[] closer = {"</#" + tagName + ">"};
                        Token endTag = findFirst(template, closer, next.index + 2, false, null);

                        String body = template.substring(next.index + 2, endTag.index);

                        AbstractTag tag = TemplatorConfig.instance().getTag(tagName);
                        tag.setBody(body);
                        tag.setArguments(argumentLine);
                        builtIn.add(tag);
                        i = cursor = endTag.index + endTag.token.length() - 1;
                    }
                }
            }

            //process tail if there is some
            if (i == (template.length() - 1)) {
                String tail = template.substring(cursor + 1, i + 1);
                if (!blank(tail)) {
                    builtIn.add(new StringToken(tail));
                }
            }
        }
    }

    private Map<String, String> getArguments(String argumentLine) {
        Map<String, String> arguments = new HashMap<String, String>();
        String[] pairs = split(argumentLine, ' ');
        for (String pair : pairs) {
            if (!pair.contains("=") || pair.startsWith("=") || pair.endsWith("=")) {
                throw new ParseException("Argument line: " + argumentLine + " does not have correct arguments");
            }
            String[] argument = split(pair, '=');
            arguments.put(argument[0], argument[1]);
        }
        return arguments;
    }


    private static class Token {
        int index;
        String token;

        private Token(int index, String token) {
            this.index = index;
            this.token = token;
        }
    }


    private Token findFirst(String template, String[] toFind, int start, boolean suppressException, String[] excludes) {
        //TODO: test if opening or closing characters from other tags interleave
        //TODO: we only expect closing chars for the one that found here.


        int closingIndex = -1;

        Token first = null;

        for (String toFindString : toFind) {
            int i = template.indexOf(toFindString, start);
            if (i != -1) {
                if (first == null) {
                    first = new Token(i, toFindString);
                } else {

                    if (i < first.index) {
                        first = new Token(i, toFindString);
                    }
                }

            }
        }

//
//        if (!suppressException && closingIndex == -1) {
//            //TODO:best to provide a line number and index on line. Have to decompose template into lines.
//            throw new ParseException("Failed to find closer  " + toFind + " for field at index " + start);
//        }
        return first;
    }

    public void process(Map values, Writer writer) {

        try {
            for (TemplateToken token : builtIn) {
                token.process(values, writer);
            }
        } catch (Exception e) {
            throw new TemplateException(e);
        }
    }
}
