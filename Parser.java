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
    
    public static final int C_LABEL = 3;
    public static final int C_GOTO = 4;
    public static final int C_IF = 5;
    public static final int C_FUNCTION = 6;
    public static final int C_RETURN = 7;
    public static final int C_CALL = 8;

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
        if (cmd.equals("label")) return C_LABEL;
        if (cmd.equals("goto")) return C_GOTO;
        if (cmd.equals("if-goto")) return C_IF;
        if (cmd.equals("function")) return C_FUNCTION;
        if (cmd.equals("return")) return C_RETURN;
        if (cmd.equals("call")) return C_CALL;
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