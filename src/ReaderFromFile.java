import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class ReaderFromFile {
    public Scanner getScanner() {
        return scanner;
    }
    private final Scanner scanner;

    public ReaderFromFile(File input) throws FileNotFoundException {
        scanner = new Scanner(input);
    }

}