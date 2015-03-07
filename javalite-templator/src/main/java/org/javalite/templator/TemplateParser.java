package org.javalite.templator;

import java.util.ArrayList;

/**
 * BNF rules:
 *
 * <pre>
 * IDENTIFIER = Character.isJavaIdentifierStart() Character.isJavaIdentifierPart()*
 * IDENTIFIER_OR_FUNCTION = IDENTIFIER "()"?
 * CHAINED_IDENTIFIERS = IDENTIFIER ('.' IDENTIFIER_OR_FUNCTION)*
 * LOWER_ALPHA = [a-z]+
 * VAR = "%{" CHAINED_IDENTIFIER (LOWER_ALPHA)? '}'
 *
 * CONST = .*
 *
 * COMPARISON_OP = "==" | "&gt;" | "&gt;=" | "&lt;" | "&lt;=" | "!="
 * COMPARISON = CHAINED_IDENTIFIERS COMPARISON_OP CHAINED_IDENTIFIERS
 * EXP = TERM ("||" TERM)?
 * TERM = FACTOR ("&& FACTOR)?
 * FACTOR = COMPARISON | ('!' FACTOR) | ('(' EXP ')')
 *
 *
 * TAG_START = '&lt' '#'
 * TAG_END = '&lt' '/' '#' LOWER_ALPHA '>'
 * IF_TAG = TAG_START "if" '(' EXPRESSION ')' '&gt;' LIST_TAG_BODY TAG_END "if" '&gt;'
 * </pre>
 *
 * @author Eric Nielsen
 */
public final class TemplateParser {

    private final String source;
    private int index;
    private boolean done;
    private char currentChar;
    private int constStartIndex;

    TemplateParser(String source) {
        this.source = source;
    }

    private void accept() {
        if (++index < source.length()) {
            currentChar = source.charAt(index);
        } else {
            done = true;
        }
    }
    private boolean accept(char c) {
         if (!done && currentChar == c) {
             accept();
             return true;
         } else {
             return false;
         }
    }

    private boolean acceptWhitespace() {
        if (!done && currentChar <= ' ') {
             accept();
             return true;
         } else {
             return false;
         }
    }
    @SuppressWarnings("empty-statement")
    private boolean _whitespace() {
        if (!acceptWhitespace()) { return false; }
        while (acceptWhitespace());
        return true;
    }

    private boolean acceptJavaIdentifierStart() {
        if (!done && Character.isJavaIdentifierStart(currentChar)) {
             accept();
             return true;
         } else {
             return false;
         }
    }
    private boolean acceptJavaIdentifierPart() {
        if (!done && Character.isJavaIdentifierPart(currentChar)) {
             accept();
             return true;
         } else {
             return false;
         }
    }
    @SuppressWarnings("empty-statement")
    private boolean _identifier() {
        if (!acceptJavaIdentifierStart()) { return false; }
        while (acceptJavaIdentifierPart());
        return true;
    }

    private boolean _identifierOrFunction() {
        if (!_identifier()) { return false; }
        if (accept('(') && !accept(')')) { return false; }
        return true;
    }

    private Identifiers _chainedIdentifiers() {
        int startIndex = index;
        if (!_identifier()) { return null; }
        String firstIdentifier = source.substring(startIndex, index);
        ArrayList<String> identifiers = new ArrayList<String>();
        while (accept('.')) {
            startIndex = index;
            if (!_identifierOrFunction()) { return null; }
            identifiers.add(source.substring(startIndex, index));
        }
        identifiers.trimToSize();
        return new Identifiers(firstIdentifier, identifiers);
    }

    private boolean acceptLowerAlpha() {
        if (!done && currentChar >= 'a' && currentChar <= 'z') {
            accept();
            return true;
        } else {
            return false;
        }
    }
    @SuppressWarnings("empty-statement")
    private String _lowerAlpha() {
        int startIndex = index;
        if (!acceptLowerAlpha()) { return null; }
        while (acceptLowerAlpha());
        return source.substring(startIndex, index);
    }

    private VarNode _var() {
        if (!(accept('%') && accept('{'))) { return null; }
        _whitespace(); // optional
        Identifiers ident = _chainedIdentifiers();
        if (ident == null) { return null; }
        BuiltIn builtIn = null;
        if (_whitespace()) {
            String builtInName = _lowerAlpha();
            if (builtInName != null) {
                builtIn = TemplatorConfig.instance().getBuiltIn(builtInName);
                _whitespace(); // optional
            }
        }
        if (!accept('}')) { return null; }
        return new VarNode(ident, builtIn);
    }

