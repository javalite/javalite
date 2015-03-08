package org.javalite.templator;

import java.util.ArrayList;

/**
 * BNF rules:
 *
 * <pre>
 * ID = javaIdentifierStart javaIdentifierPart*
 * ID_OR_FUNC = ID "()"?
 * CHAINED_IDS = ID ('.' ID_OR_FUNC)*
 * LOWER_ALPHA = [a-z]+
 * VAR = "%{" CHAINED_IDS (LOWER_ALPHA)? '}'
 *
 * CONST = .*
 *
 * COMPARISON_OPERATOR = "==" | "&gt;" | "&gt;=" | "&lt;" | "&lt;=" | "!="
 * EXP_ID = CHAINED_IDS (COMPARISON_OPERATOR CHAINED_IDS)?
 * EXP = TERM ("||" TERM)?
 * TERM = FACTOR ("&& FACTOR)?
 * FACTOR = EXP_ID | ('!' FACTOR) | ('(' EXP ')')
 *
 *
 * TAG_START = '&lt' '#'
 * TAG_END = '&lt' '/' '#' LOWER_ALPHA '&gt;'
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
    private boolean _id() {
        if (!acceptJavaIdentifierStart()) { return false; }
        while (acceptJavaIdentifierPart());
        return true;
    }

    private boolean _idOrFunc() {
        return _id() && (!accept('(') || accept(')'));
    }

    private ChainedIds _chainedIds() {
        int startIndex = index;
        if (!_id()) { return null; }
        String firstId = source.substring(startIndex, index);
        ArrayList<String> ids = new ArrayList<String>();
        while (accept('.')) {
            startIndex = index;
            if (!_idOrFunc()) { return null; }
            ids.add(source.substring(startIndex, index));
        }
        ids.trimToSize();
        return new ChainedIds(firstId, ids);
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
        ChainedIds chainedIds = _chainedIds();
        if (chainedIds == null) { return null; }
        BuiltIn builtIn = null;
        if (_whitespace()) {
            String builtInName = _lowerAlpha();
            if (builtInName != null) {
                builtIn = TemplatorConfig.instance().getBuiltIn(builtInName);
                _whitespace(); // optional
            }
        }
        if (!accept('}')) { return null; }
        return new VarNode(chainedIds, builtIn);
    }

    private Comparison.Operator _comparisonOperator() {
        if (done) { return null; }
        switch (currentChar) {
        case '=':
            accept();
            return accept('=') ? Comparison.Operator.eq : null;
        case '>':
            accept();
            return accept('=') ? Comparison.Operator.gte : Comparison.Operator.gt;
        case '<':
            accept();
            return accept('=') ? Comparison.Operator.lte : Comparison.Operator.lt;
        case '!':
            accept();
            return accept('=') ? Comparison.Operator.neq : null;
        default:
            return null;
        }
    }

    private Exp _expId() {
        ChainedIds chainedIds = _chainedIds();
        if (chainedIds == null) { return null; }
        _whitespace(); // optional
        //TODO: refactor this
        int currentIndex = index;
        Comparison.Operator operator = _comparisonOperator();
        if (operator == null) {
            return currentIndex == index ? new BooleanId(chainedIds) : null;
        } else {
            _whitespace(); // optional
            ChainedIds rightOperand = _chainedIds();
            if (rightOperand == null) { return null; }
            return new Comparison(chainedIds, operator, rightOperand);
        }
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
        if (done) { return null; }
        Exp exp;
        switch (currentChar) {
        case '(':
            accept();
            _whitespace(); // optional
            exp = _exp();
            if (exp == null) { return null; }
            _whitespace(); // optional
            if (!accept(')')) { return null; }
            return exp;
        case '!':
            accept();
            _whitespace(); // optional
            exp = _factor();
            return exp == null ? null : new NotExp(exp);
        default:
            return _expId();
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

    private ParentNode _ifTagStart() {
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
