import java.util.regex.Pattern;

public enum ListOfTokens {
    KEYWORD_INCLUDE("include"),
    KEYWORD_INT("int"),
    KEYWORD_FLOAT("float"),
    KEYWORD_DOUBLE("double"),
    KEYWORD_CHAR("char"),
    KEYWORD_VOID("void"),
    KEYWORD_IF("if"),
    KEYWORD_ELSE("else"),
    KEYWORD_RETURN("return"),
    KEYWORD_MAIN("main"),
    KEYWORD_FOR("for"),
    KEYWORD_BREAK("break"),
    KEYWORD_CONTINUE("continue"),
    INT_CONSTANT("[0-9]+"),
    INT_BIN_CONSTANT("b[01]+\\b"),
    IDENTIFIER("[a-zA-Z_][a-zA-Z_0-9]*"),
    OPEN_CAST("\\("),
    DIVIDE_ASSIGN("/="),
    OPEN_BRACE("\\{"),
    CLOSE_CAST("\\)"),
    CLOSE_BRACE("}"),
    EQUALS("="),
    MORE_THAN(">"),
    LESS_THAN("<"),
    BITWISE_COMPLEMENT("~"),
    LOGICAL_AND("&&"),
    LOGICAL_NEGATION("!"),
    ADDITION("\\+"),
    NEGATION("-"),
    MULTIPLICATION("\\*"),
    DIVISION("/"),
    MODULO("%"),
    BINARY_NOT("%"),
    HASH("#"),
    DOT("\\."),
    COMMA(","),
    SPACE_N("\\n"),
    SEMICOLONS(";"),
    UNKNOWN("");

    public final String pattern;

    ListOfTokens(String regex) {
        pattern = String.valueOf(Pattern.compile(regex));
    }
}