    private Op _comparisonOp() {
        if (accept('=')) {
            return accept('=') ? Op.eq : null;
        } else if (accept('>')) {
            return accept('=') ? Op.gte : Op.gt;
        } else if (accept('<')) {
            return accept('=') ? Op.lte : Op.lt;
        } else {
            return accept('!') && accept('=') ? Op.neq : null;
        }
    }

    private Comparison _comparison() {
        Identifiers leftIdent = _chainedIdentifiers();
        if (leftIdent == null) { return null; }
        _whitespace(); // optional
        Op op = _comparisonOp();
        if (op == null) { return null; }
        _whitespace(); // optional
        Identifiers rightIdent = _chainedIdentifiers();
        if (rightIdent == null) { return null; }
        return new Comparison(leftIdent, op, rightIdent);
    }

    private Exp _exp() {
        Exp leftExp = _term();
        if (leftExp == null) { return null; }
        _whitespace(); // optional
        if (accept('|')) {
            if (!accept('|')) { return null; }
            _whitespace(); // optional
            Exp rightExp = _term();
            if (rightExp == null) { return null; }
            return new OrExp(leftExp, rightExp);
        }
        return leftExp;
    }

    private Exp _term() {
        Exp leftExp = _factor();
        if (leftExp == null) { return null; }
        _whitespace(); // optional
        if (accept('&')) {
            if (!accept('&')) { return null; }
            _whitespace(); // optional
            Exp rightExp = _factor();
            if (rightExp == null) { return null; }
            return new AndExp(leftExp, rightExp);
        }
        return leftExp;
    }

    private Exp _factor() {
        if (accept('(')) {
            _whitespace(); // optional
            Exp exp = _exp();
            if (exp == null) { return null; }
            _whitespace(); // optional
            if (!accept(')')) { return null; }
            return exp;
        } else if (accept('!')) {
            _whitespace(); // optional
            Exp exp = _factor();
            return exp == null ? null : new NotExp(exp);
        } else {
            return _comparison();
        }
    }

    private boolean _tagStart() {
        if (!accept('<')) { return false; }
        _whitespace(); // optional
        if (!accept('#')) { return false; }
        return true;
    }

    private String _tagEnd() {
        if (!accept('<')) { return null; }
        _whitespace(); // optional
        if (!accept('/')) { return null; }
        _whitespace(); // optional
        if (!accept('#')) { return null; }
        String tagName = _lowerAlpha();
        if (tagName == null) { return null; }
        _whitespace(); // optional
        if (!accept('>')) { return null; }
        return tagName;
    }

    private Node _ifTagStart() {
        if (!_tagStart()) { return null; }
        String tagName = _lowerAlpha();
        if (!"if".equals(tagName)) { return null; }
        _whitespace(); // optional
        if (!accept('(')) { return null; }
        _whitespace(); // optional
        Exp exp = _exp();
        if (exp == null) { return null; }
        _whitespace(); // optional
        if (!accept(')')) { return null; }
        _whitespace(); // optional
        if (!accept('>')) { return null; }
        return new IfNode(exp);
    }

    Node parse() {
        ParentNode root = new RootNode();
        if (source == null || source.isEmpty()) { return root; }
        currentChar = source.charAt(0);
        //TODO: refactor this
         while (!done) {
            int startIndex = index;
            Node node = _ifTagStart();
            if (node != null) {
                addConstEndingAt(startIndex, root);
                constStartIndex = index;
                if (!"if".equals(parse((ParentNode) node))) {
                    node = null;
                }
            } else {
                node = _var();
            }
            if (node == null) {
                if (startIndex == index) {
                    accept();
                }
            } else {
                addConstEndingAt(startIndex, root);
                constStartIndex = index;
                root.children.add(node);
            }
        }
        addConstEndingAt(source.length(), root);
        return root;
    }

    private String parse(ParentNode root) {
        //TODO: refactor this
        constStartIndex = index;
        while (!done) {
            int startIndex = index;
            String tagEndName = _tagEnd();
            if (tagEndName != null) {
                addConstEndingAt(startIndex, root);
                constStartIndex = index;
                return tagEndName;
            }
            Node node = _ifTagStart();
            if (node == null) {
                node = _var();
            } else {
                if (!"if".equals(parse((ParentNode) node))) {
                    node = null;
                }
            }
            if (node == null) {
                if (startIndex == index) {
                    accept();
                }
            } else {
                addConstEndingAt(startIndex, root);
                constStartIndex = index;
                root.children.add(node);
            }
        }
        return null;
    }

    private void addConstEndingAt(int endIndex, ParentNode root) {
        if (endIndex - 1 > constStartIndex) {
            root.children.add(new ConstNode(source.substring(constStartIndex, endIndex)));
        }
    }
}
