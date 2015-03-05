package org.javalite.templator;

import java.util.ArrayList;
import java.util.List;


/**
 * BNF rules:
 *
 * <pre>
 * WHITESPACE = Character.isWhitespace()+
 * IDENTIFIER = Character.isJavaIdentifierStart() Character.isJavaIdentifierPart()*
 * IDENTIFIER_OR_FUNCTION = IDENTIFIER ("()")?
 * CHAINED_IDENTIFIERS = IDENTIFIER ('.' IDENTIFIER_OR_FUNCTION)*
 * VAR = "%{" CHAINED_IDENTIFIER (WHITESPACE IDENTIFIER)? '}'
 *
 * CONST = .*
 * </pre>
 *
 * @author Eric Nielsen
 */
final class TemplateParser {

    private final List<TemplateNode> nodes = new ArrayList<TemplateNode>();
    private final String source;
    private int index = -1;
    private int currentCodePoint = 0;
    private int constStartIndex;

    TemplateParser(String source) {
        this.source = source;
    }

    private boolean next() {
        if (++index < source.length()) {
            currentCodePoint = source.codePointAt(index);
            return true;
        } else {
            return false;
        }
    }

    @SuppressWarnings("empty-statement")
    private boolean _whitespace() {
        if (!Character.isWhitespace(currentCodePoint)) { return false; }
        while (next() && Character.isWhitespace(currentCodePoint));
        return true;
    }

    @SuppressWarnings("empty-statement")
    private boolean _identifier() {
        if (!Character.isJavaIdentifierStart(currentCodePoint)) { return false; }
        while (next() && Character.isJavaIdentifierPart(currentCodePoint));
        return true;
    }

    private boolean _identifierOrFunction() {
        if (!_identifier()) { return false; }
        if (currentCodePoint == '(') {
            if (!(next() && currentCodePoint == ')')) { return false; }
            next();
        }
        return true;
    }

    private boolean _chainedIdentifiers(List<String> identifiers) {
        int startIndex = index;
        if (!_identifier()) { return false; }
        identifiers.add(source.substring(startIndex, index));
        while (currentCodePoint == '.') {
            if (!next()) { return false; }
            startIndex = index;
            if (!_identifierOrFunction()) { return false; }
            identifiers.add(source.substring(startIndex, index));
        }
        return true;
    }

    private boolean _var() {
        int startIndex = index;
        if (currentCodePoint != '%') { return false; }
        if (!(next() && currentCodePoint == '{')) { return false; }
        List<String> identifiers = new ArrayList<String>();
        if (!(next() && _chainedIdentifiers(identifiers))) { return false; }
        BuiltIn builtIn = null;
        if (_whitespace()) {
            int builtInStartIndex = index;
            if (!_identifier()) { return false; }
            builtIn = TemplatorConfig.instance().getBuiltIn(source.substring(builtInStartIndex, index));
        }
        if (currentCodePoint != '}') { return false; }
        next();
        //TODO: refator this
        addConstEndingAt(startIndex);
        constStartIndex = index;
        nodes.add(new VarNode(identifiers, builtIn));
        return true;
    }

    @SuppressWarnings("empty-statement")
    List<TemplateNode> parse() {
        //TODO: refactor this
        if (next()) {
            for (;;) {
                int startIndex = index;
                if (!_var()) {
                    if (startIndex == index) {
                        if (!next()) { break; }
                    }
                }
            }
        }
        addConstEndingAt(source.length());
        return nodes;
    }

    private void addConstEndingAt(int endIndex) {
        if (endIndex - 1 > constStartIndex) {
            nodes.add(new ConstNode(source.substring(constStartIndex, endIndex)));
        }
    }
}
