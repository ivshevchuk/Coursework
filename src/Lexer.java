import java.io.File;
import java.io.FileNotFoundException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer {
    private final Tokens tokens;

    public Lexer(Tokens tokens) {
        this.tokens=tokens;
    }

    public boolean Lexing() throws FileNotFoundException{
        ReaderFromFile compiler = new ReaderFromFile(new File("KP-25-java-IO-02-Shevchuk.c"));
        boolean error;
        String currNextLine;
        String currNext;

        Pattern pattern = Pattern.compile("&{2}|/{2,}|b[01]+\\b|[0-9]+|[a-zA-Z_][a-zA-Z_0-9]*|\\S");
        Matcher matcher;

        System.out.println("Lexemes:");
        while (compiler.getScanner().hasNextLine()) {
            currNextLine = compiler.getScanner().nextLine();
            currNextLine=currNextLine.replace("  ", "");

            matcher = pattern.matcher(currNextLine);
            if(!currNextLine.startsWith("//"))
                System.out.println("\n'" + currNextLine + "'");
            while (matcher.find()) {
                currNext = currNextLine.substring(matcher.start(), matcher.end());

                //comments lexing
                if (currNext.equals("//")) break;

                error = tokens.setPair(currNext);
                if (!error) {
                    System.out.println("Error while lexing the program in the row '" + currNextLine + "'");
                    return true;
                }
            }
        }
        return false;
    }
}