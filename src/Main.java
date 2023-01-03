import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;

public class Main {

    public static void main(String[] args) throws FileNotFoundException {
        Tokens tokens = new Tokens();
        Lexer lexer = new Lexer(tokens);
        GenCode genCode = new GenCode();
        Parser parser = new Parser(tokens);

        boolean error =lexer.Lexing();
        if(!error) {
            Node node = parser.Parsing();
            if (!Objects.isNull(node)) {

                String codeASM = genCode.generationCode(node);
                if (codeASM != null) {
                    try {
                        FileWriter myWriter = new FileWriter("KP-25-java-IO-02-Shevchuk.asm");
                        myWriter.write(codeASM);
                        myWriter.close();
                        System.out.println("\n\nCode MASM 32 in file KP-25-java-IO-02-Shevchuk.asm");
                    } catch (IOException e) {
                        System.out.println("Error with writing code to file.");
                        e.printStackTrace();
                    }
                }
            }
        }
        System.out.println("\nCode generation from C to MASM32 end.");
    }
}
