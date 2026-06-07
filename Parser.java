import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Parser {
    private Scanner scanner;
    private String currentCommand;
    private String[] currentTokens;

    public static final int C_ARITHMETIC = 0;
    public static final int C_PUSH = 1;
    public static final int C_POP = 2;

    public Parser(File file) {
        try {
            scanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            System.err.println("Erro: Arquivo não encontrado.");
        }
    }

    public boolean hasMoreCommands() {
        while (scanner != null && scanner.hasNextLine()) {
            String line = scanner.nextLine();
            int commentIndex = line.indexOf("//");
            if (commentIndex != -1) line = line.substring(0, commentIndex);
            line = line.trim();

            if (!line.isEmpty()) {
                currentCommand = line;
                currentTokens = currentCommand.split("\\s+");
                return true;
            }
        }
        return false;
    }

    public void advance() {}

    public int commandType() {
        String cmd = currentTokens[0];
        if (cmd.equals("push")) return C_PUSH;
        if (cmd.equals("pop")) return C_POP;
        return C_ARITHMETIC; 
    }

    public String arg1() {
        if (commandType() == C_ARITHMETIC) return currentTokens[0];
        return currentTokens[1];
    }

    public int arg2() {
        return Integer.parseInt(currentTokens[2]);
    }
}